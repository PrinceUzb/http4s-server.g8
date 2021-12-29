#!/bin/bash

source set_env.sh
sbt -mem 3000 "project server" ~reStart