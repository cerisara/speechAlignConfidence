# -*- coding: utf-8 -*-
#!/usr/bin/python
# MfccCollection for models

import numpy as np
from utils.csvUtils import *


class MfccCollection:

    def __init__(self):
        self.loaded = False

    def loadFromMfccFile(self, mfccFile, minFile, maxFile):
        minFile = open(minFile, 'r')
        maxFile = open(maxFile, 'r')
        minTotal = np.load(minFile)
        maxTotal = np.load(maxFile)
        minFile.close()
        maxFile.close()

        self.mfccArray = []
        reader = csvReader(mfccFile)
        while reader.hasNextLine():
            sample = []
            for elt in reader.getNextLine():
                sample.append(float(elt))
            self.mfccArray.append((np.asarray(sample) - minTotal) / (maxTotal - minTotal))

        self.getMfccFrame = self.__getMfccFrameFile
        self.isFrameValid = self.__isMfccFrameGoodFile

        self.loaded = True

    def loadFromMfccDict(self, mfccDict):
        self.mfccDict = dict()
        reader = csvReader(mfccDict)
        while reader.hasNextLine():
            headerContext = reader.getNextLine()
            hashCode = int(headerContext[0])
            sample = []

            for elt in reader.getNextLine():
                sample.append(float(elt))

            self.mfccDict[hashCode] = sample
        self.getMfccFrame = self.__getMfccFrameDict
        self.isFrameValid = self.__isMfccFrameGoodDict

        self.loaded = True

    def __getMfccFrameDict(self, hashcode):
        return self.mfccDict[hashcode]

    def __getMfccFrameFile(self, referenceFrame):
        return self.mfccArray[referenceFrame]

    def getMfccFrame(self):
        raise Exception("Not loaded")

    def __isMfccFrameGoodFile(self, referenceFrame, leftSize=5, rightSize=5):
        if referenceFrame < leftSize or referenceFrame > len(self.mfccArray) - (rightSize + 1):
            return False
        else:
            return True

    def __isMfccFrameGoodDict(self, hashcode):
        return hashcode in self.mfccDict

    def isFrameValid(self):
        raise Exception("Not loaded")

#col = MfccCollection()
#col.loadFromMfccFile("/home/gserrier/mfcc_gold/acc_del_07.mfcc", "/home/gserrier/normMin.npy", "/home/gserrier/normMax.npy")

#col2 = MfccCollection()
#col2.loadFromMfccDict("/home/gserrier/sample_allWrong_dict_norm.fc")

