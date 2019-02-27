#!/bin/bash

PORT=9001
HOST=http://localhost:$PORT
LAUNDRYPID=
START=yes
FIREJAIL=/usr/local/bin/firejail
TOOLS=/opt/laundry

alivep() {
    curl -s $HOST/alive | grep -q yes
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
   test -f target/uberjar/laundry.jar || die "no laundry.jar"
   java -jar target/uberjar/laundry.jar \
      --port $PORT \
      --tools $TOOLS \
      > laundry.log &
   LAUNDRYPID=$!
   wait_for alivep
}

test_checksum() {
   date > tmp/testdata.txt
   echo -n "Computing SHA256:"
   curl -sf -F file=@tmp/testdata.txt -X POST "$HOST/digest/sha256" > tmp/response || die "upload failed"
   RESPONSE=$(cat tmp/response)
   cat tmp/testdata.txt | sha256sum | grep -q "$RESPONSE" || die "bad result"
   echo " $RESPONSE ok"
}

test_pdf2txt() {
   echo -n "Testing pdf2txt:"
   test -x $FIREJAIL || { echo "no firejail - skipping"; return 0; }
   echo -n " converting"
   curl -sf -F file=@test/testcases/hypno.pdf -X POST "$HOST/pdf/pdf2txt" > tmp/response.txt || die "conversion failed"
   echo -n ", checking"
   grep -q 'All glory to the hypnotoad\.' tmp/response.txt || die "no glory to the hypnotoad in tmp/response.txt"
   echo ", ok"
}

test_pdf2jpeg() {
   echo -n "Testing pdf2jpeg:"
   test -x $FIREJAIL || { echo "no firejail - skipping "; return 0; }
   echo -n " converting"
   curl -sf -F file=@test/testcases/hypno.pdf -X POST "$HOST/pdf/pdf-preview" > tmp/response.jpeg || die "conversion failed"
   echo -n ", checking"
   file tmp/response.jpeg | grep -q 'JPEG' || die "tmp/response.jpeg does not look like a jpeg file"
   echo ", ok (response is jpeg)"
}

test_pdf2pdfa() {
   echo -n "Testing pdf2pdfa:"
   echo -n " converting"
   curl -sf -F file=@test/testcases/hypno.pdf -X POST "$HOST/pdf/pdf2pdfa" > tmp/response.pdf || die "conversion failed"
   echo -n ", checking"
   file tmp/response.pdf | grep -q 'PDF' || die "tmp/response.pdf does not look like a pdf file"
   echo ", ok (response is pdf)"
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
   
   ls -lR /opt
   test -d "$TOOLS/bin" || die "no tools at $TOOLS: `ls -ld $TOOLS/bin 2>&1`"

   # Laundry startup and initial state logging
   start_laundry
   
   # Actual tests 
   test_checksum
   test_pdf2txt
   test_pdf2jpeg
   test_pdf2pdfa
   
   echo "Tests OK"
}

main $@

kill $LAUNDRYPID
wait
