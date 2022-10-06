# laundry

[![Build Status](https://travis-ci.org/solita/laundry.svg?branch=develop)](https://travis-ci.org/solita/laundry)

`laundry` converts user-supplied possibly dangerous files to more static and safer versions. Use it to reduce the risks of malware spreading via files supplied by external users or systems. The conversions are done with an up-to-date toolchain in a hardened stateless sandbox.

Antivirus products can mitigate the risks of malware, but they are imperfect. They mostly work against mass malware and have their own large attack surfaces. Consider using antivirus tool to check all user-supplied files and use `laundry` for additional level of security.

## Features

`laundry` provides an HTTP API for the conversions below.

| Input  | Output | Uses                                        | Purpose |
|--------|--------|---------------------------------------------|---------|
| doc(x) | pdf    | [LibreOffice](https://www.libreoffice.org/) | Removes any embedded macros etc and turns .doc(x) to portable PDF which can be e.g. embedded in HTML. |
| jpeg   | jpeg   | [ImageMagick](https://imagemagick.org/)     | Strip away all metadata and extraneous bytes, keep only pixel-by-pixel color data. Conversion performed with intermediate [PPM](https://en.wikipedia.org/wiki/Netpbm) format. |
| pdf    | pdf/a  | [Ghostscript](https://www.ghostscript.com/) | Archive PDF's as [PDF/A](https://en.wikipedia.org/wiki/PDF/A). |
| pdf    | jpeg   | [Ghostscript](https://www.ghostscript.com/) | Converts the first page to jpeg for thumbnails or previews. |
| pdf    | text   | [Ghostscript](https://www.ghostscript.com/) | Extract plain text from a PDF. Does **not** perform [OCR](https://en.wikipedia.org/wiki/Optical_character_recognition). |
| png    | png    | [ImageMagick](https://imagemagick.org/)     | Strip away all metadata and extraneous bytes, keep only pixel-by-pixel color data. Conversion performed with intermediate [PPM](https://en.wikipedia.org/wiki/Netpbm) format. |
| xls(x) | pdf    | [LibreOffice](https://www.libreoffice.org/) |  Removes any embedded macros etc and turns .xls(x) to portable PDF which can be e.g. embedded in HTML. |

The `laundry` HTTP server provides an REST API and online tool to try out the conversions directly from the browser. Optional API-key-based authorization is available.

Conversions are performed in single-use disposable Docker containers. The containers are secured, and their runtime is [gVisor](https://gvisor.dev/) `runsc`. It provides an additional layer of isolation for the containers.

## Installation instructions

Three kinds of installation methods are presented in the following subsections. 

- We recommend to start by doing a [Temporary installation for demo purposes](#temporary-installation-for-demo-purposes) so that you can integrate the `laundry` to your systems and processes. 
- The [Production installation with Docker and gVisor runsc](#production-installation-with-docker-and-gvisor-runsc) outlines the procedures for a common installation on a dedicated server or vm.
- You can optionally continue to harden the setup with instructions given in [Customized production  installations](#customized-production-installations)

### Temporary installation for demo purposes

**System requirements:** Linux or Mac & Docker

This installation method gives you the option to try out the laundry, integrate it into your systems or just to play around with it. This temporary installation method is **not suitable for production use** as it lacks sandboxing.

```sh
git clone https://github.com/solita/laundry.git
./laundry/docker-demo/build-and-run.sh
```

The script builds the necessary docker images including a temporary `laundry-demo`. It starts a docker container for the `laundry` HTTP server. The Docker host socket is exposed to the container, so that the `laundry-demo` can create temporary sibling containers for each conversion. `gVisor runsc` runtime is **not used** in the demo installation.

Default port is `8080`. The port can be given as parameter to the script

    ./docker-demo/build-and-run.bash -p 7777

See the script output for random api-key and the HTTP API address. Exit the demo with `docker stop laundry-demo`.

**Note:** macOS users might need to edit the script to run the `laundry-demo` container with `--user=root`, because the Docker socket has `root:root` ownership in the container.

### Production installation with Docker and gVisor runsc

**System requirements:** Linux with Docker, gVisor runsc, Java SDK and leiningen

Install the prerequisites:

 1. Docker: https://docs.docker.com/engine/install/
 2. gVisor runsc: https://gvisor.dev/docs/user_guide/install/, https://gvisor.dev/docs/user_guide/production/ and https://gvisor.dev/docs/architecture_guide/platforms/
 3. Java SDK should be version 11 or newer: https://adoptium.net/temurin/releases/
 4. Leiningen: https://leiningen.org/

**Note:** We recommend running [Docker Bench for Security](https://github.com/docker/docker-bench-security) before proceeding with the installation. It checks your Docker installation for common security-related best practices.

Download, build and install laundry as systemd service. The following example assumes that:

- Current user is `laundry`
- The user `laundry` has privileges to run `docker`
- Current directory is `/home/laundry`
- The HTTP API should be run in port `8080`
- A random API KEY should be generated and used for authorization

```sh
git clone https://github.com/solita/laundry.git

# docker images
./laundry/docker-build/build-all.sh

# HTTP server
(cd laundry && lein uberjar)

# API key
tr -dc 'a-zA-Z0-9' < /dev/urandom | head -c 32 >> /home/laundry/api-key.txt

# systemd service
sudo tee /etc/systemd/system/laundry.service <<EOF
[Unit]
Description=Laundry services

[Service]
ExecStart=/usr/bin/java -jar /home/laundry/laundry/target/default+uberjar/laundry.jar -p 8080 --api-key-file /home/laundry/api-key.txt
User=laundry
Group=laundry
Type=simple
KillMode=process
Restart=always
WorkingDirectory=/laundry/home

[Install]
WantedBy=multi-user.target
EOF

# enable and start laundry
sudo systemctl enable laundry.service
sudo systemctl start laundry.service

# verify installation is running
curl -w "\n" http://localhost:8080/alive

# verify request without api key is rejected
curl -I http://localhost:8080/auth-test

# verify request with api key succeeds
curl -I -u "laundry-api:$(cat /home/laundry/api-key.txt)" http://localhost:8080/auth-test
```

### Customized production installations

You can install and run a customized `laundry` with alternative sandboxing, such as [nsjail](https://github.com/google/nsjail). The scripts in `programs/` will be executed by the `laundry` HTTP API, thus you have the option to customize their behaviour; Clone the repository and edit the contents of `programs/` to match your needs.

You could also run `laundry` without Docker; Check the `docker-build/` Dockerfiles for dependencies of `programs/` scripts. Install them or customized versions of them into the host. Clone the repository and edit the `programs/` to invoke those directly without Docker.

## Local development

**System requirements:** Linux or Mac & [Vagrant](https://www.vagrantup.com/)

```sh
git clone https://github.com/solita/laundry.git

cd laundry
vagrant up
```

**Note:** Once `vagrant up` is ready it will print some instructions for you. Make sure to check them out!

Vagrant brings up an Centos Stream 8 VM with all the required dependencies. The source code is synced to `/vagrant`. Run `vagrant rsync-auto` to continue syncing further file edits from the host to the VM.

Connect to the VM with `vagrant ssh` and do `cd /vagrant`:
 
- Run `./docker-build/build-all.sh` to (re)build the required Docker images. 
- Run `./vagrant-dev/compile.sh` to compile the HTTP API with leiningen
- Run `./vagrant-dev/devserver.sh` to start the HTTP API at `http://192.168.123.123:8080/`

To work with Clojure REPL from the host you should first run `lein repl :start` inside the VM and then connect to it from the host with `lein repl :connect`.

### Testing

To run all tests except ones marked with `^:integration`:

    lein test

To run integration tests (that use docker):

    lein test :integration

All tests can be run with:

    lein test :all