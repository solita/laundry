#!/usr/bin/env bash

# This script can be used to export the built laundry conversion
# programs (gs, imagemagick, etc) from inside the laundrybuild
# container to a tar archive.

containerid="$1"
shift
outfilename="$1"
set -x
if [ -z "$outfilename" -o  -z "$containerid" -o "$outfilename" = "-h" ]; then
    echo usage: $0 container-id-or-name output-file-name
    exit 0
fi

set -euxo pipefail

if [ -f "$outfilename" ]; then
    echo NOTICE: Overwriting existing file.
fi

docker cp -a "$containerid":/opt/laundry - | gzip > "$outfilename"
