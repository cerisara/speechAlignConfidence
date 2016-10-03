#!/usr/bin/python
# The goal of this script is to split and normalize the data
# !WARNING! This code needs a lot of RAM to be executed

from keras.models import Sequential
from keras.layers.core import Dense, Dropout, Activation, Flatten, Merge
from keras.layers.recurrent import LSTM
from data.dataModelMfccPhon import dataModelMfccPhon
from data.mfccCollection import MfccCollection
from utils.csvUtils import csvReader
from utils.utils import *

import numpy as np
import sys
import os
import tempfile
import zipfile


frameSize = 0
contextSize = 0

#target = [[0.9, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],
    #[0.1, 0.8, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],
    #[0.0, 0.1, 0.8, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],
    #[0.0, 0.0, 0.1, 0.8, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],
    #[0.0, 0.0, 0.0, 0.1, 0.8, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0],
    #[0.0, 0.0, 0.0, 0.0, 0.1, 0.8, 0.1, 0.0, 0.0, 0.0, 0.0],
    #[0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.8, 0.1, 0.0, 0.0, 0.0],
    #[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.8, 0.1, 0.0, 0.0],
    #[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.8, 0.1, 0.0],
    #[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.8, 0.1],
    #[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.9]]

#target = [[1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],
    #[0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],
    #[0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],
    #[0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],
    #[0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],
    #[0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0],
    #[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0],
    #[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0],
    #[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0],
    #[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0],
    #[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0]]

# Loading traning set
trainingSet = list()
reader = csvReader(sys.argv[1])

globalHeaderDict = analyzeGlobalHeader(reader.getNextLine())
contextSize = globalHeaderDict['leftContextSize'] + globalHeaderDict['rightContextSize'] + 1

while reader.hasNextLine():
    sample = dataModelMfccPhon()
    sample.loadFromCSV(reader)
    trainingSet.append(sample)

print "Got " + str(len(trainingSet)) + " samples for training"

# Loading validation set
validationSet = list()
reader = csvReader(sys.argv[2])
reader.getNextLine()  # Add check if the file is the same
while reader.hasNextLine():
    sample = dataModelMfccPhon()
    sample.loadFromCSV(reader)
    validationSet.append(sample)

print "Got " + str(len(validationSet)) + " samples for validation"

print "Load all mfcc frame"

mfccCol = MfccCollection()
mfccCol.loadFromMfccDict(sys.argv[3])

print "######################################"
print "Generate the model"

dropout = float(sys.argv[5])
modlstm = Sequential()
modlstm.add(LSTM(50, return_sequences=True, input_shape=(contextSize, globalHeaderDict['frameSize']), activation='tanh'))
modlstm.add(LSTM(50, return_sequences=False, go_backwards=True, activation='tanh'))
#modlstm.add(Flatten())

modph = Sequential()
modph.add(Dense(66, input_shape=(globalHeaderDict['phonCount'] * globalHeaderDict['phonListLength'],)))
modph.add(Activation('linear'))

model = Sequential()
model.add(Merge([modlstm, modph], mode='concat'))
model.add(Dense(250, activation='relu'))
model.add(Dropout(dropout))
model.add(Dense(175, activation='relu'))
model.add(Dropout(dropout))
model.add(Dense(100, activation='relu'))
model.add(Dropout(dropout))
model.add(Dense(50, activation='relu'))
model.add(Dropout(dropout))
model.add(Dense(12, activation='softmax'))
model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])

summary = generateSummary(model)
print summary

batch_size = 8192

print "######################################"
print "Starting training"

cumulativeAcc = []
cumulativeLoss = []
validationAcc = []
validationLoss = []

xValidationPhon = []
xValidationMfcc = []
yValidation = []
for elt in validationSet:
    xValidationMfcc.append(elt.getContextArray(mfccCol, flatten=False, phon=False))
    xValidationPhon.append(elt.getPhonArray())
    yValidation.append(elt.getTarget(binary=False))

xValidationMfcc = np.asarray(xValidationMfcc)
xValidationPhon = np.asarray(xValidationPhon)
yValidation = np.asarray(yValidation)

posCount = 0
xSampleArrayMfcc = []
xSampleArrayPhon = []
ySampleArray = []
# We need to built data and goal
for elt in trainingSet:
    if elt.isGood():
        posCount += 1
    xSampleArrayMfcc.append(elt.getContextArray(mfccCol, flatten=False, phon=False))
    xSampleArrayPhon.append(elt.getPhonArray())
    ySampleArray.append(elt.getTarget(binary=False))
xSampleArrayMfcc = np.asarray(xSampleArrayMfcc)
xSampleArrayPhon = np.asarray(xSampleArrayPhon)
ySampleArray = np.asarray(ySampleArray)


for i in range(0, int(sys.argv[4])):
    history = model.fit([xSampleArrayMfcc, xSampleArrayPhon], ySampleArray, nb_epoch=1, batch_size=batch_size, verbose=1, validation_split=0.0, shuffle=True)
    print history.history['acc'][0]
    score = model.evaluate([xValidationMfcc, xValidationPhon], yValidation, batch_size=batch_size, verbose=0)
    print model.predict([xSampleArrayMfcc[:1], xSampleArrayPhon[:1]])
    print ySampleArray[:1]
    cumulativeAcc.append(history.history['acc'][0])
    cumulativeLoss.append(history.history['loss'][0])
    validationAcc.append(score[1])
    validationLoss.append(score[0])

# We now store everything in an archive
zf = zipfile.ZipFile('data_model3.zip', 'w')

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






