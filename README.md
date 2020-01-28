# laundry
Data sanitation services

[![Build Status](https://travis-ci.org/solita/laundry.svg?branch=develop)](https://travis-ci.org/solita/laundry)

## Development environment

    lein uberjar
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
