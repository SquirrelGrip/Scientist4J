#!/bin/bash
:: checkout the release branch
git fetch "origin"
git checkout -B release/%CURRENT_RELEASE_VERSION% remotes/origin/release/%CURRENT_RELEASE_VERSION% --

:: replace the -SNAPSHOT versions on the release branch with the release versions
call mvn versions:set -DgenerateBackupPoms=false -DnewVersion=%CURRENT_RELEASE_VERSION%
git commit -a -m "[gitflow] updating poms for branch 'release/%CURRENT_RELEASE_VERSION%' with non-snapshot versions"

:: merge the release branch to master and create a tag
git checkout -B master remotes/origin/master --
git merge --no-ff -m "[gitflow] merging 'release/%CURRENT_RELEASE_VERSION%' into 'master'" release/%CURRENT_RELEASE_VERSION%
git tag %CURRENT_RELEASE_VERSION%

:: update the -SNAPSHOT versions on develop with the release version to avoid merge conflicts
git checkout -B develop remotes/origin/develop --
call mvn versions:set -DgenerateBackupPoms=false -DnewVersion=%CURRENT_RELEASE_VERSION%
git commit -a -m "[gitflow] updating develop poms to master versions to avoid merge conflicts"

:: merge master to develop
git merge --no-ff -m "[gitflow] merging 'master' into 'develop'" master

:: set the versions on develop back to the next -SNAPSHOT version
call mvn versions:set -DgenerateBackupPoms=false -DnewVersion=%NEXT_DEVELOP_SNAPSHOT%
git commit -a -m "[gitflow] updating develop poms back to pre merge state"

:: push the changes atomically
git push --atomic origin master develop refs/tags/%CURRENT_RELEASE_VERSION%

:: delete the release branch
git push origin --delete release/%CURRENT_RELEASE_VERSION%
git branch -d release/%CURRENT_RELEASE_VERSION%