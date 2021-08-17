provider "google" {
  project = "hanki-2361-laundry-dev"
  region  = "europe-north1"
  zone    = "europe-north1-a"
}

resource "google_compute_instance_from_template" "tpl" {
  name = "instance-from-template"
  source_instance_template = "laundry-appserver-2021-01-15v1"

  // Override fields from instance template
  can_ip_forward = false
  labels = {
    my_key = "my_value"
  }
}

resource "google_compute_network" "vpc_network" {
  name                    = "terraform-network"
  auto_create_subnetworks = "true"
}


