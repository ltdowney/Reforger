#!/bin/sh

/usr/bin/find . -not -path '.git' -type f \
  -exec /usr/bin/sed -i 's/ *$//' \{} \;  \
  -exec /usr/bin/sed -i 's/\r\n$/\n/' \{} \;

