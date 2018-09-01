<?php
$colors = array('#007AFF','#FF7000','#FF7000','#15E25F','#CFC700','#CFC700','#CF1100','#CF00BE','#F00');
$color_pick = array_rand($colors);
?>

<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<style type="text/css">


.chat-wrapper
{
	font: bold 11px/normal 'lucida grande', tahoma, verdana, arial, sans-serif;
    margin: auto;
		border-left-style: solid;
		border-left-width: 50px;
		border-left-color: #00122e;
		border-top-style: none;
		border-right-style: none;
		border-bottom-style: none;
	width:auto;
	height:auto;
}
#message-box
{
    width: 97%;
    display: inline-block;
    height: 800px;
    background: #cecece;
    box-shadow: inset 0px 0px 2px #00000017;
    overflow: auto;
    padding: 10px;
		font-size: 20px;
		font-style: italic;
}
.user-panel
{
		background: #0a1c5c;
		padding: 15px;
		width: 96.5%;
}
input[type=text]
{
    border: none;
    padding: 5px 5px;
    box-shadow: 2px 2px 2px #0000001c;
		height: 35px
}
input[type=text]#name
{
    width:20%;
}
input[type=text]#message
{
    width:60%;
}
button#send-message
 {
    border: outset;
    padding: 5px 15px;
    background: #0149e3;
		border-color: red;
    box-shadow: 2px 2px 2px #0000001c;
		width:18%;
		height: 45px
}
</style>
</head>
<body>

<div class="chat-wrapper">
<div id="message-box"></div>
<div class="user-panel">
<input type="text" name="name" id="name" placeholder="NAME HERE" maxlength="15" />
<input type="text" name="message" id="message" maxlength="100" />
<button id="send-message">SEND</button>
</div>
</div>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<script language="javascript" type="text/javascript">


	var msgBox = $('#message-box');
	var wsUri = "ws://localhost:5555/demo/server.php";
	websocket = new WebSocket(wsUri);

	websocket.onopen = function(ev)
	{
		msgBox.append('<div class="system_msg" style="color:#bbbbbb"></div>');
	}

	websocket.onmessage = function(ev) {
		var response 		= JSON.parse(ev.data);

		var res_type 		= response.type;
		var user_message 	= response.message;
		var user_name 		= response.name;
		var user_color 		= response.color;

		switch(res_type){
			case 'usermsg':
				msgBox.append('<div><span class="user_name" style="color:' + user_color + '">' + user_name + '</span> : <span class="user_message">' + user_message + '</span></div>');
				break;
			case 'system':
				msgBox.append('<div style="color:#bbbbbb">' + user_message + '</div>');
				break;
		}
		msgBox[0].scrollTop = msgBox[0].scrollHeight;

	};

	websocket.onerror	= function(ev){ msgBox.append('<div class="system_error">Error Occurred - ' + ev.data + '</div>'); };
	websocket.onclose 	= function(ev){ msgBox.append('<div class="system_msg">Connection Closed</div>'); };


	$('#send-message').click(function(){
		send_message();
	});


	$( "#message" ).on( "keydown", function( event ) {
	  if(event.which==13){
		  send_message();
	  }
	});


	function send_message()
	{
		var message_input = $('#message');
		var name_input = $('#name');

		if(message_input.val() == "")
		{
			alert("Enter your Name please!");
			return;
		}
		if(message_input.val() == "")
		{
			alert("Enter Some message Please!");
			return;
		}


		var msg =
		{
			message: message_input.val(),
			name: name_input.val(),
			color : '<?php echo $colors[$color_pick]; ?>'
		};

		websocket.send(JSON.stringify(msg));
		message_input.val('');
	}
</script>
</body>
</html>
