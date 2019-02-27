#!/usr/bin/env bash

# This script can be used to export the libreoffice (or other) docker image
# for deployment inside docker on a destination server.

# for loading there is no script,
# just do: zcat docker-libreconv-image.tar.gz | docker load

outfilename="$1"
set -x
if [ -z "$outfilename" -o "$outfilename" = "-h" ]; then
    echo usage: $0 output-file-name
    exit 0
fi

set -euxo pipefail

if [ -f "$outfilename" ]; then
    echo NOTICE: Overwriting existing file.
fi

docker save libreconv:latest | gzip > "$outfilename"
