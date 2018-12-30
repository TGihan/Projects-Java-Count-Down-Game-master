# Count-Down-Game-Java

3rd Year Final Project - Software Engineering<br>
NSBM Green University<br>
University College Dublin Ireland


How to setup this game

####What you’ll need:

* About 10 minutes. watch “How to setup” and “Game Demo” videos on Youtube <br>
* XAMPP or any MySQL Database (Used XAMPP Version 3.2.2)<br>
* Strong Internet connection<br>
* NetBeans IDE 8.0 (only for code view and GUI design view)<br>
* JDK 1.7 or above version<br>

####Video Links:

How to setup - https://www.youtube.com/watch?v=MUw6aDU8Ngw<br>
Game Demo – https://www.youtube.com/watch?v=RcnRSBRLs_c<br>
<br>

####1.0 Server configuration:

NOTE: Should be connected to the internet

1. Extract zip file<br>
2. Open MySQL (PhpMyAdmin or workbench)<br>
3. Open “SourceCode” Folder<br>
4. Find “flogmaster.sql” and import or execute it’s queries in database<br>
   When it successfully executed, it creates “flogmaster” database with a table called “playboard”.<br>
5. Open “config.properties” file.<br>
* If you want to setup your machine as server machine.<br>
Set SERVER_IP value to your computer IP<br>
No need to change port<br>
DB_HOSTNAME value to your computer IP<br>
When execute “flogmaster.sql” it creates a User “spacex” with password “spacex”, so no need<br>
to change DB_NAME, DB_USER_NAME and DB_PASSWORD.<br>
6. Server configuration completed<br>
7. Copy this “config.properties” file and go to FlogServer > target folder and paste (replace)<br>
8. Go to target folder and double click jar file and click green color start button to start server<br>
<br>

####2.0 Players configuration –

* Another machine that can access above server machine through network<br>

1. Open “SourceCode” folder<br>
2. Copy above changed config file<br>
3. Copy and go to FlogGame > target folder and paste (replace)<br>
4. Player configuration completed<br>
5. Double click jar file to start main screen of the game.<br>
