#!/bin/sh

cd MfccHandler
ant jar
cd ../TextGridParser
ant jar
cd ../TextGridIntervalAligner
ant jar
