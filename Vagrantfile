$intro = <<EOF
Welcome to local development env for laundry!
---------------------------------------------

To finalize the startup do these:

$ vagrant rsync-auto
=> Run it in new terminal. It keeps host files synced to guest vm. Thus you can 
   edit the files on the host. Do not close the window.

$ vagrant ssh
=> To connect to the guest vm. Run `cd /vagrant` to work with the files.

Run these scripts to build & start the local server:

$ /vagrant/docker-build/build-all.sh
=> Builds and tags the docker images where the conversions will take place.

$ /vagrant/vagrant-dev/compile.sh
=> Compiles and packages the Clojure web app.

$ /vagrant/vagrant-dev/devserver.sh
=> Runs the web app. Available at http://192.168.123.123:8080/

Live-reload is not yet supported; thus recompile and restart the web app as
needed.

Run `lein repl :start` in `/vagrant/` to start the REPL on the guest. Connect to
it from the host with `lein repl :connect`.

EOF

Vagrant.configure("2") do |config|
  config.vm.define "laundry-dev"
  config.vm.hostname = "laundry-dev"
  config.vm.box = "centos/stream8"

  # Excluded '.git' from rsync-auto to reduce unnecessary syncs
  # Excluded 'target' from rsync-auto to not lose compiled jars from the VM on each sync
  config.vm.synced_folder ".", "/vagrant", type: "rsync", rsync__exclude: ['.git/', 'target/']

  # Static ip for easy access from host
  config.vm.network "private_network", ip: "192.168.123.123"

  # HTTP for docs and REST API, access at http://192.168.123.123:8080/
  config.vm.network "forwarded_port", guest: 8080, host: 8080, auto_correct: true
  # Clojure REPL available at port 43210
  config.vm.network "forwarded_port", guest: 43210, host: 43210

  # Install dependencies
  config.vm.provision "shell", path: "vagrant-dev/provision-docker-runsc-java.sh"

  # Instructions how to work with the VM are displayed after `vagrant up`
  config.vm.post_up_message = $intro
end
