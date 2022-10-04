# laundry

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

## Building

To rebuild the uberjar and docker images, run:

    ./rebuild.sh

To rebuild just the uberjar:

    lein uberjar

## Development environment

    java -jar target/uberjar/laundry.jar

Access swagger API docs on http://localhost:9001/api-docs/

## Vagrant environment

Vagrant uses ansible playbooks under `ansible/` to build (first time) and deploy laundry and the docker images to the vagrant box.

    vagrant up --provision

By default the host port 8080 (or the first available port after it) is forwarded to the guest, so to access swagger API docs, open <http://localhost:8080/api-docs/>.

Username for the API is `laundry-api`. The password can be read with the following commands:

    vagrant ssh
    $ sudo cat /opt/laundry/app/api-key.txt

## Testing

To run all tests except ones marked with `^:integration`:

    lein test

To run integration tests (that use docker):

    lein test :integration

As usual, all tests can be run with:

    lein test :all

If you don't have gvisor configured for docker, you may want to use `runc` as docker runtime:

    LAUNDRY_DOCKER_RUNTIME=runc lein test :all

On osx if you don't have rsyslog installed, you may also want to pass `LAUNDRY_DOCKER_LOG_DRIVER=none`.

## Setting up a new server

Build uberjar and docker images:

    ./rebuild.sh

Install to a server using ad-hoc inventory:

    ansible-playbook -u username -i 123.123.123.123, ansible/playbook.yml

Alternatively, create a `hosts` file and pass it to `ansible-playbook`:

    echo "123.123.123.123 ansible_user=username" > hosts
    ansible-playbook -i hosts ansible/playbook.yml

This will run laundry on port 8080.
To change the port, set the `laundry_port` ansible variable.
The ansible configuration was developed & tested with version 2.9.4.

## Using in development or CI/CD
_**Do not use this method in production. It is unsafe!**_

Laundry and required images can be built without cloning the entire repository:

    ./docker-dev/build-and-run.bash

The script builds `libreconv` and `laundry-programs` images and `laundry` itself using GitHub as build context.

When running the laundry, the Docker host socket is exposed to the laundry container, so that the laundry can create sibling containers.

Default port is 8080. The port can be given as parameter to the script

    ./docker-dev/build-and-run.bash -p 7777

### Running on Windows
You can run this in WSL but the distro has to be WSL version 2. The Docker socket won't have correct ownership otherwise.

### Running on Mac
Mac users might need to run the laundry container with `--user=root`, because the Docker socket has `root:root` ownership in the container.
