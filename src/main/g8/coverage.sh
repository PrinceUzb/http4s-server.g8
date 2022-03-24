#!/bin/bash

sbt -mem 3000 clean reload coverage test coverageAggregate
