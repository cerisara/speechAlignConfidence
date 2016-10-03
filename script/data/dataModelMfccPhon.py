#!/usr/bin/python
# Compute the activation for model 1


class dataModelMfccPhon:
    def __init__(self):
        self.good = -1

    def loadFromCSV(self, csvReader):
        self.loadHeader(csvReader.getNextLine())
        self.loadInfoRef(csvReader.getNextLine())
        self.loadHashCode(csvReader.getNextLine())
        self.loadPhon(csvReader.getNextLine())
        self.loadDistance(csvReader.getNextLine())

    def loadHeader(self, header):
        self.header = header
        self.good = int(header[6])

    def loadHashCode(self, haschodeList):
        self.hashArray = []
        for elt in haschodeList:
            self.hashArray.append(int(elt))

    def loadPhon(self, phonList):
        self.leftPhon = int(phonList[0])
        self.rightPhon = int(phonList[1])

    def loadDistance(self, distance):
        self.distance = float(distance[0])

    def loadInfoRef(self, infoRef):
        self.infoRef = infoRef

    # If flatten is true we don't consider conv2D argument
    def getContextArray(self, mfccCol, flatten=True, phon=True, conv2D=False):
        res = []
        if flatten:
            for i in range(0, len(self.hashArray)):
                for elt in mfccCol.getMfccFrame(self.hashArray[i]):  # We make a copy to avoid to mess up things
                    res.append(elt)
            if phon:
                for i in range(0, 33):  # TODO: Avoid any hard reference
                    if i == self.leftPhon:
                        res.append(1.0)
                    else:
                        res.append(0.0)
                for i in range(0, 33):  # TODO: Avoid any hard reference
                    if i == self.rightPhon:
                        res.append(1.0)
                    else:
                        res.append(0.0)
                res.append(0.0)
        else:
            if not conv2D:
                for i in range(0, len(self.hashArray)):
                    res.append(self.getMfccArrayForIndex(i, mfccCol))
            else:
                for i in range(0, 13):
                    res.append([])
                for i in range(0, len(self.hashArray)):
                    res.append([self.getMfccArrayForIndex(i, mfccCol, staticOnly=True)])
            if phon:
                p = self.getPhonArray()
                res = (res, p)
        return res

    def getMfccArrayForIndex(self, index, mfccCol, staticOnly=False):
        res = []
        mfccCoeffArr = mfccCol.getMfccFrame(self.hashArray[index])
        size = len(mfccCoeffArr)
        if staticOnly:
            size = size / 3
        for i in range(0, size):
            res.append(mfccCoeffArr[i])
        return res

    def getPhonArray(self):
        res = []
        for i in range(0, 33):  # TODO: Avoid any hard reference
            if i == self.leftPhon:
                res.append(1.0)
            else:
                res.append(0.0)
        for i in range(0, 33):  # TODO: Avoid any hard reference
            if i == self.rightPhon:
                res.append(1.0)
            else:
                res.append(0.0)
        return res

    def getTarget(self, binary=True):
        if binary:
            if(self.good >= 3 and self.good <= 7):
                return [1.0, 0.0]
            else:
                return [0.0, 1.0]
        else:
            res = []
            for _ in range(0, 12):
                res.append(0)
            if self.good == 15:
                res[11] = 1.
            else:
                res[self.good] = 1.
            return res

    def isGood(self):
        return self.good == 5

    def isBegin(self):
        return self.infoRef[1] == "begin"