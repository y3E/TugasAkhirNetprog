<?php

$host = "localhost";
$port = '5555';

#Create Socket
$socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
#Reusable Port
socket_set_option($socket, SOL_SOCKET, SO_REUSEADDR, 1);
#Bind
socket_bind($socket, 0, $port);
#Listen
socket_listen($socket);

#Create an array to list all the connected clients
$clients = array($socket);

while(1)
{
  #Create a copy for modyfying withou affecting the real one
  $c_copy = $clients;
  #Get Client data
  $write  = NULL;
  $except = NULL;
  socket_select($c_copy,$write,$except, 0, 10);

  #Accepting
  if(in_array($socket,$c_copy))
  {
    #Check and accept if there is a client trying to connect
    $newacc    = socket_accept($socket);
    $clients[] = $newacc;

    $header = socket_read($newacc, 1024);
		perform_handshaking($header, $newacc, $host, $port);

    #Inform for a new connection
    socket_getpeername($newacc, $ip, $port);
    $message = mask(json_encode(array('type'=>'system', 'message'=>'')));
		send_message($message);

    #Create room for new socket
    $found_socket = array_search($socket, $c_copy);
		unset($c_copy[$found_socket]);
  }

  #Loop to Read data from client list
  foreach($c_copy as $read)
  {

    while(socket_recv($read, $data, 1024, 0) >= 1)
		{
			$received_text = unmask($data);
			$tst_msg = json_decode($received_text, true);
			$user_name = $tst_msg['name'];
			$user_message = $tst_msg['message'];
			$user_color = $tst_msg['color'];

			#Send message to clients
			$response = mask(json_encode(array('type'=>'usermsg', 'name'=>$user_name, 'message'=>$user_message, 'color'=>$user_color)));
			send_message($response);
			break 2;
		}

    #Read
    $data = @socket_read($read, 4096, PHP_BINARY_READ);
    #Remove disconnected clients
    if($data === false)
    {
      $disconnected = array_search($read, $clients);
      unset($clients[$disconnected]);
      #Skip to next Loop
      echo "Client Disconnected";
      continue;
    }



    #Trim to clear whitespaces
    /*$data = trim($data);

    if(!empty($data))
    {
      #Broadcast message to all clients
      foreach($clients as $socket_send)
      {
        #Validation for not sending teh message to the sender
        if($socket_send == $socket)
        {
          continue;
        }
        #Write message
        socket_write($socket_send, $data);
      }
    }*/
  }
}

#Close Socket
socket_close($socket);

#Broadcast messages
function send_message($msg)
{
	global $clients;
	foreach($clients as $copyS)
	{
		@socket_write($copyS,$msg,strlen($msg));
	}
	return true;
}

function perform_handshaking($receved_header,$client_conn, $host, $port)
{
	$headers = array();
	$lines = preg_split("/\r\n/", $receved_header);
	foreach($lines as $line)
	{
		$line = chop($line);
		if(preg_match('/\A(\S+): (.*)\z/', $line, $matches))
		{
			$headers[$matches[1]] = $matches[2];
		}
	}

	$secKey = $headers['Sec-WebSocket-Key'];
	$secAccept = base64_encode(pack('H*', sha1($secKey . '258EAFA5-E914-47DA-95CA-C5AB0DC85B11')));
	//hand shaking header
	$upgrade  = "HTTP/1.1 101 Web Socket Protocol Handshake\r\n" .
	"Upgrade: websocket\r\n" .
	"Connection: Upgrade\r\n" .
	"WebSocket-Origin: $host\r\n" .
	"WebSocket-Location: ws://$host:$port/demo/shout.php\r\n".
	"Sec-WebSocket-Accept:$secAccept\r\n\r\n";
	socket_write($client_conn,$upgrade,strlen($upgrade));
}

function mask($text)
{
	$b1 = 0x80 | (0x1 & 0x0f);
	$length = strlen($text);

	if($length <= 125)
		$header = pack('CC', $b1, $length);
	elseif($length > 125 && $length < 65536)
		$header = pack('CCn', $b1, 126, $length);
	elseif($length >= 65536)
		$header = pack('CCNN', $b1, 127, $length);
	return $header.$text;
}

function unmask($text) {
	$length = ord($text[1]) & 127;
	if($length == 126) {
		$masks = substr($text, 4, 4);
		$data = substr($text, 8);
	}
	elseif($length == 127) {
		$masks = substr($text, 10, 4);
		$data = substr($text, 14);
	}
	else {
		$masks = substr($text, 2, 4);
		$data = substr($text, 6);
	}
	$text = "";
	for ($i = 0; $i < strlen($data); ++$i) {
		$text .= $data[$i] ^ $masks[$i%4];
	}
	return $text;
}
?>
