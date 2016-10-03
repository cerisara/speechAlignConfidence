#!/usr/bin/python
# Compute the activation for model 3

from keras.models import model_from_json
from utils.csvUtils import *
from data.mfccCollection import *
from data.phon import Phon

import numpy as np
import zipfile
import io


class model3:
    # Argument: mfccCollection (init with loadFromMfccFile), path to the .zip containing the structure and the weight of the dnn
    def __init__(self, mfccCol, configArchive):
        self.mfccCol = mfccCol

        #We need to open the zipfile containing the structure and the weight of the DNN
        zf = zipfile.ZipFile(configArchive)

        self.model = model_from_json(zf.read("config.json"))
        self.model.compile(loss='categorical_crossentropy', optimizer='adam')

        self.model.set_weights(np.load(io.BufferedReader(zf.open("weight.npy", mode='r'))))

    def loadMfccCol(self, newMfcc):
        self.mfccCol = newMfcc

    def generateData(self, time, left, right):
        leftPhon = Phon(left)
        rightPhon = Phon(right)
        if not leftPhon.isValid() or not rightPhon.isValid():
            return (None, None)

        refFrameNumero = int(round(time * 100., 0))

        if not self.mfccCol.isFrameValid(refFrameNumero):
            return (None, None)

        mfcc = []
        for i in range(-5, 6):
            temp = []
            for elt in self.mfccCol.getMfccFrame(refFrameNumero + i):
                temp.append(elt)
            mfcc.append(temp)

        phon = []
        for elt in leftPhon.getPhonArray():
            phon.append(elt)

        for elt in rightPhon.getPhonArray():
            phon.append(elt)

        return (mfcc, phon)

    def compute(self, time, left, right):
        mfcc, phon = self.generateData(time, left, right)
        if mfcc is None:
            return None

        return self.model.predict([np.asarray([mfcc]), np.asarray([phon])])[0]

    def computeBatch(self, time, left, right):
        dataP = []
        dataM = []
        for i in range(0, len(time)):
            tempM, tempP = self.generateData(time[i], left[i], right[i])
            if tempM is None:
                return None
            dataM.append(tempM)
            dataP.append(tempP)
        return self.model.predict([np.asarray(dataM), np.asarray(dataP)])


#mfccCol = MfccCollection()
#mfccCol.loadFromMfccFile("/home/gserrier/mfcc_gold/acc_del_07.mfcc", "/home/gserrier/normMin.npy", "/home/gserrier/normMax.npy")
#m3 = model3(mfccCol, "data_model3.zip")
#print m3.compute(1.87, "on", "s")