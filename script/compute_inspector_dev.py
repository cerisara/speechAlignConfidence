#!/usr/bin/python
# Compute the activation for model 1

from keras.models import Sequential, Model, model_from_json
from keras.layers.core import Dense, Dropout, Activation
from data.dataModelMfccPhon import dataModelMfccPhon
from data.mfccCollection import *
from utils.csvUtils import *

import numpy as np
import sys
import zipfile
import io


reader = csvReader(sys.argv[2])
header = reader.getNextLine()
contextSize = int(header[0]) + int(header[1]) + 1
frameSize = int(header[2])
phonCount = int(header[3])
phonListLength = int(header[4])

dataLength = frameSize * contextSize + phonCount * phonListLength + 1
dataSet = list()
while reader.hasNextLine():
    sample = dataModelMfccPhon()
    sample.loadFromCSV(reader)
    if not sample.isBegin():
        dataSet.append(sample)

print "Got " + str(len(dataSet)) + " samples"

mfccCol = MfccCollection()
mfccCol.loadFromMfccDict(sys.argv[3])

zf = zipfile.ZipFile(sys.argv[1])

model = model_from_json(zf.read("config.json"))
model.compile(loss='categorical_crossentropy', optimizer='adam')

model.set_weights(np.load(io.BufferedReader(zf.open("weight.npy", mode='r'))))

header_list = list()
sample_array = []

for elt in dataSet:
    sample_array.append(elt.getContextArray(mfccCol))

prediction = model.predict(np.asarray(sample_array))

output = csvWriter(sys.argv[4])
index_prediction = 0

for elt in dataSet:
    output.addLine((elt.infoRef[0], elt.good, prediction[index_prediction][0]))
    index_prediction += 1
output.close()
