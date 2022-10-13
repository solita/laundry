#!/usr/bin/env bash
set -ex

# 1. docker installation and permissions for `vagrant` user.
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
dnf -y install docker-ce docker-ce-cli containerd.io

usermod -aG docker vagrant

systemctl enable docker.service
systemctl enable containerd.service

# 2. the docker runtime is gvisor runsc which provides additional layer of sandboxing.
curl --fail -o /usr/local/bin/runsc https://storage.googleapis.com/gvisor/releases/nightly/2019-11-11/runsc
echo "3a3ea9ef6d6f8f23e4748af925e23811946d719d5542e09e5afbadc086543cef2a69d78843683f2ec683d93395eb59c8738d791ca037d86832ded307465e9db3  /usr/local/bin/runsc" | sha512sum -c -
chmod a+x /usr/local/bin/runsc
/usr/local/bin/runsc install -- --platform=ptrace
systemctl restart docker

# 3. the HTTP API is implemented with Clojure, thus java & leiningen are required.
dnf -y install java-11-openjdk-headless
curl --silent --fail -o /usr/local/bin/lein https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
chmod a+x /usr/local/bin/lein
