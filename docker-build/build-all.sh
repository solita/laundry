#!/usr/bin/env bash

set -eux

# Find out the directory where this script is located
DIR="$( dirname -- "$( readlink -f -- "$0"; )"; )"

docker build -t laundry-programs - < "$DIR"/Dockerfile.laundry-programs
