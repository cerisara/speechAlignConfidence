#!/bin/sh

cd library
sh build_all_library.sh
cd ..

#build TextGridAnalyzer
cd TextGridAnalyzer
ant jar
cd ..

#build TextGridCorrector
cd TextGridCorrector
ant jar
cd ..

#build MfccContextForCorrectionGenerator
cd MfccContextForCorrectionGenerator
ant jar
cd ..

# build DeepAlignSampleGenerator
cd DeepAlignSampleGenerator
ant jar
cd ..

# build GoldAnalyzer
cd GoldAnalyzer
ant jar
cd ..

