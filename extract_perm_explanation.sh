#!/bin/bash

cat strings-ko.xml | sed -r -n ':s;/<string.*name=\"perm.*/{/<\/string>\s*$/!{N;bs;};/product=\"[twn]/d;s/<string.*name=\"([^\"]*)\"[^>]*>/<string name="\1">/p;}' | tee perminfo.xml

_exit 0
