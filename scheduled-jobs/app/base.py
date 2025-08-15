# app/base.py
from __future__ import annotations
from typing import Callable, Dict, Type

_JOB_REGISTRY: Dict[str, Type["Job"]] = {}

def register_job(name: str) -> Callable[[Type["Job"]], Type["Job"]]:
    """Decorator to register a job class under a name."""
    def decorator(cls: Type["Job"]) -> Type["Job"]:
        if name in _JOB_REGISTRY:
            raise ValueError(f"Job '{name}' is already registered")
        _JOB_REGISTRY[name] = cls
        cls.job_name = name
        return cls
    return decorator

def get_job(name: str) -> Type["Job"]:
    try:
        return _JOB_REGISTRY[name]
    except KeyError:
        raise SystemExit(f"Unknown job '{name}'. Available: {', '.join(sorted(_JOB_REGISTRY))}")

class Job:
    """Base class for all jobs."""
    job_name: str = "base"

    def run(self) -> int:
        """Return process exit code (0 = success). Implement in subclass."""
        raise NotImplementedError
