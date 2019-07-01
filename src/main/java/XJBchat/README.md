## XJBchat

A simple C/S chatapp made by Java-Websocket and Java swing.

### 1.TODO

#### High priority

* allow user register(both backend and GUI)
* allow pressing shift+enter to add a new line
* get online list(both backend and GUI)
* wrap up components
* fix timestamp for client and server(send and recv time)
* use log but not print 
* allow sending images
* server cache for offline clients
* improve user random name generator
* add find/add friend
* end to end chat
* wss

#### Low priority

* add database 
* add frontend

---
### 2.DONE

* logic for send/connect/close button
* connect two or more clients together
* set different text colors for sender and receiver
* append local timestamp to each message
* wrap up messages, convert them between String and JSONObject
* use constants for client/server-side message demultiplexing
* broadcast logic(server forward)

---
### 3.Configuration shit

* [DO NOT add idea to git](https://stackoverflow.com/questions/11124053/accidentally-committed-idea-directory-files-into
-git)
* [run two instances/compound in intellij](https://stackoverflow.com/a/35753820)
* [export jar file](https://stackoverflow.com/questions/9689793/cant-execute-jar-file-no-main-manifest-attribute)
