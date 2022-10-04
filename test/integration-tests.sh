#!/bin/sh
set -eux

ansible-galaxy install --role-file=ansible/dependencies.yml --roles-path=ansible/roles -e ansible_python_interpreter=python3
ansible-playbook --connection=local -i localhost, ansible/build.yml ansible/playbook.yml  -e ansible_python_interpreter=python3

ln -s programs bin
env LAUNDRY_DOCKER_RUNTIME=runc lein test :integration

curl http://localhost:8080/alive
