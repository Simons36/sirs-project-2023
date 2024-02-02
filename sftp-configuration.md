# SFTP Configuration

Audio files will be transfered via **SFTP** to be stored in the database server's filesystem. This guide will illustrate the steps needed to setup the **ssh connection** required for **SFTP**.

>_**NOTE:**_<br>- Application server ip: **192.168.0.10**
<br>- Database server ip: **192.168.0.100**
<br> - Machines username: **groove-admin**

## _PREREQUISITES_

* Must have installed **openssh server** in database machine:
  ```shell
  sudo apt-get update
  sudo apt-get install openssh-server
  ```

## _SSH CONFIGURATION_

1. In the **application server**, open the terminal and type the command:

    ```shell
    ssh-keygen -t rsa  
    ```

    * You will be asked for a **filename** and a **passphrase**:


      * For **filename** you can choose the path you want your keys to be stored at, or you can just hit enter for default path (~/.ssh/)

      * Enter a valid passphrase and **save it**

2. We will now need to transfer the **public key** we have just created from the **application server** to the **database server** using **scp** command

    ```shell
    scp ~/.ssh/id_rsa.pub groove-admin@192.168.0.100:
    # Assuming you saved the keys in the default path
    ```
    
    * You will be prompted the password of the account you are trying to login at the database server

3. Now log in to **database server**

4. Open terminal and enter the following commands:

    ```shell
    cd ~
    mkdir -p .ssh
    touch .ssh/authorized_keys
    cat ~/id_rsa.pub >> ~/.ssh/authorized_keys
    ```
5. You are now able to connect via ssh to the **database server**! Go back to the **application server** and type in the terminal:

   ```shell
   ssh 192.168.0.100
   ```

   * You will then be prompted to insert the **passphrase** you defined earlier.