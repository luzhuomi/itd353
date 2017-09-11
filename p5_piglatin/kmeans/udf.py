import math

@outputSchema("t:tuple(cx:double,cy:double)") 
def nearest_centroid(x,y,centroids):
	result = None
	min_ed = None
	for centroid in centroids:
		ed = math.sqrt( (x-centroid[0])**2 + (y-centroid[1])**2 )
		if result is None:
			result = centroid
			min_ed = ed
		elif min_ed > ed:
			result = centroid
			min_ed = ed
		else:
			pass
	return result

@outputSchema("t:tuple(mx:double,my:double)")
def mean(points_with_centroid):
	sx = 0
	sy = 0
	for (x,y,centroid) in points_with_centroid:
		sx = sx + x
		sy = sy + y
	l = len(points_with_centroid)
	return (sx/l, sy/l)
