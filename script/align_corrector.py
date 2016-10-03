#!/usr/bin/python
# Simple corrector to test the effect of models

from utils.csvUtils import csvReader
from data.mfccCollection import *
from aggregator import *
from model1 import *
from model3 import *

import sys

class infoBorder:
    def __init__(self, line):
        self.line = line
        self.gold = float(self.line[1])
        self.jtrans = float(self.line[2])
        self.phons = (self.line[4], self.line[5])

    def getJtransTime(self):
        return self.jtrans

    def getGoldTime(self):
        return self.gold

    def getPhons(self):
        return self.phons

    def getFile(self):
        return self.line[0]

    def isEqual(self, info):
        if self.gold == info.gold and self.jtrans == info.jtrans and self.phons[0] == info.phons[0] and self.phons[1] == info.phons[1]:
            return True
        return False

def loadFile(filename):
    reader = csvReader(filename)
    elementDict = dict()
    fileList = list()

    while reader.hasNextLine():
        info = infoBorder(reader.getNextLine())
        if info.getFile() not in fileList:
            elementDict[info.getFile()] = list()
            fileList.append(info.getFile())
        l = elementDict[info.getFile()]
        alreadyIn = False
        for elt in l:
            if elt.isEqual(info):
                alreadyIn = True
                break

        if not alreadyIn:
            elementDict[info.getFile()].append(info)

    return (elementDict, fileList)


def isLimitGood(gold, limit, tolerance=0.02):
    if abs(gold - limit) < tolerance:
        return 1
    return 0


def foundPredictedLimit(element, m, jtransThreshold, correctionThreshold, step, maxDepth):
    #print("Jtrans : " + str(element.getJtransTime()), "Gold : " + str(element.getGoldTime()),
        #"Phons left : " + element.getPhons()[0], "Phons right : " + element.getPhons()[1])
    time = element.getJtransTime()
    phons = element.getPhons()
    resultTime = time
    p0 = m.compute(time, phons[0], phons[1])
    if p0 is None:
        return (time, True)
    p0 = p0[0]
    #print("Jtrans activation : " + str(p0))

    if p0 < jtransThreshold:  # We launch the recognition
        phonG = np.repeat(phons[0], maxDepth * 2)
        phonD = np.repeat(phons[1], maxDepth * 2)
        timeVector = []

        for i in range(1, maxDepth + 1):
            timeVector.append(round(time - (i * step), 2))
            timeVector.append(round(time + (i * step), 2))

        batchPrediction = m.computeBatch(timeVector, phonG, phonD)
        if batchPrediction is None:
            return (time, False)
        #print timeVector
        #print batchPrediction
        currentDepth = 0
        bestPrediction = p0
        while currentDepth < maxDepth:

            pm = batchPrediction[currentDepth * 2][0]
            pp = batchPrediction[currentDepth * 2 + 1][0]

            #print("Time", "%.2f" % (time - (currentDepth * step)), "%.2f" % (time + (currentDepth * step)))
            #print("Act ", "%.3f" % pm, "%.3f" % pp)

            if pm > correctionThreshold and bestPrediction < pm:  # If the minus step is valid
                if pm > pp:
                    resultTime = time - ((currentDepth + 1) * step)
                    bestPrediction = pm
                else:
                    resultTime = time + ((currentDepth + 1) * step)
                    bestPrediction = pp
            if pp > correctionThreshold and bestPrediction < pp:  # If the plus step is valid. Some case have already been handle
                if pm < pp:
                    resultTime = time + ((currentDepth + 1) * step)
                    bestPrediction = pp
            currentDepth += 1

    else:
        return (time, True)
    #print(resultTime)
    return (resultTime, False)


def getError(model, eltList, jtransThreshold=0.50, correctionThreshold=0.5, step=0.02, maxDepth=200):
    accStartError = 0.
    accCorrectedError = 0.
    startAccurancyAcc = 0.
    correctedAccurancyAcc = 0.
    countElement = 0
    countFoundGood = 0
    accConfidenceScore = 0
    for elt in eltList:
        correctedLimit, foundGood = foundPredictedLimit(elt, model, jtransThreshold, correctionThreshold, step, maxDepth)

        if foundGood:
            countFoundGood += 1
        #if correctedLimit == elt.getJtransTime():
            #continue
        countElement += 1

        if isLimitGood(elt.getGoldTime(), elt.getJtransTime()) == 1:
            if foundGood:
                accConfidenceScore += 1
        else:
            if not foundGood:
                accConfidenceScore += 1
        accStartError += abs(elt.getGoldTime() - elt.getJtransTime())
        accCorrectedError += abs(elt.getGoldTime() - correctedLimit)
        startAccurancyAcc += isLimitGood(elt.getGoldTime(), elt.getJtransTime())
        correctedAccurancyAcc += isLimitGood(elt.getGoldTime(), correctedLimit)
        #print(isLimitGood(elt.getGoldTime(), elt.getJtransTime()), isLimitGood(elt.getGoldTime(), correctedLimit))
        #print (accStartError / countElement, accCorrectedError / countElement, startAccurancyAcc / countElement, correctedAccurancyAcc / countElement,
        #countElement, len(eltList), countFoundGood, accConfidenceScore)
    return (accStartError / countElement, accCorrectedError / countElement, startAccurancyAcc / countElement, correctedAccurancyAcc / countElement,
    countElement, len(eltList), countFoundGood, accConfidenceScore, float(accConfidenceScore) / countElement)


# Start of the script
elementDict, fileList = loadFile(sys.argv[1])
m1 = aggregator(None, "/home/gserrier/deepalign/paper/aggregator.zip", "/home/gserrier/deepalign/paper/data_model1.zip", "/home/gserrier/deepalign/paper/data_model3.zip")
#m1 = model1(None, "data_model1.zip")
#m1 = model3(None, "data_model3_100.zip")
result = list()
allList = list()
for f in fileList:
    eltList = elementDict[f]
    print f

    mfccCol = MfccCollection()
    mfccCol.loadFromMfccFile(sys.argv[2] + f + ".mfcc", sys.argv[3], sys.argv[4])
    m1.loadMfccCol(mfccCol)

    error = getError(m1, eltList, correctionThreshold=float(sys.argv[5]), step=float(sys.argv[6]), maxDepth=int(sys.argv[7]))
    result.append(error)
    print error

accStartError = 0.
accEndError = 0.
accConfidence = 0.
accStartAccuracy = 0
accEndAccuracy = 0
for error in result:
    accStartError += error[0]
    accEndError += error[1]
    accStartAccuracy += error[2]
    accEndAccuracy += error[3]
    accConfidence += error[8]

print("RES", accStartError / float(len(fileList)), accEndError / float(len(fileList)), accStartAccuracy / float(len(fileList)), accEndAccuracy / float(len(fileList)),
    accConfidence / float(len(fileList)))
