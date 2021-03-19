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

cat /dev/urandom | tr -dc 'a-zA-Z0-9' | head -c 32 >> api-key
echo "Using api key: $(cat api-key)"

echo Building laundry
docker build -t laundry --build-arg PORT=$port --file Dockerfile.laundry ./
rm api-key

echo Run laundry
docker run -d --rm -p 8080:8080 -v /var/run/docker.sock:/var/run/docker.sock --group-add "$(cut -d: -f3 < <(getent group docker))" -e LAUNDRY_DOCKER_RUNTIME=runc laundry
