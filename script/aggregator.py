#!/usr/bin/python
# Compute the activation for model 1

from keras.models import model_from_json
from utils.csvUtils import *
from data.mfccCollection import *
from model1 import model1
from model3 import model3

import numpy as np
import zipfile
import io


class aggregator:
    # Argument: mfccCollection (init with loadFromMfccFile), path to the .zip containing the structure and the weight of the dnn
    def __init__(self, mfccCol, configArchive, model1Conf, model3Conf):
        self.mfccCol = mfccCol

        #We need to open the zipfile containing the structure and the weight of the DNN
        self.zf = zipfile.ZipFile(configArchive)

        self.model = model_from_json(self.zf.read("config.json"))
        self.model.compile(loss='categorical_crossentropy', optimizer='adam')
        self.model.set_weights(np.load(io.BufferedReader(self.zf.open("weight.npy", mode='r'))))

        self.model1 = model1(mfccCol, model1Conf)
        self.model3 = model3(mfccCol, model3Conf)

    def loadMfccCol(self, newMfcc):
        self.mfccCol = newMfcc
        self.model1.loadMfccCol(newMfcc)
        self.model3.loadMfccCol(newMfcc)

    def generateData(self, model1Res, model3Res):
        if model1Res is None or model3Res is None:
            return None

        data = [model1Res[0]]
        for elt in model3Res:
            data.append(elt)

        return data

    def compute(self, time, left, right):
        model1Prediction = self.model1.compute(time, left, right)
        model3Prediction = self.model3.compute(time, left, right)

        data = self.generateData(model1Prediction, model3Prediction)
        if data is None:
            return None

        return self.model.predict(np.asarray([data]))[0]

    def computeBatch(self, time, left, right):
        model1Prediction = self.model1.computeBatch(time, left, right)
        model3Prediction = self.model3.computeBatch(time, left, right)

        if model1Prediction is None or model3Prediction is None:
            return None

        data = []
        for i in range(0, len(time)):
            temp = self.generateData(model1Prediction[i], model3Prediction[i])
            if temp is None:
                return None
            data.append(temp)

        return self.model.predict(np.asarray(data))

#mfccCol = MfccCollection()
#mfccCol.loadFromMfccFile("/home/gserrier/mfcc_gold/acc_del_07.mfcc", "/home/gserrier/normMin.npy", "/home/gserrier/normMax.npy")
#ag = aggregator(mfccCol, "aggregator.zip", "data_model1.zip", "data_model3.zip")
#print ag.compute(1.87, "on", "s")

