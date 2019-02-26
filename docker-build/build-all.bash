#!/usr/bin/env bash

set -e
set -x
docker build -t laundrybuild .
# todo: detect when existing laundrybuild container exists, and do docker rm laundrybuild
docker run --cap-add SYS_PTRACE -it  --name laundrybuild laundrybuild
bash export-opt.bash laundrybuild laundry-opt.tgz
docker build -f Dockerfile.programs-runtime -t laundry-programs  .
