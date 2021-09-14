# Copyright 2021 Harness Inc.
# 
# Licensed under the Apache License, Version 2.0
# http://www.apache.org/licenses/LICENSE-2.0

set -e

export VERSION=`cat destination/dist/${SERVICE}/version.txt`

export PURPOSE=`cat destination/dist/${SERVICE}/purpose.txt 2>/dev/null`
if [ ! -z "${PURPOSE}" ]
then
    export PURPOSE=/${PURPOSE}
fi


export IMAGE_TAG=`cat destination/dist/${SERVICE}/image_tag.txt 2>/dev/null`
if [ -z "${IMAGE_TAG}" ]
then
    export IMAGE_TAG=${VERSION}
fi

docker login -u _json_key --password-stdin https://us.gcr.io < $GCR_CREDENTIALS

export IMAGE_REPO="us.gcr.io/platform-205701/harness${PURPOSE}/${SNAPSHOT_PREFIX}${SERVICE}:${IMAGE_TAG}"
docker build -t ${IMAGE_REPO} destination/dist/${SERVICE} -f destination/dist/${SERVICE}/Dockerfile-gcr
docker push ${IMAGE_REPO}
