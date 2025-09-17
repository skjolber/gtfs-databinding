#!/bin/bash
set -e

if [ ! -f "src/test/resources/rb_norway-aggregated-gtfs.zip" ]; then
  echo "Transfer GTFS feed"
  curl -sS 'https://storage.googleapis.com/marduk-production/outbound/gtfs/rb_norway-aggregated-gtfs.zip' -o 'src/test/resources/rb_norway-aggregated-gtfs.zip'
fi