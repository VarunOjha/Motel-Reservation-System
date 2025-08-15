# app/utils.py
from __future__ import annotations
from typing import Dict, Optional
import os, json, logging
from datetime import datetime, timezone

def setup_logging():
    logging.basicConfig(
        level=os.getenv("LOG_LEVEL", "INFO"),
        format="%(asctime)s %(levelname)s %(message)s",
    )
    return logging.getLogger("jobs")

def env_bool(v: Optional[str], default: bool = False) -> bool:
    if v is None:
        return default
    return v.strip().lower() in {"1", "true", "yes", "y", "on"}

def parse_headers(raw: str) -> Dict[str, str]:
    if not raw:
        return {}
    out: Dict[str, str] = {}
    for pair in raw.split(","):
        if ":" in pair:
            k, v = pair.split(":", 1)
            out[k.strip()] = v.strip()
    return out

def utc_now():
    return datetime.now(timezone.utc)

def jlog(logger, **kv):
    logger.info(json.dumps(kv))
