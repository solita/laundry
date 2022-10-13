# laundry development guide

## Local development environment

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

## Testing

To run all tests except ones marked with `^:integration`:

    lein test

To run integration tests (that use docker):

    lein test :integration

All tests can be run with:

    lein test :all

## Releases

Version numbers follow [semantic versioning](https://en.wikipedia.org/wiki/Software_versioning#Semantic_versioning) principles:

- Breaking changes either to the API or system requirements are presented by incrementing the _major_ version.
- New non-breaking features are indicated by incrementing the _minor_ number.
- All other changes are indicated by incrementing the _patch_ number.

Version numbers are marked with [Git Tags](https://github.com/solita/laundry/tags).

Releases are distributed with [GitHub Releases](https://github.com/solita/laundry/releases). The assets are built & bundled by a GitHub Workflow, see `.github/workflows/release-from-tags.yml`. See [GitHub Docs](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository) for introduction to release management with GitHub.

### How to publish a new release

1. Switch to `main` branch: `git switch main`
2. Check the state of the working tree: `git status`
3. Create a tag for your version: `git tag v1.2.3 main`
4. Push the tag to GitHub: `git push origin v1.2.3`
5. Wait for ~5 minutes for the [workflow](https://github.com/solita/laundry/actions/workflows/release-from-tags.yml) to create a new Release draft.
6. Review the release draft at https://github.com/solita/laundry/releases. Edit the release notes as needed and publish when all looks good.

If the release draft or git tag had issues/errors you should [undo the release draft & tags](#how-to-undo-a-release-draft) and retry.

### How to undo a release draft

To undo a release draft you'll need to take care of the Release draft and git tags.

1. Delete the release draft with trashcan icon from [Releases page](https://github.com/solita/laundry/releases)
2. Delete the local git tag with `git tag -d v1.2.3`
3. Push the deletion to GitHub: `git push :refs/tags/v1.2.3`
