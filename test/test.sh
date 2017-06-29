#!/bin/bash

PORT=9001
HOST=http://localhost:$PORT
LAUNDRYPID=
START=yes
NSJAIL=/usr/local/bin/nsjail

# temporary manul conversion to track down a failure at travis
programs/pdf2txt test/testcases/hypno.pdf /tmp/out.txt && cat /tmp/out.txt

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
   test -f target/laundry.jar || die "no laundry.jar"
   java -jar target/laundry.jar \
      --port $PORT \
      --checksum-command programs/checksum \
      --pdf2txt-command programs/pdf2txt \
      --pdf2png-command programs/pdf2png \
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

test_pdf2txt() {
   echo -n "Testing pdf2txt:"
   test -x $NSJAIL || { echo "no nsjail - skipping"; return 0; }
   echo -n " converting"
   curl -sf -F file=@test/testcases/hypno.pdf -X POST "$HOST/api/pdf/pdf2txt" > tmp/response.txt || die "conversion failed"
   echo -n ", checking"
   grep -q 'All glory to the hypnotoad\.' tmp/response.txt || die "no glory to the hypnotoad in tmp/response.txt"
   echo ", ok"
}

test_pdf2png() {
   echo -n "Testing pdf2png:"
   test -x $NSJAIL || { echo "no nsjail - skipping "; return 0; }
   echo -n " converting"
   curl -sf -F file=@test/testcases/hypno.pdf -X POST "$HOST/api/pdf/pdf-preview" > tmp/response.png || die "conversion failed"
   echo -n ", checking"
   file tmp/response.png | grep -q 'PNG' || die "tmp/response.png does not look like a png file"
   echo ", ok"
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
   #test_pdf2txt
   #test_pdf2png
   
   echo "Tests OK"
}

main $@

kill $LAUNDRYPID
wait
