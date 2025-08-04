FROM rust:latest as builder

WORKDIR /usr/src/app

# Install required dependencies and target
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    build-essential \
    pkg-config \
    libssl-dev && \
    rm -rf /var/lib/apt/lists/* && \
    rustup update && \
    rustup target add x86_64-unknown-linux-gnu

# Copy source code
COPY . .

# Build release version
RUN cargo build --release

# Runtime
FROM ubuntu:22.04

WORKDIR /app

# Add CA certs for HTTP client (if needed)
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Create config directory
RUN mkdir -p /app/config

# Copy binary and config
COPY --from=builder /usr/src/app/target/release/motel-api-gateway /usr/local/bin/motel-api-gateway
COPY --from=builder /usr/src/app/config/routes.yaml /app/routes.yaml

EXPOSE 8080

CMD ["motel-api-gateway"]
