#!/usr/bin/python
# Compute the activation for model 1

from keras.models import model_from_json
from utils.csvUtils import *
from data.mfccCollection import *
from data.phon import Phon

import numpy as np
import zipfile
import io


class model1:
    # Argument: mfccCollection (init with loadFromMfccFile), path to the .zip containing the structure and the weight of the dnn
    def __init__(self, mfccCol, configArchive):
        self.mfccCol = mfccCol

        #We need to open the zipfile containing the structure and the weight of the DNN
        self.zf = zipfile.ZipFile(configArchive)

        self.model = model_from_json(self.zf.read("config.json"))
        self.model.compile(loss='categorical_crossentropy', optimizer='adam')
        self.model.set_weights(np.load(io.BufferedReader(self.zf.open("weight.npy", mode='r'))))

        print "Load model1 complete"

    def loadMfccCol(self, newMfcc):
        self.mfccCol = newMfcc

    def generateData(self, time, left, right):
        leftPhon = Phon(left)
        rightPhon = Phon(right)
        if not leftPhon.isValid() or not rightPhon.isValid():
            return None

        refFrameNumero = int(round(time * 100., 0))

        if not self.mfccCol.isFrameValid(refFrameNumero):
            return None

        data = []
        for i in range(-5, 6):
            for elt in self.mfccCol.getMfccFrame(refFrameNumero + i):
                data.append(elt)

        for elt in leftPhon.getPhonArray():
            data.append(elt)

        for elt in rightPhon.getPhonArray():
            data.append(elt)

        data.append(0.0)
        return data

    def compute(self, time, left, right):
        data = self.generateData(time, left, right)
        if data is None:
            return None

        return self.model.predict(np.asarray([data]))[0]

    def computeBatch(self, time, left, right):
        data = []
        for i in range(0, len(time)):
            temp = self.generateData(time[i], left[i], right[i])
            if temp is None:
                return None
            data.append(temp)
        return self.model.predict(np.asarray(data))

#mfccCol = MfccCollection()
#mfccCol.loadFromMfccFile("/home/gserrier/mfcc_gold/acc_del_07.mfcc", "/home/gserrier/normMin.npy", "/home/gserrier/normMax.npy")
#m1 = model1(mfccCol, "data_model1.zip")
#print m1.compute(1.87, "on", "s")