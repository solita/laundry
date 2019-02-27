#!/usr/bin/env bash
set -euxo pipefail

# clojure build (to check but also because we run the uberjar later in the e2e test.sh)
lein uberjar

if [ ! -f $CI_CACHE/laundrybuild.tar ]; then
    pushd docker-build
      docker build -t laundrybuild .
    popd
    docker save laundrybuild > $CI_CACHE/laundrybuild.tar
fi
docker load < $CI_CACHE/laundrybuild.tar

docker run --cap-add SYS_PTRACE -it --name laundrybuild -e LAUNDRY_BUILD_BRANCH=$TRAVIS_BRANCH laundrybuild

pushd docker-build
  bash export-opt.bash laundrybuild laundry-opt.tgz
popd

# xxx move test.sh to a later stage once it's debugged
tar -C /opt -xzf docker-build/laundry-opt.tgz && env LAUNDRY_DOCKER_RUNTIME=runc test/test.sh

pushd docker-build
  docker build -f Dockerfile.programs-runtime -t laundry-programs  .
  docker build -t libreconv - < Dockerfile.libreoffice
popd


env LAUNDRY_DOCKER_RUNTIME=runc programs/pdf2pdfa test/testcases/hypno.pdf /tmp/hypno-pdfa.pdf && file /tmp/hypno-pdfa.pdf | grep "PDF document"

pushd programs
  make docker-libre-image
popd

docker run -i -e docconv_suffix=doc --rm --name convrun1 libreconv < test/testcases/test.doc > /tmp/docresult.tar
tar --to-stdout -xf /tmp/docresult.tar out/document.pdf | file - | grep "PDF document"
docker run -i -e docconv_suffix=docx --rm --name convrun1 libreconv < test/testcases/test.docx > /tmp/docxresult.tar
tar --to-stdout -xf /tmp/docxresult.tar out/document.pdf | file - | grep "PDF document"

# - ldd /opt/laundry/bin/gs
# - /opt/laundry/bin/pdf2txt test/testcases/hypno.pdf /tmp/out.txt && cat /tmp/out.txt
