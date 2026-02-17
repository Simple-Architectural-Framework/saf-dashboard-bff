#!/bin/bash

echo "Navigating to /resources/static directory"
cd ../../resources/static/

echo "Cleaning up directory"
rm -rf *

echo "Navigating to main directory"
cd ../../vue/dashboard/

echo "Navigating to dist_prod directory"
cd config/vite_config/dist_prod/

echo "Cleaning up directory"
rm -rf *
echo "Going back to dashboard directory"
cd ../../../

echo "Running build"
pnpm run build-prod

echo "Going to dist_prod directory again"
cd config/vite_config/dist_prod/

echo "Copying the static files to the resources/static directory"
cp -rf * ../../../../../resources/static/

