from __future__ import print_function

import sys

from pyspark import SparkContext, SparkConf

def main():
    
    conf = SparkConf().setAppName("Wordcount Application")
    sc = SparkContext(conf=conf)

    text_file = sc.textFile("hdfs://localhost:9000/input/")
    counts = text_file.flatMap(lambda line: line.split(" ")) \
             .map(lambda word: (word, 1)) \
             .reduceByKey(lambda a, b: a + b)
    counts.saveAsTextFile("hdfs://localhost:9000/output/")
    sc.stop()

if __name__ == "__main__":
    main()

'''
$ /opt/spark-2.2.1-bin-hadoop2.7/spark-submit wordcount.py
'''
