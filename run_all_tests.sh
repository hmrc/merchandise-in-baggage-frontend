#!/usr/bin/env bash

sbt clean compile scalafmtAll scalastyleAll coverage Test/test coverageOff coverageReport dependencyUpdates
