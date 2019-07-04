// MessagePrefix
const REGISTER = "register";
const BROADCAST = "broadcast";
const UPDATE_NEW_CLIENT = "updateNewClient";
const REMOVE_OFFLINE_CLIENT = "removeOfflineClient";

const PREFIX = "prefix";
const NICKNAME = "nickname";
const MESSAGE = "message";
const ONLINE_CLIENT = "onlineClient";
const OFFLINE_CLIENT = "offlineClient";
const ONLINE_LIST = "onlineList";

var nickname;

window.onload = function() {
    // Get references to elements on the page.
    var form = document.getElementById('message-form');
    var messageField = document.getElementById('message');
    var messagesList = document.getElementById('messages');
    var connectBtn = document.getElementById("connect");
    var socketStatus = document.getElementById('status');
    var closeBtn = document.getElementById('close');
    var onlineList = document.getElementById('onlinelist');
    var usernum = document.getElementById('num');
    var userlist = []


    var socket = new WebSocket("ws://localhost:8877");
    
    socket.onopen = function(event) {
        socketStatus.innerHTML = 'Connected to: ' + event.currentTarget.url;
        socketStatus.className = 'open';
    };

    // Handle any errors that occur.
    socket.onerror = function(error) {
        console.log('WebSocket Error: ' + error);
    };

    form.onsubmit = function(e) {
        e.preventDefault();

        // Retrieve the message from the textarea.
        var messageBody = messageField.value;
        console.log("nickname...", nickname);
        var obj = { prefix: BROADCAST, nickname: nickname , message: messageBody};
       
        // pack to json
        var message = JSON.stringify(obj) 

        // Send the message through the WebSocket.
        socket.send(message);

        // Add the message to the messages list.
        messagesList.innerHTML += "<div class=\"sent\"><span>" + nickname + ": </span>" + messageBody +
                                    "</div>";

        // Clear out the message field.
        messageField.value = "";

        return false;
    }

    socket.onmessage = function(event) {
        var message = event.data;
        if (message instanceof Blob) { // get image
            var image = document.getElementById('image');
            
            // https://stackoverflow.com/a/18650249
            var reader = new FileReader();
            reader.readAsDataURL(message);  // why this is a url
            reader.onloadend = function() {
                image.src = reader.result;
            }
        
        } else if (typeof message === "string") { // get strings
            var msgObj = JSON.parse(message);
            var msgPrefix = msgObj.prefix;
            switch(msgPrefix) {
                case REGISTER:
                    handleRegister(msgObj);
                    break;
                case BROADCAST:
                    handleBroadcast(msgObj);
                    break;
                case UPDATE_NEW_CLIENT:
                    handleUpdateNewClient(msgObj)
                    break;
                case REMOVE_OFFLINE_CLIENT:
                    handleRemoveOfflineClient(msgObj);
                    break;
            }
        }
    }

    function handleUpdateNewClient(msgObj) {
        // add to li
        console.log(msgObj);

        userlist.remove()
        onlineList.innerHTML += "<li>" + msgObj.onlineClient + "</li>"
        num = usernum.innerHTML;
        num++;
        console.log("number", num);
        usernum.innerHTML = num;
    }

    function handleRemoveOfflineClient(msgObj) {
        // todo: remove li

    }

    function handleRegister(msgObj) {
        // store the client nick name
        nickname = msgObj.nickname;
        userlist = msgObj.onlineList;
        userlist.forEach(addusers);
        
        console.log(msgObj, userlist);
        socketStatus.innerHTML = 'Hi ' + nickname + ', You are Connected to: ' + event.currentTarget.url; 
        usernum.innerHTML = userlist.length;
      }

    function addusers(user) {
        onlineList.innerHTML += "<li>" + user + "</li>"
    }
    function handleBroadcast(msgObj) {
        var nickname = msgObj.nickname;
        var message = msgObj.message;
        messagesList.innerHTML += "<div class=\"received\"><span>" + nickname  +": </span>" + message + "</div>";
    }

    socket.onclose = function(event) {
        onlineList.innerHTML = "";
        socketStatus.innerHTML = "Disconnected from websocket";
        socketStatus.className = "closed";
    }

    closeBtn.onclick = function(e) {
        console.log("click closed button");
        e.preventDefault();
        socket.close();
        return false;
    }
};