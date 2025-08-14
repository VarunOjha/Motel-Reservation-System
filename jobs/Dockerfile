FROM python:3.11-slim

WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY main.py utils.py ./

# Env defaults can be overridden by K8s ConfigMap/Secrets
ENV PYTHONUNBUFFERED=1

CMD ["python", "main.py"]
