#!/usr/bin/python
# The goal of this script is to split and normalize the data

from keras.models import Sequential
from keras.layers.core import Dense, Dropout, Activation
from data.dataModelMfccPhon import dataModelMfccPhon
from data.mfccCollection import MfccCollection
from utils.csvUtils import csvReader
from utils.utils import *

import numpy as np
import sys
import os
import tempfile
import zipfile

# Loading traning set
trainingSet = list()
reader = csvReader(sys.argv[1])

# We get the header to grab some parameter
globalHeaderDict = analyzeGlobalHeader(reader.getNextLine())
contextSize = globalHeaderDict['leftContextSize'] + globalHeaderDict['rightContextSize'] + 1

dataLength = globalHeaderDict['frameSize'] * contextSize + globalHeaderDict['phonCount'] * globalHeaderDict['phonListLength'] + 1

while reader.hasNextLine():
    sample = dataModelMfccPhon()
    sample.loadFromCSV(reader)
    trainingSet.append(sample)

print "Got " + str(len(trainingSet)) + " samples for training (dimension : " + str(contextSize) + ")"

# Loading validation set
validationSet = list()
reader = csvReader(sys.argv[2])
reader.getNextLine()  # Add check if the file is the same
while reader.hasNextLine():
    sample = dataModelMfccPhon()
    sample.loadFromCSV(reader)
    validationSet.append(sample)

print "Got " + str(len(validationSet)) + " samples for validation (dimension : " + str(contextSize) + ")"

print "Load all mfcc frame"

mfccCol = MfccCollection()
mfccCol.loadFromMfccDict(sys.argv[3])

print "######################################"
print "Generate the model"

xValidation = []
yValidation = []
for elt in validationSet:
    xValidation.append(elt.getContextArray(mfccCol))
    yValidation.append(elt.getTarget())

xValidation = np.asarray(xValidation)
yValidation = np.asarray(yValidation)

posCount = 0
xSampleArray = []
ySampleArray = []
# We need to built data and goal
for elt in trainingSet:
    if elt.isGood():
        posCount += 1
    xSampleArray.append(elt.getContextArray(mfccCol))
    ySampleArray.append(elt.getTarget())
xSampleArray = np.asarray(xSampleArray)
ySampleArray = np.asarray(ySampleArray)

print posCount
print float(posCount) / float(len(trainingSet))

dropout = float(sys.argv[5])

model = Sequential()
model.add(Dense(250, input_dim=dataLength, activation='relu'))
model.add(Dropout(dropout))
model.add(Dense(150, activation='relu'))
model.add(Dropout(dropout))
model.add(Dense(100, activation='relu'))
model.add(Dropout(dropout))
model.add(Dense(75, activation='relu'))
model.add(Dropout(dropout))
model.add(Dense(50, activation='relu'))
model.add(Dropout(dropout))
model.add(Dense(25, activation='relu'))
model.add(Dropout(dropout))
model.add(Dense(2, activation='softmax'))

model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])

summary = generateSummary(model)
print summary

cumulativeAcc = []
cumulativeLoss = []
validationAcc = []
validationLoss = []

batch_size = 8192

print "######################################"
print "Starting training"


for i in range(0, int(sys.argv[4])):
    print("loop :", i)
    for j in range(0, 1):
        history = model.fit(xSampleArray, ySampleArray, nb_epoch=1, batch_size=batch_size, verbose=1, validation_split=0.0, shuffle=True)
        score = model.evaluate(xValidation, yValidation, batch_size=batch_size, verbose=1)
        print score
        cumulativeAcc.append(history.history['acc'][0])
        cumulativeLoss.append(history.history['loss'][0])
        validationAcc.append(score[1])
        validationLoss.append(score[0])


# We now store everything in an archive
zf = zipfile.ZipFile('data_model1.zip', 'w')

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