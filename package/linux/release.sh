#!/usr/bin/env bash
# Requirements:
#   - GPG signing key has been created
# Prior to running this script:
#   - Update version below

version=0.9.5-SNAPSHOT
base_dir=$( cd "$(dirname "$0")" ; pwd -P )/../../..
package_dir=$base_dir/package
release_dir=$base_dir/release/$version

deb=1m5-$version.deb

read -p "Enter email address used for gpg signing: " gpg_user

echo Creating release directory
if [ -d "$release_dir" ]; then
    rm -fr "$release_dir"
fi
mkdir -p "$release_dir"

echo Copying files to release folder

# signing key
cp "$package_dir/linux/signingkey.asc" "$release_dir"
if [ -f "$package_dir/linux/$deb" ]; then
    cp "$package_dir/linux/$deb" "$release_dir"
    cp "$package_dir/linux/$deb.txt" "$release_dir"
fi

echo Creating signatures
if [ -f "$release_dir/$deb" ]; then
    gpg --digest-algo SHA256 --local-user $gpg_user --output "$release_dir/$deb.asc" --detach-sig --armor "$release_dir/$deb"
fi

echo Verifying signatures
if [ -f "$release_dir/$deb" ]; then
    gpg --digest-algo SHA256 --verify "$release_dir/$deb.asc"
fi

echo Done!
