Vagrant.configure("2") do |config|
  config.vm.box = "centos/7"

  config.vm.network "forwarded_port", guest: 8080, host: 8080, auto_correct: true

  config.vm.provision "ansible" do |ansible|
    ansible.config_file = "ansible/ansible.cfg"
    ansible.playbook = "ansible/playbook.yml"
  end
end
