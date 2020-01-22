#!/usr/bin/env bash

set -euxo pipefail

docker build -f Dockerfile.programs-runtime -t laundry-programs  .
docker build -t libreconv -f - < Dockerfile.libreoffice
