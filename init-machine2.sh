sudo apt install net-tools
sudo ifconfig enp0s3 192.168.0.100/24 up
sudo systemctl restart NetworkManager
sudo wget https://dev.mysql.com/get/mysql-apt-config_0.8.29-1_all.deb
sudo dpkg -i mysql-apt-config_0.8.29-1_all.deb
sudo apt-get update
sudo apt-get install mysql-server
mysql -u root -p

# Now you will need to provide the password you gave during installation
# then, you need to grant acces to the database to the user @ 192.168.0.10
# In the mysql console, put the following commands: 
# mysql> CREATE USER 'admin'@'192.168.0.10' IDENTIFIED BY 'admin123';
# mysql> GRANT ALL ON *.* TO 'admin'@'192.168.0.10' WITH GRANT OPTION;
# CREATE DATABASE test_groove;


