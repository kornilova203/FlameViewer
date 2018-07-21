#!/usr/bin/env bash

mkdir dependencies
cd dependencies
git clone https://github.com/google/flatbuffers.git
cd flatbuffers
git checkout tags/v1.9.0
mkdir target
cd target
cmake .. -G "Unix Makefiles"
make
sudo mv flatc /usr/local/bin/
