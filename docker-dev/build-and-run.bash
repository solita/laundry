#!/usr/bin/env bash

port=8080
while getopts p: flag
do
    case "${flag}" in
        p) port=${OPTARG};;
    esac
done

echo Building libreconv
docker build -t libreconv https://github.com/solita/laundry.git#develop --file docker-build/Dockerfile.libreoffice

echo Building laundry programs
docker build -t laundry-programs https://github.com/solita/laundry.git#develop --file docker-build/Dockerfile.programs-runtime

api_key="$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | head -c 32)"

echo Building laundry
docker build -t laundry https://github.com/solita/laundry.git#develop --build-arg PORT=$port --build-arg API_KEY=$api_key --file docker-dev/Dockerfile.laundry-dev

echo Run laundry
echo "Using api key: $api_key"
docker run -d --rm -p $port:$port -v /var/run/docker.sock:/var/run/docker.sock --group-add "$(cut -d: -f3 < <(getent group docker))" -e LAUNDRY_DOCKER_RUNTIME=runc laundry
