#!/bin/sh

/usr/bin/git rev-list --abbrev-commit HEAD | /usr/bin/wc -l | /usr/bin/awk '{print $1}'

