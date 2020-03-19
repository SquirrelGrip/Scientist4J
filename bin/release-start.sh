#!/bin/bash
mvn --batch-mode -U -DtrimStackTrace=false -e clean jgitflow:release-start -PgitflowStart