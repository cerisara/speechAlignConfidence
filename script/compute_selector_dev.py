#!/usr/bin/python
#

from keras.models import Sequential, Model, model_from_json
from keras.layers.core import Dense, Dropout, Activation, Flatten, Merge
from keras.layers.recurrent import LSTM
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
sample_array_mfcc = []
sample_array_phon = []

for elt in dataSet:
    sample_array_mfcc.append(elt.getContextArray(mfccCol, flatten=False, phon=False))
    sample_array_phon.append(elt.getPhonArray())

sample_array_mfcc = np.asarray(sample_array_mfcc)
sample_array_phon = np.asarray(sample_array_phon)

prediction = model.predict([sample_array_mfcc, sample_array_phon])

output = csvWriter(sys.argv[4])
index_prediction = 0

for elt in dataSet:
    output.addLine((elt.infoRef[0], elt.good))
    output.addLine(prediction[index_prediction])
    index_prediction += 1
output.close()