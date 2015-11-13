#!/bin/sh

## copy of starting instructions

# get all of the code
git clone git@github.com:Nitro/data-pipelines.git
cd data-pipelines/fp_for_ml

# get the 20 newsgroups data
mkdir –p data/; cd data
wget http://qwone.com/~jason/20Newsgroups/20news-19997.tar.gz
tar –zxf 20news-19997.tar.gz; rm 20news-19997.tar.gz
cd ../

# skip if you already have sbt installed
./sbt # downloads sbt
# once ^^ downloads sbt and loads * sbt * shell (not Scala!)
# relevant sbt commands:
#   console     [loads Scala REPL with all dependencies]
#   compile     [compile all project code]
#   test        [execute tests]
#   pack        [makes executable scripts for all Main files]

