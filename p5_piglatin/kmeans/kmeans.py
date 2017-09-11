#!/bin/python

import sys
from org.apache.pig.scripting import *



def main(points_path, centroids_path, new_centroids_path):
	P = Pig.compile(
		"""
		POINTS = load '$points' using PigStorage('\t') as (x:double,y:double);
		CENTROIDS = load '$centroids' using PigStorage('\t') as (cx:double,cy:double);
		GC = foreach (group CENTROIDS all) generate $1 as centroids;
		POINTS_AND_CENTROIDS = CROSS POINTS, GC;
		POINTS_WITH_NEAREST_CENTROID = foreach POINTS_AND_CENTROIDS generate x, y, myfunc.nearest_centroid(x,y,centroids) as centroid;
		CLUSTERS = group POINTS_WITH_NEAREST_CENTROID by centroid;
		NEWCENTROIDS = foreach CLUSTERS generate myfunc.mean(POINTS_WITH_NEAREST_CENTROID) as newcentroid;
		OUT = foreach NEWCENTROIDS generate newcentroid.mx, newcentroid.my;
		store OUT into '$newcentroids' using PigStorage('\t');	
		"""
		)
	Pig.registerUDF("udf.py", "myfunc")

	params = {
		'points': points_path,
		'centroids' : centroids_path,
		'newcentroids' : new_centroids_path
	}

	bound = P.bind(params)



	for i in range(0,10):
		Pig.fs("rmr " + new_centroids_path)
		stats = bound.runSingle()
		Pig.fs("cp  " + new_centroids_path + "/* " + centroids_path + "/")
	return 0;



if __name__ == "__main__":
	if len(sys.argv) > 3:
		sys.exit(main(sys.argv[1],sys.argv[2],sys.argv[3]))
	else:
		print "USAGE : pig kmeans.py <points> <centroids> <newcentroids>"
