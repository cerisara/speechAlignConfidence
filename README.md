# Weakly-supervised text-to-speech alignment confidence measure

## Repository overview

This repository gives all the information, code with links to data and corpora related to the COLING 2016 paper:
"Weakly-supervised text-to-speech alignment confidence measure" by
Guillaume Serrière, Christophe Cerisara, Dominique Fohr and Odile Mella.

As a preliminary warning, please note however that the complete process to fully reproduce the paper results may be
very long and require a lot of work: we are extremely sorry about that, but this is pitifully due to a quite complex
process, which involves manipulating large corpora and various pieces of software written in different languages.

The most important software component of the pipeline is the deep neural networks that compute a text-to-speech alignment confidence measure.
This deep neural network is language-independent and is composed of three models:

- inspector.py, called Boundary inspector in the paper;
- selector.py, called Boundary selector in the paper;
- mlp\_aggregator.py, called Aggregator in the paper.

inspector.py is trained on a set of positive and negative examples, where each positive example corresponds to a reliable
word boundary. Ideally, such a set of correct word boundary may be defined by hand, but in practice, we have used 
the agreement between two automatic text-to-speech aligners to select the most reliable word boundaries.
This positive training set is then augmented with negative examples, which are obtained by random translation of the correct word boundary timestamps.

selector.py is trained on a corpus of short frame sequences around each correct word boundarie: these sequences shall be
built so that the correct word boundary is not always in the center. Ideally, its position should follow the distribution of
the original text-to-speech aligner system, but in practice, we have use a uniform distribution.
The training set of selector.py is composed of the same files as the training set of inspector.py.

The mlp\_aggregator is trained afterwards on another corpus of positive and negative examples that has been first processed by both inspector.py and selector.py.

## Datasets and software dependencies

- The model is validated on the French spontaneous speech corpus TCOF, which sound files and transcriptions are freely available [here](http://www.cnrtl.fr/corpus/tcof)
- The automatic text-to-speech alignment of TCOF will be released as part of the ORFEO corpus at the end of the project. It will then be made available [here](http://www.projet-orfeo.fr), but in the meantime, you can easily realize this alignment yourself with the open-source JTrans software that can be downloaded [here](https://github.com/synalp/jtrans).
- Part of the TCOF corpus automatic alignment has been manually corrected: this corpus constitues our gold evaluation corpus; it is available [here](http://talc1.loria.fr/users/cerisara/goldcorpus.tgz).
- The ASTALI text-to-speech alignment software may be requested to its authors (fohr@loria.fr). It is used to extract the most likely correct word boundaries obtained with JTrans in order to build a training corpus for the deep models. Note however that it is not absolutely required to combine both ASTALI and JTrans to build this corpus, as alternative heuristics may even prove better. For instance, you may actually perform a fast manual verification of some of the proposed word boundaries in order to bootstrap the confidence model, and then tune it at a high-precision configuration and use it in a self-training fashion to iteratively increase the number of reliable correct boundaries.
- The source code of the proposed confidence models is available in this repository in the directory *code/*
- This source code depends on the [Keras](http://keras.io) v1.0 deep learning library and on python2.7

## Development guide

1- Build all tools and libraries

> sh build_all.sh

2- Generate metadata and mfcc dictionary

> java -jar gen_all_context.jar [jtrans textgrid folder] [astali textgrid folder] [mfcc file folder] [output Metadata] [output Mfcc dictionnary] [amount of frame on left] [amount of frame on right]

3- Split the metadata between dev and train

> cd script

> python split_meta.py [metadata] [output Train metadata] [output Dev metadata]

4- Normalize the mfcc directory

> python normalize_dict.py [Mfcc dictionnary] [output Normalized mfcc dictionnary] [output Min value (numpy)] [output Max value (numpy)]

5- Train models

> python inspector.py  [Train metadata] [Dev metadata] [Normalized dictionnary] [Amount of Epoch] [Droupout value]

> python selector.py  [Train metadata] [Dev metadata] [Normalized dictionnary] [Amount of Epoch] [Droupout value]

6- Compute activation for each model on dev

> python compute_inspector_dev.py  [Configuration archive] [Dev metadata] [Normalized dictionnary] [output Activations]

> python compute_selector_dev.py  [Configuration archive] [Dev metadata] [Normalized dictionnary] [output Activations]

7- Split metadata for the aggregator

> python split_dev.py [Dev metadata] [output TrainAgg metadata] [output DevAgg metadata]

8- Train aggregator

> python mlp_aggregator.py [Activations model1] [Activations model3] [TrainAgg metadata] [DevAgg metadata]

9- Generate gold informations

> java -jar goldAnalyzer.jar [Jtrans textgrid] [Gold textgrid] [output Gold informations]

10- Generate a global gold informations file by concat all gold informations files

11- Generate stats on aggregator and correction

> cd script

> python align_corrector.py [Global gold informations] [Gold mfcc directory] [Minimum Values (numpy)] [Maximum values (numpy)] [Activation threshold] [step size] [Maximum step]


