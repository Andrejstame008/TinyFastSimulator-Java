#!/bin/bash

if [ ! -f "./TinyFastSimulator-Java.jar" ]
then
	echo "Simulator program file (TinyFastSimulator-Java.jar) not found"
	exit 1
fi

if [ "$1" == ""  -o "$2" == ""  -o "$3" == "" ]
then
	echo "The script has to be called with three parameters:"
	echo "1. Maximum number of threads"
	echo "2. Number of arrivals (in single thread mode) to be simulated"
	echo "3. Output file for the results"
	echo "Example: ./TinyFastSimulator-Benchmark.sh 32 100000000 results.txt"
	exit 1
fi

echo "Benchmarking the performance at different numbers of threads"
echo "Output will be written to $3"

echo -e "Threads\tRuntime [sec.]\tMemory [KB]" >> $3
for ((x=1;x<=$1;x++))
do	
    echo "Running simulation $x of $1"
	echo -en "$x\t" >> $3
	\time -f "%e\t%M" -o $3 -a java -jar TinyFastSimulator-Java.jar threads=$x arrivals=$2 increase_arrivals > /dev/null
done