# This is an auxiliary container that is used to build the binaries that will go into the deployed laundry.
# first, build:
# docker build -t laundrybuild .
# then, run like: 
# docker run --cap-add SYS_PTRACE -it  --name laundrybuild laundrybuild
#
# To debug the build interactiely, start this container like:
# docker run --cap-add SYS_PTRACE -it  --name laundrybuild --entrypoint=/bin/bash laundrybuild
# and then run the commands in ENTRYPOINT manually.

FROM ubuntu:18.04
RUN apt-get update && apt-get -y upgrade && apt-get -y install git make unzip && useradd -m laundrybuild && mkdir /opt/laundry && chown laundrybuild /opt/laundry

# the following deps are to bake probably needed packages into the build image, to reduce build times
RUN apt-get -y install --download-only libpng-dev libjpeg-dev build-essential clang wget unzip curl install pkg-config bison flex libprotobuf-dev protobuf-compiler

WORKDIR /home/laundrybuild
# branch env var can be overridden with -e on docker command line (as is done in our travis conf)
ENV LAUNDRY_BUILD_BRANCH=develop
ENTRYPOINT su -c "git clone -b $LAUNDRY_BUILD_BRANCH https://github.com/solita/laundry.git" laundrybuild && cd laundry && cd programs && make deps && su -c "make install" laundrybuild
