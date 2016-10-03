#!/usr/bin/python
# Phon information


def getPhonCode(phon):
    if phon == "a":
        return 0
    elif phon == "an" or phon == "a~":
        return 1
    elif phon == "b":
        return 2
    elif phon == "ch" or phon == "S":
        return 3
    elif phon == "d" or phon == "d":
        return 4
    elif phon == "e" or phon == "eh" or phon == "E" or phon == "E/":
        return 5
    elif phon == "eu" or phon == "2":
        return 6
    elif phon == "euf" or phon == "9":
        return 7
    elif phon == "f":
        return 8
    elif phon == "g":
        return 9
    elif phon == "ge" or phon == "Z":
        return 10
    elif phon == "gn" or phon == "J":
        return 11
    elif phon == "i" or phon == "H":
        return 12
    elif phon == "in" or phon == "e~" or phon == "9~":
        return 13
    elif phon == "j" or phon == "%j":
        return 14
    elif phon == "k":
        return 15
    elif phon == "l":
        return 16
    elif phon == "m" or phon == "mm":
        return 17
    elif phon == "n":
        return 18
    elif phon == "oh" or phon == "o" or phon == "O" or phon == "O/":
        return 19
    elif phon == "on" or phon == "o~":
        return 20
    elif phon == "p" or phon == "pp":
        return 21
    elif phon == "r" or phon == "R" or phon == "rr":
        return 22
    elif phon == "s":
        return 23
    elif phon == "SIL" or phon == "#" or phon == "bb" or phon == "bip" or phon == "&blabla" or phon == "&bruit" or phon == "hh" or phon == "xx":
        return 24
    elif phon == "swa" or phon == "@":
        return 25
    elif phon == "t" or phon == "tt":
        return 26
    elif phon == "u" or phon == "U~/":
        return 27
    elif phon == "v":
        return 28
    elif phon == "w" or phon == "%w":
        return 29
    elif phon == "y":
        return 30
    elif phon == "z":
        return 31
    elif phon == "":
        return 32
    else:
        return -1


def getPhonCount():
    return 33


class Phon:
    def __init__(self, phon):
        self.phonCode = getPhonCode(phon)

    def isValid(self):
        return not self.phonCode == -1

    def getPhonArray(self):
        res = []
        for i in range(0, getPhonCount()):
            if i == self.phonCode:
                res.append(1.0)
            else:
                res.append(0.0)
        return res