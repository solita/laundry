#!/bin/bash

export PATH="$PATH:/home/runner/work"

IFS='
'
OUTPUT=$(clj-kondo --lint src --config .clj-kondo/config.edn)

CLJ_KONDO_EXIT=$?

SHA=$(git rev-parse HEAD)

ANNOTATIONS=''
FIRST=1
FAILURES=0
WARNINGS=0

for output in $OUTPUT
do
    if [[ $output =~ ^([^:]+):([0-9]+):([0-9]+):\ ([^:]+):\ (.*)$ ]]; then
        FILE=${BASH_REMATCH[1]}
        ROW=${BASH_REMATCH[2]}
        #COL=${BASH_REMATCH[3]}
        TYPE=${BASH_REMATCH[4]}
        MSG=${BASH_REMATCH[5]}
        SEP=","
        if [ $FIRST -eq 1 ]; then
            SEP=""
            FIRST=0
        fi
        if [ "$TYPE" == "error" ]; then
            TYPE="failure"
            FAILURES=$((FAILURES + 1))
        fi
        if [ "$TYPE" == "warning" ]; then
            WARNINGS=$((WARNINGS + 1))
        fi

        # Github API allows at most 50 annotations
        if (( (FAILURES + WARNINGS) < 50 )); then
            ANNOTATIONS="$ANNOTATIONS$SEP{\"path\":\"$FILE\",\"start_line\":$ROW,\"end_line\":$ROW,\"annotation_level\":\"$TYPE\",\"message\":\"$MSG\"}"
        fi
    fi
done

BODY="{\"name\": \"lint\", \"head_sha\": \"$SHA\", \"output\": {\"title\":\"Lint output\",\"summary\": \"$FAILURES failures, $WARNINGS warnings\", \"annotations\": [$ANNOTATIONS]}}"


curl -H "Content-Type: application/json" \
     -H "Accept: application/vnd.github.antiope-preview+json" \
     -H "Authorization: Bearer $GITHUB_TOKEN" \
     -X POST \
     -d "$BODY" \
     "https://api.github.com/repos/$GITHUB_REPOSITORY/check-runs"


if [ $CLJ_KONDO_EXIT -eq 2 ]; then
    exit 0;
fi

exit $CLJ_KONDO_EXIT
