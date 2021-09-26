#!/bin/sh

#Expected parameters:
# $1 - name of webhook URL
# $2 - commit Message
# $3 - pipeline details

MESSAGE="\"<b>New changes on branch:</b> master.<br> <b>Message:</b><br> $2 <br> <b>Pipeline details:</b> $3 \""
curl -H 'Content-Type: application/json' -d "{\"text\": $MESSAGE }" "$1"

