from __future__ import print_function

import sys

#from pyspark.sql import SparkSession # spark 2.0
from pyspark import SparkContext # spark 1.x

def main():
    sc = SparkContext()
    sc.appName = "Wordcount Application"
    text_file = sc.textFile("hdfs://localhost:9000/input/")
    counts = text_file.flatMap(lambda line: line.split(" ")) \
             .map(lambda word: (word, 1)) \
             .reduceByKey(lambda a, b: a + b)
    counts.saveAsTextFile("hdfs://localhost:9000/output/")
    sc.stop()

if __name__ == "__main__":
    main()

'''
$ /opt/spark-1.5.2-bin-hadoop2-hive2-r/bin/spark-submit wordcount.py
'''
