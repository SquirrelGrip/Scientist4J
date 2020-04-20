# Releasing
```
travis login --com --github-token $GIT_TOKEN
export SLUG=SquirrelGrip%2F$scientist4k
export TRAVIS_TOKEN=`travis token --no-interactive --com`
export BODY='{
    "request": {
        "message": "Starting a release",
        "branch":"develop",
        "config": {
            "env": {
                "jobs": [
                    "RELEASE=true"
                ]
            }
        }
    }
}'

curl -s -X POST \
   -H "Content-Type: application/json" \
   -H "Accept: application/json" \
   -H "Travis-API-Version: 3" \
   -H "Authorization: token $TRAVIS_TOKEN" \
   -d "$BODY" \
   https://api.travis-ci.com/repo/$SLUG/requests
```
