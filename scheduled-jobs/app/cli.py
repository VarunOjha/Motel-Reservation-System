# app/cli.py
from __future__ import annotations
import os, sys, argparse
from app.base import get_job
import app.jobs  # noqa: F401  # triggers job registration

def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description="Run a registered job.")
    parser.add_argument("--job", default=os.getenv("JOB_NAME"),
                        help="Job name to run (or set env JOB_NAME).")
    args = parser.parse_args(argv)

    if not args.job:
        raise SystemExit("No job specified. Use --job <name> or set env JOB_NAME.")
    JobCls = get_job(args.job)
    return JobCls().run()

if __name__ == "__main__":
    sys.exit(main())
