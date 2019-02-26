# This is an auxiliary container that is used to build the binaries that will go into the deployed laundry.
# first, build:
# docker build -t laundrybuild .
# then, run like: 
# docker run --cap-add SYS_PTRACE -it  --name laundrybuild laundrybuild .

FROM ubuntu:18.04
RUN apt-get update && apt-get -y upgrade && apt-get -y install git make libpng-dev libjpeg-dev build-essential clang wget unzip curl && useradd -m laundrybuild && mkdir /opt/laundry && chown laundrybuild /opt/laundry
WORKDIR /home/laundrybuild
ENV LAUNDRY_BUILD_BRANCH=develop
ENTRYPOINT su -c "git clone -b $LAUNDRY_BUILD_BRANCH https://github.com/solita/laundry.git" laundrybuild && cd laundry && cd programs && su -c "make deps" laundrybuild && su -c "make install" laundrybuild
