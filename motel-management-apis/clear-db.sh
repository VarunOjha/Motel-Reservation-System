#!/bin/bash
set -e

# Navigate to your project directory (optional, but good practice)
# cd /path/to/your/docker/project

echo "Docker Compose Down"

docker-compose down

echo "Remove the volumne"
docker volume rm $(docker volume ls -qf name=motel_pgdata)

# echo "Recreating the db & volumne"
# docker-compose up --build