import os
import sys
import time
import json
import logging
from datetime import datetime, timedelta, timezone
from typing import Dict, Any, List, Optional

import requests

from utils import http_get, http_post, iso_date, env_bool

# ---------- Config from env with sane defaults ----------
GET_ROOMS_URL = os.getenv("GET_ROOMS_URL", "http://motel-api.motel.svc.cluster.local:8085/motelApi/v1/motelRooms")
POST_PRICE_URL = os.getenv("POST_PRICE_URL", "http://reservation-api.reservation.svc.cluster.local:8086/reservationApi/v1/priceList")

PAGE_SIZE = int(os.getenv("PAGE_SIZE", "50"))
DAYS_AHEAD = int(os.getenv("DAYS_AHEAD", "10"))

DEFAULT_ROOM_TYPE = os.getenv("DEFAULT_ROOM_TYPE", "Deluxe Suite")
DEFAULT_PRICE = os.getenv("DEFAULT_PRICE", "199.99")
DEFAULT_AVAILABLE = os.getenv("DEFAULT_AVAILABLE", "12")
DEFAULT_BOOKED = os.getenv("DEFAULT_BOOKED", "0")
DEFAULT_STATUS = os.getenv("DEFAULT_STATUS", "Active")

# optional headers (e.g., auth). Comma-separated "Key: Value" pairs.
RAW_HEADERS = os.getenv("EXTRA_HEADERS", "").strip()
TIMEOUT_SECS = float(os.getenv("HTTP_TIMEOUT_SECS", "10"))
MAX_RETRIES = int(os.getenv("HTTP_MAX_RETRIES", "3"))
RETRY_BACKOFF_SECS = float(os.getenv("HTTP_RETRY_BACKOFF_SECS", "1.5"))

DRY_RUN = env_bool(os.getenv("DRY_RUN", "false"))

# ---------- Logging ----------
logging.basicConfig(
    level=os.getenv("LOG_LEVEL", "INFO"),
    format="%(asctime)s %(levelname)s %(message)s",
)
log = logging.getLogger("jobs.main")


def parse_headers(raw: str) -> Dict[str, str]:
    if not raw:
        return {}
    headers = {}
    for pair in raw.split(","):
        if ":" in pair:
            k, v = pair.split(":", 1)
            headers[k.strip()] = v.strip()
    return headers


COMMON_HEADERS = parse_headers(RAW_HEADERS)


def fetch_all_rooms() -> List[Dict[str, Any]]:
    """
    Paginates through GET_ROOMS_URL which is expected to return:
      {
        "response": {
          "http_code": "200",
          "data": {
            "content": [ ... ],
            "pagination": {
              "first": true/false, "last": true/false,
              "page": n, "size": m, "total_pages": t, ...
            }
          }
        }
      }
    """
    rooms: List[Dict[str, Any]] = []
    page = 0
    while True:
        params = {"page": page, "size": PAGE_SIZE}
        log.info(json.dumps({
            "event": "fetch_rooms_page",
            "page": page,
            "size": PAGE_SIZE,
            "url": GET_ROOMS_URL
        }))

        resp = http_get(GET_ROOMS_URL, params=params, headers=COMMON_HEADERS,
                        timeout=TIMEOUT_SECS, max_retries=MAX_RETRIES, backoff=RETRY_BACKOFF_SECS)

        try:
            payload = resp.json()
        except Exception as e:
            log.error(json.dumps({"event": "invalid_json", "page": page, "error": str(e)}))
            raise

        data = (payload or {}).get("response", {}).get("data", {})
        content = data.get("content", [])
        pagination = data.get("pagination", {})

        rooms.extend(content)

        last = bool(pagination.get("is_last", pagination.get("last", False)))
        log.info(json.dumps({
            "event": "page_summary",
            "page": pagination.get("page", page),
            "received": len(content),
            "accumulated": len(rooms),
            "last": last
        }))

        if last or not content:
            break
        page += 1

    log.info(json.dumps({"event": "fetch_rooms_done", "total_rooms": len(rooms)}))
    return rooms


def build_price_payload(room: Dict[str, Any], day_date: datetime) -> Dict[str, Any]:
    """Map room fields to the POST payload expected by /priceList."""
    return {
        "motel_id": room.get("motelId"),
        "motel_chain_id": room.get("motelChainId"),
        "motel_room_category_id": room.get("motelRoomCategoryId"),
        "room_type": DEFAULT_ROOM_TYPE,
        "available_room_number": DEFAULT_AVAILABLE,
        "date": iso_date(day_date),
        "status": DEFAULT_STATUS,
        "price": DEFAULT_PRICE,
        "booked_room_count": DEFAULT_BOOKED
    }


def post_prices_for_room(room: Dict[str, Any], start_date: datetime, days: int) -> None:
    room_id = room.get("roomId")
    for offset in range(days):
        d = start_date + timedelta(days=offset)
        payload = build_price_payload(room, d)

        log.info(json.dumps({
            "event": "post_price_attempt",
            "roomId": room_id,
            "date": payload["date"],
            "url": POST_PRICE_URL,
            "dry_run": DRY_RUN
        }))

        if DRY_RUN:
            continue

        resp = http_post(POST_PRICE_URL, json=payload, headers=COMMON_HEADERS,
                         timeout=TIMEOUT_SECS, max_retries=MAX_RETRIES, backoff=RETRY_BACKOFF_SECS)

        ok = 200 <= resp.status_code < 300
        body: Optional[str] = None
        try:
            body = json.dumps(resp.json())
        except Exception:
            body = resp.text[:500]

        log.log(
            logging.INFO if ok else logging.ERROR,
            json.dumps({
                "event": "post_price_result",
                "roomId": room_id,
                "date": payload["date"],
                "status_code": resp.status_code,
                "response": body
            })
        )


def main():
    start = datetime.now(timezone.utc)
    log.info(json.dumps({
        "event": "job_start",
        "start_time": start.isoformat(),
        "days_ahead": DAYS_AHEAD,
        "get_url": GET_ROOMS_URL,
        "post_url": POST_PRICE_URL
    }))

    rooms = fetch_all_rooms()
    for idx, room in enumerate(rooms, 1):
        log.info(json.dumps({"event": "room_start", "index": idx, "roomId": room.get("roomId")}))
        post_prices_for_room(room, start, DAYS_AHEAD)

    end = datetime.now(timezone.utc)
    log.info(json.dumps({
        "event": "job_done",
        "end_time": end.isoformat(),
        "duration_secs": (end - start).total_seconds(),
        "rooms_processed": len(rooms),
        "days_per_room": DAYS_AHEAD
    }))


if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        log.exception(json.dumps({"event": "job_exception", "error": str(e)}))
        sys.exit(1)
