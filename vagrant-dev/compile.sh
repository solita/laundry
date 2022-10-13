#!/usr/bin/env bash

pushd /vagrant > /dev/null || exit 1

lein uberjar

popd > /dev/null || exit 1