#!/usr/bin/env bash

if [ ! -e target/lif ] ; then
    mkdir -p target/lif
fi
if [ ! -d target/xml ] ; then
    mkdir -p target/xml
fi

echo "Converting to LIF/JSON"
for file in `ls src/test/resources/data/*.xml` ; do
    filename=`basename $file`
    echo "    $filename"
    outfile=`echo $filename | sed 's/.xml/.lif/'`
    java -jar target/convert.jar $file > target/lif/$outfile
done

echo "Converting back to XMI"
for file in `ls target/lif/*.lif` ; do
    filename=`basename $file`
    echo "    $filename"
    outfile=`echo $filename | sed 's/.lif/.xml/'`
    java -jar target/convert.jar $file > target/xml/$outfile
done