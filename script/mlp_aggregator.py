#!/usr/bin/python
# The goal of this script is to split and normalize the data

#from keras.models import Sequential
#from keras.layers.core import Dense, Dropout, Activation, Flatten, Merge
#from keras.layers.recurrent import LSTM
#from keras.callbacks import Callback

from keras.models import Sequential
from keras.layers.core import Dense, Dropout, Activation
from utils.csvUtils import csvReader
from utils.utils import *

import numpy as np
import sys
import os
import tempfile
import zipfile


class intervalInfo:
    def __init__(self, hashcode, frameInfo):
        self.hashcode = hashcode
        self.frameInfo = frameInfo
        self.good = (self.frameInfo >= 4 and self.frameInfo <= 6)

    def getData(self):
        #print model1Dict[self.hashcode]
        #print self.good
        res = [model1Dict[self.hashcode][self.frameInfo]]
        #res = [0.0]

        for elt in model3Dict[self.hashcode][self.frameInfo]:
            res.append(float(elt))
        return res

    def getTarget(self):
        if self.good:
            return [1.0, 0.0]
        else:
            return [0.0, 1.0]


model1Dict = dict()
reader = csvReader(sys.argv[1])
while reader.hasNextLine():
    line = reader.getNextLine()
    hashcode = int(line[0])
    if not hashcode in model1Dict:
        model1Dict[hashcode] = dict()

    model1Dict[hashcode][int(line[1])] = float(line[2])

model3Dict = dict()
reader = csvReader(sys.argv[2])
while reader.hasNextLine():
    line = reader.getNextLine()
    ar = []
    for elt in reader.getNextLine():
        ar.append(float(elt))

    hashcode = int(line[0])
    if not hashcode in model3Dict:
        model3Dict[hashcode] = dict()

    model3Dict[hashcode][int(line[1])] = ar

trainList = list()
reader = csvReader(sys.argv[3])
reader.getNextLine()  # We remove the header
while reader.hasNextLine():
    header = reader.getNextLine()
    infoRef = reader.getNextLine()

    hashcode = int(infoRef[0])
    frameInfo = int(header[6])
    if infoRef[1] == "end":
        if hashcode in model1Dict:
            if hashcode in model3Dict:
                trainList.append(intervalInfo(hashcode, frameInfo))
            else:
                print "not 3"
        else:
            print "not 1"

    for _ in range(0, 3):  # We need to skip the three next line
        reader.getNextLine()

validationList = list()
reader = csvReader(sys.argv[4])
reader.getNextLine()  # We remove the header
while reader.hasNextLine():
    header = reader.getNextLine()
    infoRef = reader.getNextLine()

    hashcode = int(infoRef[0])
    frameInfo = int(header[6])
    if infoRef[1] == "end":
        if hashcode in model1Dict:
            if hashcode in model3Dict:
                validationList.append(intervalInfo(hashcode, frameInfo))
            else:
                print "not 3"
        else:
            print "not 1"

    for _ in range(0, 3):  # We need to skip the three next line
        reader.getNextLine()

turn = 150

posCount = 0
xTrainArray = []
yTrainArray = []
for info in trainList:
    if info.good:
        posCount += 1
    xTrainArray.append(info.getData())
    yTrainArray.append(info.getTarget())

print posCount

xTrainArray = np.asarray(xTrainArray)
yTrainArray = np.asarray(yTrainArray)

xDevArray = []
yDevArray = []
for info in validationList:
    xDevArray.append(info.getData())
    yDevArray.append(info.getTarget())

xDevArray = np.asarray(xDevArray)
yDevArray = np.asarray(yDevArray)

#print xTrainArray[0]
#print yTrainArray[0]
#print elementList[0].hashcode


model = Sequential()
model.add(Dense(20, input_dim=12))
model.add(Activation('relu'))
#model.add(Dropout(0.2))
model.add(Dense(16))
model.add(Activation('relu'))
model.add(Dense(12))
model.add(Activation('relu'))
model.add(Dense(9))
model.add(Activation('relu'))
model.add(Dense(5))
model.add(Activation('relu'))
model.add(Dense(3))
model.add(Activation('relu'))
model.add(Dense(2))
model.add(Activation('softmax'))

wtmp = model.get_weights()
print("model nparms", len(wtmp), sum([len(np.array(x).flatten()) for x in wtmp]))

model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])

summary = generateSummary(model)
print summary

cumulativeAcc = []
cumulativeLoss = []
validationAcc = []
validationLoss = []

batch_size = 256
for _ in range(0, turn):
    history = model.fit(xTrainArray, yTrainArray, nb_epoch=1, batch_size=batch_size, verbose=1, validation_split=0, shuffle=True)
    score = model.evaluate(xDevArray, yDevArray, batch_size=batch_size, verbose=0)
    cumulativeAcc.append(history.history['acc'][0])
    cumulativeLoss.append(history.history['loss'][0])
    validationAcc.append(score[1])
    validationLoss.append(score[0])

#print history.history
# We now store everything in an archive
zf = zipfile.ZipFile('aggregator.zip', 'w')

f = tempfile.NamedTemporaryFile(delete=False)
path = f.name
np.save(f, model.get_weights())
f.close()

zf.write(path, arcname='weight.npy')
zf.writestr('result_raw_train.txt', str(cumulativeAcc))
zf.writestr('loss_train.txt', str(cumulativeLoss))
zf.writestr('result_raw_validation.txt', str(validationAcc))
zf.writestr('loss_validation.txt', str(validationLoss))
zf.writestr('summary.txt', summary)

json = model.to_json()
zf.writestr("config.json", json)

zf.close()
os.remove(path)
