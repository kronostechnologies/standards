#!/usr/bin/env bash

set -e

if (( "$#" != 1 ))
then
    echo "Tag has to be provided"

    exit 1
fi

ROOT_FOLDER=$PWD
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

for REMOTE in php-coding-standard 
do
    echo ""
    echo ""
    echo "Releasing $REMOTE";

    TMP_DIR="/tmp/standards-split"
    REMOTE_URL="git@github.com:kronostechnologies/$REMOTE.git"

    rm -rf $TMP_DIR;
    mkdir $TMP_DIR;

    (
        cd $TMP_DIR;

        git clone $REMOTE_URL .
        git checkout "$CURRENT_BRANCH";

        git tag $1

        echo $1 > $ROOT_FOLDER/php/$REMOTE/.tag 
        echo $1 > $TMP_DIR/.tag

        git add $TMP_DIR/.tag
        git commit -m "Push tag $1"

        git push origin
        git push origin --tags
    )
done