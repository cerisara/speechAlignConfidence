#!/usr/bin/python
# The goal of this script is to split and normalize the data

import csv
import sys


def printInFile(f, line):
    f.write(str(line[0]))
    for i in range(1, len(line)):
        f.write(";")
        f.write(str(line[i]))
    f.write("\n")


class mfccMeta:
    def __init__(self, header):
        self.header = header

    def storeHashCode(self, hashCodeList):
        self.hashCodeList = hashCodeList

    def getGood(self):
        return int(self.header[len(self.header) - 1])

    def storePhonList(self, phonList):
        self.phonList = phonList

    def storeDistance(self, distance):
        self.distance = distance

    def storeRefInfo(self, info):
        self.infoRef = info

    def getRefHashcode(self):
        return int(self.infoRef[0])

    def dumpInFile(self, f):
        printInFile(f, self.header)
        printInFile(f, self.infoRef)
        printInFile(f, self.hashCodeList)
        printInFile(f, self.phonList)
        printInFile(f, self.distance)

fileCodex = dict()
count = 0

with open(sys.argv[1], 'rb') as csvfile:
    spamreader = csv.reader(csvfile, delimiter=';', quotechar='|')
    headerGlobal = spamreader.next()

    while(True):
        try:
            headerContext = spamreader.next()
        except StopIteration:
            break
        if(len(headerContext) == 0):
            break

        if(not headerContext[0] in fileCodex):
            print "New file " + headerContext[0]
            fileCodex[headerContext[0]] = list()

        sample = mfccMeta(headerContext)
        sample.storeRefInfo(spamreader.next())
        sample.storeHashCode(spamreader.next())
        sample.storePhonList(spamreader.next())
        sample.storeDistance(spamreader.next())

        count += 1
        fileCodex[headerContext[0]].append(sample)

print "Load " + str(count) + " meta."

training = open(sys.argv[2], 'w')
printInFile(training, headerGlobal)
validation = open(sys.argv[3], 'w')
printInFile(validation, headerGlobal)

validationSize = count * 0.2
validationWroteSize = 0

for key in fileCodex:
    if validationWroteSize < validationSize:  # we drop the content of the file in the validation set
        print key + " in validation " + str(len(fileCodex[key]))
        for sample in fileCodex[key]:
            sample.dumpInFile(validation)
        validationWroteSize = validationWroteSize + len(fileCodex[key])
    else:
        for sample in fileCodex[key]:
            sample.dumpInFile(training)
        print key + " in training " + str(len(fileCodex[key]))


training.close()
validation.close()


