#!/bin/bash

# show versions
echo "yarn" $(yarn --version)
echo "npm" $(npm --version)
echo "protractor" $(protractor --version)
google-chrome --version
java -version
curl --version

# start selenium server in background
echo "Starting selenium server"
webdriver-manager update
webdriver-manager start &

# small delay
sleep 5

# Download all dependencies
yarn

# run test
echo "Running tests"
protractor --troubleshoot true --baseUrl='http://localhost:8080' src/test/javascript/conf.js

# Have it quit correctly
RESULT=$?
echo "Done running test: ${RESULT}"
exit ${RESULT}