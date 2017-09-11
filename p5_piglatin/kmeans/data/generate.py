import sys
import random

def gen(num_of_records, filename):

	f = open(filename,'w')
	for i in range(0,int(num_of_records)):
		x = random.uniform(-100,100)
		y = random.uniform(-100,100)
		print >> f, "%.2f\t%.2f" % (x,y)
	f.close()


if __name__ == "__main__":
	if len(sys.argv) > 2:
		sys.exit(gen(sys.argv[1],sys.argv[2]))
	else:
		print "USAGE: python generate.py <number_of_records> <file_name>"
