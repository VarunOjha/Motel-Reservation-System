import time
import requests
from typing import Dict, Any, Optional
from datetime import datetime

def env_bool(val: Optional[str]) -> bool:
    return str(val or "").strip().lower() in {"1", "true", "yes", "y", "on"}

def iso_date(d: datetime) -> str:
    return d.date().isoformat()

def _retry_loop(fn, max_retries: int, backoff: float):
    last_exc = None
    for attempt in range(1, max_retries + 1):
        try:
            return fn()
        except requests.RequestException as e:
            last_exc = e
            if attempt == max_retries:
                break
            time.sleep(backoff * attempt)
    raise last_exc

def http_get(url: str, params: Dict[str, Any], headers: Dict[str, str],
             timeout: float, max_retries: int, backoff: float) -> requests.Response:
    def do():
        return requests.get(url, params=params, headers=headers, timeout=timeout)
    return _retry_loop(do, max_retries, backoff)

def http_post(url: str, json: Dict[str, Any], headers: Dict[str, str],
              timeout: float, max_retries: int, backoff: float) -> requests.Response:
    def do():
        return requests.post(url, json=json, headers=headers, timeout=timeout)
    return _retry_loop(do, max_retries, backoff)
