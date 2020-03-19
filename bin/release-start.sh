#!/bin/bash
CURRENT_RELEASE_VERSION=$1
NEXT_DEVELOP_SNAPSHOT=$2

git fetch origin
git checkout -B develop remotes/origin/develop --

# create release branch
git branch "release/${CURRENT_RELEASE_VERSION}"

# update the versions on develop to the next -SNAPSHOT version
call mvn versions:set -DgenerateBackupPoms=false "-DnewVersion=${NEXT_DEVELOP_SNAPSHOT}"
git commit -a -m "[gitflow] updating poms for ${NEXT_DEVELOP_SNAPSHOT} development"

# push the changes atomically
git push --atomic origin develop "release/${CURRENT_RELEASE_VERSION}"