from __future__ import print_function

import sys,re

#from pyspark.sql import SparkSession # spark 2.0
from pyspark import SparkContext # spark 1.x
hdfs_nn = "127.0.0.1"
pat = re.compile("^(.*) ([A-Za-z]{2}) ([0-9]{5})(-[0-9]{4})?$")

def match_each(line):
    r = pat.match(line.strip)
    if r is None:
        return line + "\tN"
    else:
        return line + "\tY"

def main():
    sc = SparkContext()
    sc.appName = "ETL (Extract) Example"
    input = sc.textFile("hdfs://%s:9000/data/extract/" % hdfs_nn)
    extracted = input.map(match_each)
    extracted.saveAsTextFile("hdfs://%s:9000/output/output/extracted" % hdfs_nn)
    sc.stop()

if __name__ == "__main__":
    main()

'''
$ /opt/spark-1.5.2-bin-hadoop2-hive2-r/bin/spark-submit extract.py
'''
