#!/usr/bin/env bash

chmod a+x checkincheck.sh

sbt test

cd ../merchandise-in-baggage || exit

printf "#####################\ncd to $PWD for contract verifier tests... \n#####################\n"

sbt "testOnly *VerifyContractSpec;"

cd ../merchandise-in-baggage-frontend || exit
