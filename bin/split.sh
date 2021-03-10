#!/usr/bin/env bash
# Inspired by laravel's split file: https://raw.githubusercontent.com/laravel/framework/5.7/bin/split.sh

set -e
set -x

CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [ "$(uname)" == "Darwin" ]; then
    SPLITSH=./bin/splitsh-lite_darwin_amd64
else
    SPLITSH=./bin/splitsh-lite_linux_amd64
fi

function split()
{
    SHA1=`$SPLITSH --prefix=$1`
    git push $2 "$SHA1:refs/heads/$CURRENT_BRANCH" -f
}

function remote()
{
    git remote add $1 $2 || true
}

git pull origin $CURRENT_BRANCH

remote php-coding-standard git@github.com:kronostechnologies/php-coding-standard.git

split 'php/php-coding-standard' php-coding-standard
