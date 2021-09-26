#!/bin/sh

#Expected parameters:
# $1 - name of webhook URL
# $2 - pipeline URL
# $3 - commit branch

MESSAGE="\"Pipeline <b>Failed.</b><br> <b>Commit Branch:</b> $3 <br> <b>Pipeline details:</b> $2 \""
curl -H 'Content-Type: application/json' -d "{\"text\": $MESSAGE }" "$1"

