#!/bin/sh -x
git clean -xdf target/
ansible-playbook --connection=local ansible/build.yml
