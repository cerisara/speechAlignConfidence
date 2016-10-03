#!/usr/bin/python
# This file contains various functin use accross deep align python part

from cStringIO import StringIO
import datetime
import sys


def analyzeGlobalHeader(line):
    res = dict()
    res['leftContextSize'] = int(line[0])
    res['rightContextSize'] = int(line[1])
    res['frameSize'] = int(line[2])
    res['phonCount'] = int(line[3])
    res['phonListLength'] = int(line[4])
    return res


def generateSummary(model):
    old_stdout = sys.stdout
    sys.stdout = mystdout = StringIO()

    model.summary()
    sys.stdout = old_stdout
    summary = datetime.datetime.now().strftime("%Y-%m-%d %H:%M") + "\n"
    summary += mystdout.getvalue()

    return summary