echo "Running docker build"
docker build -t motel-jobs:local .

echo "Running motel jobs"
docker run --rm --env-file .env --network motel-shared-network motel-jobs:local

echo "Well, this worked baby!"