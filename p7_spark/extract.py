from __future__ import print_function

import sys,re

from pyspark import SparkContext, SparkConf

hdfs_nn = "127.0.0.1"
pat = re.compile("^(.*) ([A-Za-z]{2}) ([0-9]{5})(-[0-9]{4})?$")

def match_each(line):
    r = pat.match(line.strip())
    if r is None:
        return line + "\tN"
    else:
        return line + "\tY"

def main():
    conf = SparkConf().setAppName("ETL (Extract) Example")
    sc = SparkContext(conf=conf)
    input = sc.textFile("hdfs://%s:9000/data/extract/" % hdfs_nn)
    extracted = input.map(match_each)
    extracted.saveAsTextFile("hdfs://%s:9000/output/output/extracted" % hdfs_nn)
    sc.stop()

if __name__ == "__main__":
    main()

'''
$ /opt/spark-2.2.1-bin-hadoop2.7/spark-submit extract.py
'''
