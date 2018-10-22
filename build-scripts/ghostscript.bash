#!/usr/bin/env bash
set -e
set -x
set -u
buildroot=$PWD/build-data
echo "Using $buildroot for build data"
mkdir -p "$buildroot"
cd "$buildroot"

tarball_url=https://github.com/ArtifexSoftware/ghostpdl-downloads/releases/download/gs925/ghostscript-9.25.tar.gz


tarball_basename="${tarball_url##*/}"
mkdir -p downloads
cd downloads
test -f $tarball_basename || wget $tarball_url
cd -
export CC=clang
export CXX=clang++
export CFLAGS="-fsanitize=address -g -O2 -fPIE -fstack-protector-strong -Wformat -Werror=format-security"
export CPPFLAGS="-Wdate-time -D_FORTIFY_SOURCE=2"
export CXXFLAGS="$CFLAGS"
export "LDFLAGS=-Wl,-Bsymbolic-functions -fPIE -pie -Wl,-z,relro -Wl,-z,now -fsanitize=address"
ncpus=`getconf _NPROCESSORS_ONLN || echo 1`
mkdir -p src
tar -C src -zxf "downloads/$tarball_basename"
cd src/ghostscript-*
make clean || true
./configure --prefix=/opt/laundry --without-x --with-divers=FILES --disable-cups --disable-gtk --disable-dbus > configure.out 2>&1
make -j $ncpus
