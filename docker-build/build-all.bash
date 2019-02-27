#!/usr/bin/env bash

set -euxo pipefail

docker build -t laundrybuild .
# todo: detect when existing laundrybuild container exists, and do docker rm laundrybuild
docker run --cap-add SYS_PTRACE -it -e LAUNDRY_BUILD_BRANCH= --name laundrybuild laundrybuild
bash export-opt.bash laundrybuild laundry-opt.tgz
docker build -f Dockerfile.programs-runtime -t laundry-programs  .
docker build -t libreconv -f - < Dockerfile.libreoffice 
