# Sheephole
(noun):
- a small, boulder-strewn mountain range near Joshua Tree.
- an application to install Drupal modules the right way (using composer) without having to use the command line.

[![sheephole video](https://img.youtube.com/vi/ylowkvirpzc/0.jpg)](https://www.youtube.com/watch?v=ylowkvirpzc)

To run:
* Download the latest .zip file from the Releases page.
* Extract the .zip file to any convenient location.
* Ensure that you have Java 17 installed on your system.
* Either double click the .jar file, or right click it and choose 'Open with Java 17'. On Unix-like systems you might need to set the executable bit (right click and choose 'Permissions').

To use, first create a profile on the Profile menu. The fields are:
* Title: any name you want, this is just used to identify the profile
* Username: your SSH username. You can get this and the other parameters from your host.
* Password: your SSH password.
* URI: the IP address or full domain name of your server. Do not include protocols like 'http' or any slashes.
* Directory: the file location where your site is installed. `composer.json` should be in this directory.

Saving the profile will test if the settings are correct. If there are any issues, please ask your hosting company for assistance.

Then, on the Commands menu, choose Install. On the resulting screen, choose one of your profiles, reenter your SSH password, and start typing in the name of the module you want to install.

After selecting the module, press the Install button and it should be installed on your system.

Note that the SSH password is not saved to the database.

Developers who have maven installed can compile from source using `mvn clean compile exec:java`.

Note: The Sheephole application (the "Software") is provided on an as-is basis. Chris Kelly hereby disclaims all warranties of any kind, express or implied, including, without limitation,
the warranties of merchantability, fitness for a particular purpose and non-infringement. Chris Kelly makes no warranty that the Software will be error free.
You understand that you use the Software at your own discretion and risk.

Note 2: Due to Maven not picking it up from Github, the KittyCache classes have been put in the spaceprogram package inside the source. Those are covered by the Apache License 2.0 (same as the rest of the code). The original location of those classes are at code.google.com/archive/p/kitty-cache.
