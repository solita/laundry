#!/usr/bin/env bash

set -e
set -u
set -x

function require {
    which "$1" 2>/dev/null && return
    echo "error: $1 not found in path" >&2
    exit 1
}

require lddtree
require strace
require mktemp

mytmp=`mktemp -d`
trap "rm -rf $mytmp" EXIT

strace -o $mytmp/strace.out -f -eopen "$@"
egrep -v 'ENOENT \([^\)]*\)$' $mytmp/strace.out |
    cut -d \" -f 2  |
    sort |
    uniq |
    egrep '^/(etc|lib|var|usr|bin)/' |
    while read fn
      do test -f "$fn" && echo "$fn"
    done > $mytmp/chroot-files

executable=`which $1`
mybin=$mytmp/bin
PATH=$PATH:$mybin
mkdir $mybin
cp ./build-data/src/nsjail-[123].*/nsjail $mybin/

require nsjail
echo $executable >> $mytmp/chroot-files
echo /usr/share/fonts >> $mytmp/chroot-files
lddtree -l $executable >> $mytmp/chroot-files
tar -C / --dereference --files-from $mytmp/chroot-files -cf $mytmp/chroot.tar
cp $mytmp/chroot.tar chroot.tar
