echo "Running docker build"
docker build -t motel-jobs:local .

echo "Running motel jobs"
docker run --rm --env-file .env motel-jobs:local

echo "Well, this worked baby!"