#!/usr/bin/env bash
set -e
set -x
set -u
buildroot=$PWD/build-data
echo "Using $buildroot for build data"
mkdir -p "$buildroot"
cd "$buildroot"




tarball_url=https://github.com/google/nsjail/archive/1.3.tar.gz

tarball_basename=nsjail-"${tarball_url##*/}"
mkdir -p downloads
cd downloads
test -f $tarball_basename || wget -O $tarball_basename $tarball_url
cd -
mkdir -p src
tar -C src -zxf "downloads/$tarball_basename"
cd src/nsjail-*
make clean || true
# todo: add kafel for bpf depends on bison & flex
make
