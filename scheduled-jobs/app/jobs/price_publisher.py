# app/jobs/price_publisher.py
from __future__ import annotations
from typing import Any, Dict, List, Optional
import os, json, logging
from datetime import timedelta

from app.base import Job, register_job
from app.http import http_get, http_post
from app.utils import setup_logging, env_bool, parse_headers, utc_now, jlog

@register_job("price_publisher")
class PricePublisher(Job):
    """
    Fetch all rooms (paged) from GET endpoint.
    For each room, POST price for N days starting today.
    """

    def __init__(self) -> None:
        self.log = setup_logging()

        # Config (env-driven)
        self.get_rooms_url = os.getenv("GET_ROOMS_URL", "http://motel-api.motel.svc.cluster.local:8085/motelApi/v1/motelRooms")
        self.post_price_url = os.getenv("POST_PRICE_URL", "http://reservation-api.reservation.svc.cluster.local:8086/reservationApi/v1/priceList")

        self.page_size = int(os.getenv("PAGE_SIZE", "50"))
        self.days_ahead = int(os.getenv("DAYS_AHEAD", "10"))

        self.default_room_type = os.getenv("DEFAULT_ROOM_TYPE", "Deluxe Suite")
        self.default_price = os.getenv("DEFAULT_PRICE", "199.99")
        self.default_available = os.getenv("DEFAULT_AVAILABLE", "12")
        self.default_booked = os.getenv("DEFAULT_BOOKED", "0")
        self.default_status = os.getenv("DEFAULT_STATUS", "Active")

        self.timeout = float(os.getenv("HTTP_TIMEOUT_SECS", "10"))
        self.max_retries = int(os.getenv("HTTP_MAX_RETRIES", "3"))
        self.backoff = float(os.getenv("HTTP_RETRY_BACKOFF_SECS", "1.5"))
        self.headers = parse_headers(os.getenv("EXTRA_HEADERS", ""))

        self.dry_run = env_bool(os.getenv("DRY_RUN", "false"))

    def run(self) -> int:
        start = utc_now()
        jlog(self.log, event="job_start", job=self.job_name, start=start.isoformat())

        rooms = self._fetch_all_rooms()
        for idx, room in enumerate(rooms, 1):
            jlog(self.log, event="room_begin", index=idx, roomId=room.get("roomId"))
            self._post_prices_for_room(room, start)

        end = utc_now()
        jlog(self.log, event="job_done", job=self.job_name, rooms=len(rooms),
             days=self.days_ahead, duration_secs=(end - start).total_seconds())
        return 0

    # ----- internals -----

    def _fetch_all_rooms(self) -> List[Dict[str, Any]]:
        rooms: List[Dict[str, Any]] = []
        page = 0
        while True:
            params = {"page": page, "size": self.page_size}
            jlog(self.log, event="fetch_rooms_page", page=page, size=self.page_size, url=self.get_rooms_url)

            resp = http_get(self.get_rooms_url, params=params, headers=self.headers,
                            timeout=self.timeout, max_retries=self.max_retries, backoff=self.backoff)

            payload = resp.json()
            data = (payload or {}).get("response", {}).get("data", {})
            content = data.get("content", [])
            pagination = data.get("pagination", {})

            rooms.extend(content)
            is_last = bool(pagination.get("is_last", pagination.get("last", False)))
            jlog(self.log, event="page_summary", page=pagination.get("page", page),
                 received=len(content), accumulated=len(rooms), last=is_last)

            if is_last or not content:
                break
            page += 1
        return rooms

    def _build_payload(self, room: Dict[str, Any], date_iso: str) -> Dict[str, Any]:
        return {
            "motel_id": room.get("motelId"),
            "motel_chain_id": room.get("motelChainId"),
            "motel_room_category_id": room.get("motelRoomCategoryId"),
            "room_type": self.default_room_type,
            "available_room_number": self.default_available,
            "date": date_iso,
            "status": self.default_status,
            "price": self.default_price,
            "booked_room_count": self.default_booked
        }

    def _post_prices_for_room(self, room: Dict[str, Any], start):
        room_id = room.get("roomId")
        for offset in range(self.days_ahead):
            d = start + timedelta(days=offset)
            date_iso = d.date().isoformat()
            payload = self._build_payload(room, date_iso)

            jlog(self.log, event="post_attempt", roomId=room_id, date=date_iso, dry_run=self.dry_run)

            if self.dry_run:
                continue

            resp = http_post(self.post_price_url, json=payload, headers=self.headers,
                             timeout=self.timeout, max_retries=self.max_retries, backoff=self.backoff)

            ok = 200 <= resp.status_code < 300
            try:
                body = resp.json()
            except Exception:
                body = resp.text[:500]

            level = logging.INFO if ok else logging.ERROR
            self.log.log(level, json.dumps({
                "event": "post_result",
                "roomId": room_id,
                "date": date_iso,
                "status_code": resp.status_code,
                "response": body
            }))
