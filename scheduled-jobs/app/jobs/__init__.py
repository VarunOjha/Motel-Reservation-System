# app/jobs/__init__.py
# Import job modules here so their @register_job runs at import time.
from .price_publisher import PricePublisher  # noqa: F401
