#!/usr/bin/python
#

import csv


class csvReader:
    def __init__(self, filename):
        csvFile = open(filename, 'rb')
        self.spamreader = csv.reader(csvFile, delimiter=';', quotechar='|')
        self.nextLine = None
        self.getNextLine()  # We ignite the function

    def getNextLine(self):
        res = self.nextLine
        try:
            self.nextLine = self.spamreader.next()
        except StopIteration:
            self.nextLine = None
        return res

    def hasNextLine(self):
        return self.nextLine is not None


class csvWriter:
    def __init__(self, filename):
        self.csvFile = open(filename, 'w')

    def close(self):
        self.csvFile.close()

    def addLine(self, l):
        s = str(l[0])
        for i in range(1, len(l)):
            s += ";" + str(l[i])
        self.csvFile.write(s + "\n")