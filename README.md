# Sheephole
(noun):
- a small, boulder-strewn mountain range near Joshua Tree.
- an application to install Drupal modules the right way (using composer) without having to use the command line.

To run, first ensure that you have Java 17 installed on your system. Then, either double click the .jar file, or ensure you have maven installed and then open a command line and type `mvn clean compile exec:java`.

To use, first create a profile on the Profile menu. The fields are:
* Title: any name you want, this is just used to identify the profile
* Username: your SSH username. You can get this and the other parameters from your host.
* Password: your SSH password.
* URI: the IP address or full domain name of your server. Do not include protocols like 'http'.
* Directory: the file location where your site is installed. `composer.json` should be in this directory.

Then, on the Commands menu, choose Install. On the resulting screen, choose one of your profiles, reenter your SSH password, and start typing in the name of the module you want to install.

After selecting the module, press the Install button and it should be installed on your system.

Note that the SSH password is not saved to the database.

Also, note that the Sheephole application (the "Software") is provided on an as-is basis. Chris Kelly hereby disclaims all warranties of any kind, express or implied, including, without limitation,
the warranties of merchantability, fitness for a particular purpose and non-infringement. Chris Kelly makes no warranty that the Software will be error free.
You understand that you use the Software at your own discretion and risk.
