#!/bin/bash
project="lang"
proj="Lang"
prePath=/Users/yrr/Desktop/defects4j/fixedVersions/$project
mkdir $prePath
for((i=1;i<=65;i++))
do
    path=$prePath/$project"_"$i"_fixed"
    echo "---------- process "$proj" "$i" -------------------------"
    defects4j checkout -p $proj -v $i'f' -w $path
done

