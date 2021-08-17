terraform {
  backend "gcs" {
    bucket  = "laundry-dev-tf-state"
    prefix  = "terraform/state"
  }
}

provider "google" {
  project = "hanki-2361-laundry-dev"
  region  = "europe-north1"
  zone    = "europe-north1-a"
}

resource "google_compute_network" "vpc_network" {
  name                    = "terraform-network"
  auto_create_subnetworks = "true"
}

resource "google_compute_region_backend_service" "default" {
  load_balancing_scheme = "INTERNAL_MANAGED"

  backend {
    group          = google_compute_region_instance_group_manager.rigm.instance_group
    balancing_mode = "UTILIZATION"
    capacity_scaler = 1.0
  }

  name        = "laundry-dev-service"
  protocol    = "HTTP"
  timeout_sec = 10

  health_checks = [google_compute_region_health_check.default.id]
}

resource "google_compute_region_health_check" "default" {
  name   = "rbs-health-check"
  http_health_check {
    port_specification = "USE_SERVING_PORT"
  }
}

resource "google_compute_region_instance_group_manager" "rigm" {
  name     = "rbs-rigm"
  version {
    instance_template = google_compute_instance_template.instance_template.id
    name              = "primary"
  }
  base_instance_name = "internal-glb"
  target_size        = 1
}

data "google_compute_image" "debian_image" {
  family   = "debian-9"
  project  = "debian-cloud"
}

resource "google_compute_instance_template" "instance_template" {
  name         = "template-region-service"
  machine_type = "e2-medium"

    network_interface {
    network    = "default"
  }

  disk {
    source_image = data.google_compute_image.debian_image.self_link
    auto_delete  = true
    boot         = true
  }

  tags = ["allow-ssh", "load-balanced-backend"]
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
