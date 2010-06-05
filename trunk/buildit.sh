#!/bin/bash


# ----------- GET INFO -----------------------------------
echo "get info..."
VERSION=`cat ./control/control | grep Version | awk '{print $2}'`
ID=`cat ./control/control | grep Package | awk '{print $2}'`
ARCH=`cat ./control/control | grep Architecture | awk '{print $2}'`
PACKAGE=$ID"_"$VERSION"_"$ARCH".ipk"

MOJO_VERSION=`cat src/appinfo.json | grep version | sed 's/[^0-9]*\([0-9\.]*\)[^0-9]*/\1/'`
MOJO_ID=`cat src/appinfo.json | grep id | sed 's/[^:]*:"\([^"]*\).*/\1/'`

# ----------- VERIFY INFO --------------------------------
echo "verify info..."
if [ $VERSION != $MOJO_VERSION ] ; then
    echo 
    echo "package version: "$VERSION
    echo "mojo version:    "$MOJO_VERSION
    echo "mojo version and package version isn't equals..."
    exit 1
fi
if [ $ID != $MOJO_ID ] ; then
    echo 
    echo "package id: "$ID
    echo "mojo id:    "$MOJO_ID
    echo "mojo ID and package ID isn't equals..."
    exit 1
fi


# ----------- PACK CONTROL PART --------------------------
echo "pack control part..."
if [[ ! ( -d .tmp ) ]] ; then
    mkdir .tmp
fi
cd ./control
tar -cf - . | gzip > ../.tmp/control.tar.gz
#tar -C $pkg_dir -czf $tmp_dir/data.tar.gz . --exclude=$CONTROL
#tar -C . -czvf ../.tmp/control.tar.gz *
cd ..

# ----------- BUILD JAVA SERVICE -------------------------
echo "build java service..."
cd VpnService/
ant > ../.tmp/ant.log 2>&1
antexit=$?
if [ "$antexit" -ne "0" ] ; then
    echo
    echo "Building java service failed!"
    echo "see .tmp/ant.log for more details"
    exit 1
fi
cp dist/VpnService.jar ../src/service
rm -rf dist
cd ..

# ----------- PACK MOJO PART -----------------------------
echo "pack data part..."
mkdir -p .tmp/usr/palm/applications/$ID/
cp -R ./src/*  ./.tmp/usr/palm/applications/$ID/
cd .tmp
tar -cf - usr | gzip > data.tar.gz
cd ..

# ----------- PACK FINAL PACKAGE -------------------------
echo "create ipk package..."
cd .tmp
echo 2.0 > ./debian-binary
rm ../$PACKAGE 
ar -r  ../$PACKAGE ./debian-binary control.tar.gz data.tar.gz
#tar -C $tmp_dir -czf ../$PACKAGE ./debian-binary ./data.tar.gz ./control.tar.gz
#tar -C . -czf ../$PACKAGE ./debian-binary ./data.tar.gz ./control.tar.gz
cd ..

# ----------- DONE ---------------------------------------

rm -rf .tmp
echo 
echo "done!"
echo "package: "$PACKAGE

