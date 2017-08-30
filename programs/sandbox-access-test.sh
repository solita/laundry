#!/bin/bash

# Some sandbox status tests

echo "Sandbox access tests:"

TESTIP=8.8.8.8
TESTHOST=haltp.org
MAXDEPTH=4

/bin/ping -c 1 $TESTIP &>/dev/null && echo "FAIL: $TESTIP can be pinged"

/bin/ping -c 1 $TESTHOST 2>&1 | grep -q 136.243.33.30  && echo "FAIL: DNS works"

curl $TESTHOST &>/dev/null && echo "FAIL: HTTP requests can be sent"

echo "Writeable files and directories up to depth $MAXDEPTH"
find / -maxdepth $MAXDEPTH -writable

echo

echo "Readable files and directories up to depth $MAXDEPTH"
find / -maxdepth $MAXDEPTH -readable

echo

echo "/proc:"
ls /proc

