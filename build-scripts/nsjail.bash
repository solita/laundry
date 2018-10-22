#!/usr/bin/env bash
set -e
set -x
set -u
buildroot=$PWD/build-data
echo "Using $buildroot for build data"
mkdir -p "$buildroot"
cd "$buildroot"


# can't use releases, because the kafel is a git subproject and it doesn't have releases

nsjail_version=2.7
mkdir -p src
cd src
test -d nsjail-$nsjail_version || {
  git clone --recurse-submodules --single-branch --branch $nsjail_version https://github.com/google/nsjail.git nsjail-$nsjail_version
  
}
cd -
cd src/nsjail-$nsjail_version
make clean || true
# todo: add kafel for bpf depends on bison & flex
make
