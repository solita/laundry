# laundry
Data sanitation services

[![Build Status](https://travis-ci.org/solita/laundry.svg?branch=develop)](https://travis-ci.org/solita/laundry)

## Development environment

    lein uberjar
    ln -s programs bin
    java -jar target/uberjar/laundry.jar -t $PWD

Access swagger API docs on http://localhost:9001/api-docs/

## Vagrant environment

Vagrant uses ansible playbooks under `ansible/` to build (first time) and deploy laundry and the docker images to the vagrant box.

    vagrant up --provision

By default the host port 8080 (or the first available port after it) is forwarded to the guest, so to access swagger API docs, open <http://localhost:8080/api-docs/>.

Username for the API is `laundry-api`. The password can be read with the following commands:

    vagrant ssh
    $ sudo cat /opt/laundry/app/api-key.txt
