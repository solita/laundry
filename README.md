# laundry
Data sanitation services

[![Build Status](https://travis-ci.org/solita/laundry.svg?branch=develop)](https://travis-ci.org/solita/laundry)

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
