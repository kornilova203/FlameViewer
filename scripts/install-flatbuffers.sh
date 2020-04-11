#!/usr/bin/env bash

git clone https://github.com/google/flatbuffers.git
cd flatbuffers
git checkout tags/v1.12.0
mkdir target
cd target
cmake .. -G "Unix Makefiles"
make
