javac *.java
time { 
	COUNTER=0
	while [ $COUNTER -lt 10 ]; do
		cat ../data/abstract.small.train | java -Xmx128m NBTrain | sort -k1,1 | java -Xmx128m MergeCounts | java -Xmx128m NBTest ../data/abstract.small.test > output.txt; 
	done
}
