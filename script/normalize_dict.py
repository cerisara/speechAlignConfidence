#!/usr/bin/python
# The goal of this script is to normalize the data
# Argument: MfccDictionnary(input) NormalizedDictionnary(output) minValues(output)

import csv
import sys
from sets import Set

import numpy as np

count=0
contextSize=0
frameSize=0
fileCodex = dict()
print "Start reading the file"

def printInFile(f, line):
    f.write(str(line[0]))
    for i in range(1, len(line)):
        f.write(";")
        f.write(str(line[i]))

class mfccContext:
    def __init__(self, h):
        self.data = []
        self.header = h

    def append(self, element):
        self.data.append(element)

    def getHeader(self):
        res = self.header[0]
        for i in range(1, len(self.header)):
            res += ";" + self.header[i]
        return res

    def generateString(self):
        res = self.getHeader()
        res += "\n" + str(self.data[0])
        for i in range(1, len(self.data)):
            res += ";" + str(self.data[i])
        return res

    def normalize(self, sampleMin, sampleMax):
        self.data = (self.data - sampleMin) / (sampleMax - sampleMin)

    def check(self):
        for elt in self.data:
            if elt < 0 or elt > 1:
                print "A data is badly formatted"

count = 0
#We scan the file once to get information about amount of sample and the min/max
with open(sys.argv[1], 'rb') as csvfile:
    spamreader = csv.reader(csvfile, delimiter=';', quotechar='|')
    minTotal = np.repeat(sys.float_info.max, 39)
    maxTotal = np.repeat(-sys.float_info.max, 39)

    while(True):
        try:
            headerContext = spamreader.next()
        except StopIteration:
            break
        if(len(headerContext) == 0):
            break

        count = count + 1

        frame = spamreader.next()
        for j in range(0, 39):
            elt = float(frame[j])

            if maxTotal[j] < elt:
                maxTotal[j] = elt
            if minTotal[j] > elt:
                minTotal[j] = elt

print "Load Successfull"
print "Found " + str(count) + " mfccFralmes"

output = open(sys.argv[2], 'w')

minFile = open(sys.argv[3], 'w')
maxFile = open(sys.argv[4], 'w')
np.save(minFile, minTotal)
np.save(maxFile, maxTotal)

#we reopen the file and we start once again
with open(sys.argv[1], 'rb') as csvfile:
    spamreader = csv.reader(csvfile, delimiter=';', quotechar='|')

    while(True):
        try:
            headerContext = spamreader.next()
        except StopIteration:
            break
        if(len(headerContext) == 0):
            break

        sample = mfccContext(headerContext)

        frame = spamreader.next()
        for j in range(0, 39):
            sample.append(float(frame[j]))

        sample.normalize(minTotal, maxTotal)
        sample.check()

        output.write(sample.generateString() + "\n")

output.close()

























