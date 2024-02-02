#!/usr/bin/bash

sudo apt update
sudo apt install net-tools
sudo apt install default-jdk
sudo apt install maven
sudo ifconfig enp0s3 192.168.1.50/24 up
sudo systemctl restart NetworkManager
cd client
mvn compile exec:java
