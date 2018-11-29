#!/bin/bash
project="time"
proj="Time"
prePath=/Users/yrr/Desktop/defects4j/buggyVersions/$project
mkdir $prePath
for((i=1;i<=27;i++))
do
    path=$prePath/$project"_"$i"_buggy"
    echo "---------- process "$proj" "$i" -------------------------"
    defects4j checkout -p $proj -v $i'b' -w $path
done

