#!/bin/bash

PORT=9001
HOST=http://localhost:$PORT
LAUNDRYPID=
START=yes

alivep() {
    curl -s $HOST/api/alive | grep -q yes
}

die() {
   echo "ERROR: $@"
   exit 1
}

wait_for() {
   echo -n "Waiting for $@: "
   while true
   do
      $@ && break
      echo -n "x"
      sleep 1
   done
   echo " ok"
}

want() {
   local QUERY=$1
   local VALUE=$2
   local MSG=$3
   echo -n "$MSG: "
   local RES=$(curl -s $HOST/$QUERY)
   test "$RES" = "$VALUE" && echo "ok" && return
   die "Received '$RES' instead of '$VALUE'"
}

start_laundry() {
   echo "Starting laundry"
   mkdir -p tmp || exit 1
   alivep && return 0
   java -jar target/laundry.jar \
      --port $PORT \
      --checksum-command programs/checksum \
      > laundry.log &
   LAUNDRYPID=$!
   wait_for alivep
}

test_checksum() {
   date > tmp/testdata.txt
   echo -n "Computing SHA256:"
   curl -sf -F file=@tmp/testdata.txt -X POST "$HOST/api/digest/sha256" > tmp/response || die "upload failed"
   RESPONSE=$(cat tmp/response)
   cat tmp/testdata.txt | sha256sum | grep -q "$RESPONSE" || die "bad result"
   echo " $RESPONSE ok"
}

usage() {
   echo "scripts/test.sh [-h|--help] [-s|--server $HOST]" 
}

parse_args() {
   while [ $# -gt 0 ]
   do
      case "$1"
      in
         ("-h" | "--help")
            usage
            return 1;;
         ("-s" | "--server")
            shift 1
            if [ $# -lt 1 ]; then die "-h needs an argument"; fi
            HOST=$1
            START=no
            REMOTE=yes
            echo "Using host '$HOST'";;
         (*)
            echo "What in the world is '$1'?"
            return 1;;
      esac;
      shift 1
   done
   return 0
}

main() {
   parse_args "$@" || return 1
   
   # Laundry startup and initial state logging
   start_laundry
   
   # Actual tests 
   test_checksum
   
   echo "Tests OK"
}

main $@

kill $LAUNDRYPID
wait
