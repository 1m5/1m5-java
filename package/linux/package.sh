#!/usr/bin/env bash
# Requirements:
#   - OracleJDK 10 installed
#     Note: OpenJDK 10 does not have the javapackager util, so must use OracleJDK
# Prior to running this script:
#   - Update version below
#   - Ensure JAVA_HOME below is pointing to OracleJDK 10 directory

version=0.9.5-SNAPSHOT
if [ ! -f "$JAVA_HOME/bin/javapackager" ]; then
	if [ -d "/usr/lib/jvm/jdk-10.0.2" ]; then
    	JAVA_HOME=/usr/lib/jvm/jdk-10.0.2
	else
	    echo Javapackager not found. Update JAVA_HOME variable to point to OracleJDK.
	    exit 1
	fi
fi

base_dir=$( cd "$(dirname "$0")" ; pwd -P )/../../..
src_dir=$base_dir/desktop/package

cd $base_dir

set -eu

echo Installing required packages
if [[ -f "/etc/debian_version" ]]; then
    sudo apt install -y fakeroot rpm
fi

if [ ! -f "$base_dir/package/1m5-$version.jar" ]; then
    echo Building application
    ./gradlew :desktop:clean :desktop:build -x test shadowJar
    jar_file=$base_dir/desktop/build/libs/desktop-$version.jar
    if [ ! -f "$jar_file" ]; then
        echo No jar file available at $jar_file
        exit 2
    fi

    tmp=$base_dir/desktop/build/libs/tmp
    echo Extracting jar file to $tmp
    if [ -d "$tmp" ]; then
        rm -rf $tmp
    fi
    mkdir -p $tmp
    unzip -o -q $jar_file -d $tmp

    echo Deleting problematic module config from extracted jar
    # Strip out Java 9 module configuration used in the fontawesomefx library as it causes javapackager to stop
    # because of this existing module information, since it is not used as a module.
    # Sometimes module-info.class does not exist
    if [ -f "$tmp/module-info.class" ]; then
        rm -f $tmp/module-info.class
    fi

    jar_file=$base_dir/package/1m5-$version.jar
    echo Zipping jar again without module config to $jar_file
    cd $tmp; zip -r -q -X $jar_file *
    cd $base_dir; rm -rf $tmp

    echo SHA256 before stripping jar file:
    shasum -a256 $jar_file | awk '{print $1}'

    echo Making deterministic jar by stripping out parameters and comments that contain dates
    java -jar $base_dir/package/tools-1.0.jar $jar_file

    echo SHA256 after stripping jar file:
    shasum -a256 $jar_file | awk '{print $1}' | tee $base_dir/package/1m5-$version.jar.txt
else
    local_src_dir="/home/$USER/1m5/build"
    mkdir -p $local_src_dir
    cp $base_dir/package/1m5-$version.jar $local_src_dir/1m5-$version.jar
    src_dir=$local_src_dir
fi

chmod o+rx "$src_dir/1m5-$version.jar"

# Remove previously generated packages so we can later determine if they are actually generated successfully
if [ -f "$base_dir/package/linux/1m5-$version.deb" ]; then
    rm "$base_dir/package/linux/1m5-$version.deb"
fi

# TODO: add the license as soon as it is working with our build setup
#-BlicenseFile=LICENSE \
#-srcfiles package/linux/LICENSE \

echo Generating deb package
$JAVA_HOME/bin/javapackager \
    -deploy \
    -BappVersion=$version \
    -Bcategory=Network \
    -Bemail=info@1m5.io \
    -BlicenseType=Unlicense \
    -Bicon=$base_dir/package/linux/icon.png \
    -native deb \
    -name 1M5 \
    -title "Uncensored Communications" \
    -vendor 1M5 \
    -outdir $base_dir/package/linux \
    -srcdir $src_dir \
    -srcfiles 1m5-$version.jar \
    -appclass io.onemfive.OneMFivePlatform \
    -BjvmOptions=-Xss1280k \
    -outfile 1m5-$version \
    -v

if [ ! -f "$base_dir/package/linux/1m5-$version.deb" ]; then
    echo No deb file found at $base_dir/package/linux/1m5-$version.deb
    exit 3
fi

if [ -f "$base_dir/package/linux/1m5-$version.deb" ]; then
   rm "$base_dir/package/linux/1m5-$version.deb"
fi

echo SHA256 of $base_dir/package/linux/1m5-$version.deb:
shasum -a256 $base_dir/package/linux/1m5-$version.deb | awk '{print $1}' | tee $base_dir/package/linux/1m5-$version.deb.txt

echo Done!
