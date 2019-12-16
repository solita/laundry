Vagrant.configure("2") do |config|
  config.vm.box = "centos/8"

  config.vm.network "forwarded_port", guest: 8080, host: 8080, auto_correct: true

  config.vm.provision "ansible" do |ansible|
    ansible.config_file = "ansible/ansible.cfg"
    ansible.playbook = "ansible/build.yml"
    ansible.raw_arguments = ['-i', 'localhost,']
    ansible.limit = "all"
  end

  config.vm.provision "ansible" do |ansible|
    ansible.config_file = "ansible/ansible.cfg"
    # change the default galaxy_command to remove --force as it downloads roles always
    ansible.galaxy_command = "ansible-galaxy install --role-file=%{role_file} --roles-path=%{roles_path}"
    ansible.galaxy_role_file = "ansible/dependencies.yml"
    ansible.galaxy_roles_path = "ansible/roles"
    ansible.playbook = "ansible/playbook.yml"
  end
end
