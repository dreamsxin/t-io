var ws;
function initWs() {
    ws = new WebSocket("ws://localhost:9321?name=科比&name=科比&name=库里");
    ws.onmessage = function (event) {
        document.getElementById("contentId").value += (event.data + "\r\n");
    };
    ws.onclose = function (event) {

    };
    ws.onopen = function (event) {
        ws.send("hello tio remote");
    };
    ws.onerror = function (event) {

    };
}
function send() {
    var msg = document.getElementById("textId");
    //alert(msg.value);
    ws.send(msg.value);
}