#!/bin/bash

PATH=/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin
cd /usr/local
which git || dnf install -y git nano
which java || dnf -y install java-11-openjdk
test -d laundry || git clone https://github.com/solita/laundry.git
cd laundry
which ansible-playbook || pip3 install ansible
ansible-galaxy install --role-file=ansible/dependencies.yml --roles-path=ansible/roles
curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > /usr/local/bin/lein
chmod +x /usr/local/bin/lein
pip3 install docker
ansible-playbook -v --connection=local -i localhost, ansible/playbook.yml -e ansible_python_interpreter=python3 || true # fails on first time (circular dep between build and playbook? build playbook doesn't work if run before mai playbook either?)

ansible-playbook -v --connection=local -i localhost, ansible/build.yml ansible/playbook.yml -e ansible_python_interpreter=python3 # fails to start server at the end?
systemctl restart laundry

