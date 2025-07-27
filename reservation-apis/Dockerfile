# Use the correct Go version
FROM golang:1.24.5-alpine

# Install git, needed for many Go module dependencies
RUN apk add --no-cache git

# Set working directory
WORKDIR /app

# Copy go.mod and go.sum first
COPY go.mod go.sum ./

# Download dependencies (will fail fast if go.sum is invalid)
RUN go mod download

# Copy the entire source code
COPY . .

# Build the app
RUN go build -o main .

# Show the output binary explicitly (for debugging)
RUN ls -lh ./main

# Use absolute path to avoid "./main" issue
CMD ["/app/main"]
