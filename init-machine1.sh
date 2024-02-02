#!/usr/bin/bash

sudo apt update
sudo apt install net-tools
sudo ifconfig enp0s8 192.168.0.10/24 up
sudo ifconfig enp0s9 192.168.1.10/24 up
sudo systemctl restart NetworkManager
sudo apt install default-jdk
sudo apt install maven
sudo apt-get install openssh-server
cd server
mvn compile exec:java
