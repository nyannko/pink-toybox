## XJBchat

A simple C/S chat app made by Java-Websocket and Java swing.

### 1.TODO

#### High priority

* allow user registration(both backend and GUI)
* chat room
* allow sending images
* allow pressing shift+enter to add a new line
* fix timestamps for client and server(send and recv time)
* server cache for offline clients
* improve user random name generator
* add find/add friend
* end to end chat
* wss
* write tests

#### Low priority

* add database 
* add frontend
* AUI

---
### 2.DONE

* get online list(backend)
* logic for send/connect/close button
* connect two or more clients together
* set different text colors for sender and receiver
* append local timestamp to each message
* wrap up messages, convert them between String and JSONObject
* use constants for client/server-side message demultiplexing
* broadcast logic(server forward)

02.07.2019

* use log but not print (change pom.xml. Too many loggers disperse everywhere, no color for output text)
* add eventqueue for GUI
* get online list(GUI): `on_message()` and `close()`
* fix multiplexing string constants

03.07.2019

* refine logic for send/connect/close
* send images from server to client GUI (file -> byte[] -> byteBuffer -> byte[] -> file -> show on GUI) 

---
### 3.Configuration shit

* [DO NOT add idea to git](https://stackoverflow.com/questions/11124053/accidentally-committed-idea-directory-files-into-git)
* [run two instances/compound in intellij](https://stackoverflow.com/a/35753820)
* [export jar file](https://stackoverflow.com/questions/9689793/cant-execute-jar-file-no-main-manifest-attribute)
* [JSONObject usage](https://www.testingexcellence.com/how-to-parse-json-in-java/)
* [maven change the default project language level](https://stackoverflow.com/questions/27037657/stop-intellij-idea-to-switch-java-language-level-every-time-the-pom-is-reloaded)


* [invalid source release: 8 in Intellij. What does it mean?](https://stackoverflow.com/a/26009627)
* [SLF4J: Failed to load class “org.slf4j.impl.StaticLoggerBinder”](https://stackoverflow.com/a/50606584)
* [SLF4J: enable debug level](https://stackoverflow.com/questions/30555432/logger-slf4j-is-not-using-the-logback-configured-level)
* [Debug in Java](https://softwareengineering.stackexchange.com/a/176081)
* [Loggers for multiple classes](https://stackoverflow.com/questions/7624895/how-to-use-log4j-with-multiple-classes)

The logger has no color after configuration, shit.

```java
// test logger
// logger.trace("Hello World");
// logger.debug("Hello World");
// logger.info("Hello World");
// logger.warn("Hello World");
// logger.error("Hello World");
```

### 4.Current state

02.07.2019

<img src="https://raw.githubusercontent.com/nyannko/pink-toybox/master/pic/xjbchat001.gif">
