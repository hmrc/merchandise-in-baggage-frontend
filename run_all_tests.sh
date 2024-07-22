#!/usr/bin/env bash

sbt clean compile scalafmtAll coverage test it/test coverageOff coverageReport A11y/test dependencyUpdates
