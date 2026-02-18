#!/bin/bash

echo "Going to dist_prod directory again"
cd config/vite_config/dist_prod/

echo "Copying the static files to the resources/static directory"
cp -rf * ../../../../../resources/static/