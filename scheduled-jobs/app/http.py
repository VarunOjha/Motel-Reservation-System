# app/http.py
from __future__ import annotations
from typing import Any, Dict, Optional, Callable
import time
import requests

def _retry(fn: Callable[[], requests.Response], max_retries: int, backoff: float) -> requests.Response:
    last_exc: Optional[Exception] = None
    for attempt in range(1, max_retries + 1):
        try:
            return fn()
        except requests.RequestException as e:
            last_exc = e
            if attempt == max_retries:
                break
            time.sleep(backoff * attempt)
    assert last_exc is not None
    raise last_exc

def http_get(url: str, params: Dict[str, Any], headers: Dict[str, str],
             timeout: float, max_retries: int, backoff: float) -> requests.Response:
    return _retry(lambda: requests.get(url, params=params, headers=headers, timeout=timeout),
                  max_retries, backoff)

def http_post(url: str, json: Dict[str, Any], headers: Dict[str, str],
              timeout: float, max_retries: int, backoff: float) -> requests.Response:
    return _retry(lambda: requests.post(url, json=json, headers=headers, timeout=timeout),
                  max_retries, backoff)
