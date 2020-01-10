#!/bin/sh
set -eux

lein test

ansible-galaxy install --role-file=ansible/dependencies.yml --roles-path=ansible/roles
ansible-playbook --connection=local -i localhost, ansible/build.yml ansible/playbook.yml

ln -s programs bin
env LAUNDRY_DOCKER_RUNTIME=runc lein test :integration

curl http://localhost:8080/alive
