from __future__ import print_function

import sys,re
import numpy
#from pyspark.sql import SparkSession # spark 2.0
from pyspark import SparkContext # spark 1.x
from pyspark.mllib import *
from pyspark.mllib.regression import LabeledPoint
from pyspark.mllib.linalg import Vectors
from pyspark.mllib.classification import SVMWithSGD
from pyspark.mllib.evaluation import BinaryClassificationMetrics

hdfs_nn = "127.0.0.1"

vector_fixed_size = 30 # fixed the size of each vector.
# if vectors have different sizes, the gradient descent algorithm will fail
# cut off if it exceeds, pad zeros if it has less than 30 elements

def hash(str):
    return reduce(lambda h,c:numpy.int32(31*h+ord(c)), str, 2147483647)

def to_words(tweet):
    return tweet.split(" ")

def pad_cap(xs,size):
    if (len(xs) == 0) and (size > 0) :
        return [ 0.0 for x in range(0,size) ]
    elif (len(xs) == 0 or size == 0):
        return []
    else:
        return [xs[0]] + pad_cap(xs[1:],size-1)

def to_labeledpoint(l, twt):
    ws = map(lambda w:hash(w), to_words(twt))
    return LabeledPoint(l, Vectors.dense(pad_cap(ws,vector_fixed_size)))

def main():
    sc = SparkContext()
    sc.appName = "Spark SVM"
    posTXT = sc.textFile("hdfs://%s:9000/data/tweet/label_data/Kpop/*.txt" % hdfs_nn)
    negTXT = sc.textFile("hdfs://%s:9000/data/tweet/label_data/othertweet/*.txt" % hdfs_nn)
    # convert the training data to labeled points
    posLP = posTXT.map(lambda twt:to_labeledpoint(1.0, twt))
    negLP = negTXT.map(lambda twt:to_labeledpoint(0.0, twt))
    data = posLP + negLP
    # Split data into training (60%) and test (40%).
    splits = data.randomSplit([0.6,0.4],seed = 11L)
    training = splits[0].cache()
    test = splits[1]

    # Run training algorithm to build the model
    num_iteration = 100
    model = SVMWithSGD.train(training,num_iteration)
    # Clear the default threshold
    model.clearThreshold()
    # Compute raw scores on the test set
    score_and_labels = test.map( lambda point: (float(model.predict(point.features)), point.label) )

    # Get the evaluation metrics
    metrics = BinaryClassificationMetrics(score_and_labels)
    au_roc = metrics.areaUnderROC

    print("Area under ROC = %s" % str(au_roc))
    sc.stop()

if __name__ == "__main__":
    main()

'''
$ /opt/spark-1.5.2-bin-hadoop2-hive2-r/bin/spark-submit extract.py
'''
