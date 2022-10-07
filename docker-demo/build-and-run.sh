#!/usr/bin/env bash

set -e

usage() { echo "usage: $0 [-p port]" 1>&2; exit 1; }

port=8080
while getopts "p:" flag
do
    case "${flag}" in
        p) port=${OPTARG};;
        *) usage;;
    esac
done

# Prevent non-option arguments
shift $((OPTIND-1))
if [ "$1" ]; then
    usage
fi

echo Building docker images...
DIR="$( dirname -- "$( readlink -f -- "$0"; )"; )"
"$DIR"/../docker-build/build-all.sh

api_key="$(tr -dc 'a-zA-Z0-9' < /dev/urandom | head -c 32)"

echo Building laundry...
docker build -t laundry-demo "$DIR" --build-arg PORT="$port" --build-arg API_KEY="$api_key" --file "$DIR"/Dockerfile.laundry-demo

echo Running laundry...
id="$(docker run --name laundry-demo -d --rm -p "$port:$port" -v /var/run/docker.sock:/var/run/docker.sock --group-add "$(cut -d: -f3 < <(getent group docker))" -e LAUNDRY_DOCKER_RUNTIME=runc laundry-demo)"

ip="$(docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' "$id")"

# Docker installations on WSL (2) might behave differently
# shellcheck disable=2143
if [ "$(grep -i "microsoft" /proc/version)" ]; then
    echo 
    echo "Running on Windows Subsystem for Linux?"
    echo "Thus the ip $ip of laundry-demo might not be accessible and you'll need to connect via localhost"
    echo 
    ip="localhost"
fi

echo
echo "Using api key: $api_key"
echo "Demo server available at http://$ip:$port"
echo
echo "To stop and destroy the container please run: docker stop laundry-demo"
echo