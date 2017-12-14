from __future__ import print_function

import sys
from operator import add

#from pyspark.sql import SparkSession # spark 2.0
from pyspark import SparkContext # spark 1.x
hdfs_nn = "127.0.0.1"

def join(tokenized):
    x = (tokenized[1].split(":"))[1]
    y = (tokenized[2].split(":"))[1]
    return "\t".join([x,y])

def main():
    sc = SparkContext()
    sc.appName = "ETL (Transform) Example"
    input = sc.textFile("hdfs://%s:9000/data/transform/" % hdfs_nn)
    tokenizeds = input.map(lambda line : line.split(" "))
    tokenizeds.cache()

    ones = tokenizeds\
      .filter(lambda tokenized : tokenized[0] == "1")\
      .map(join)
    ones.saveAsTextFile("hdfs://%s:9000/output/ones" % hdfs_nn)

    zeros = tokenizeds\
      .filter(lambda tokenized : tokenized[0] == "0")\
      .map(join)
    zeros.saveAsTextFile("hdfs://%s:9000/output/zeros" %hdfs_nn)
    sc.stop()

if __name__ == "__main__":
    main()

'''
$ /opt/spark-1.5.2-bin-hadoop2-hive2-r/bin/spark-submit transform.py
'''
