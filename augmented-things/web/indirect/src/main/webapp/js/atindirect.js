// target servlets
const SERVLET = "webpage";
const TESTER = "test";

// utility globals
const mnReq = "{\"command\":\"mns\"}";
var mainInfo = "generalmap-info";


function changeText(elem) { 
	// using jquery: . takes classes, # takes ids, nothing takes tags
	if (bool == false)
		$("\#generalmap-btn").html("Issued request!");
	else
		$("\#generalmap-btn").html("Issue general map to IN");
	bool = !bool;
	// using DOM js
//	elem.innerHTML = "Ooops!";  
}

function upDate(idelem) {
	var d = new Date();
	$("\#"+idelem).html("Last modified<br>"+d.getDate()+"/"+(d.getMonth()+1)+"/"+d.getFullYear()+" "+d.getHours()+":"+d.getMinutes());
}

// used for any unactive button
function showError(elem, errormsg) {
	elem.innerHTML = errormsg;
}

// Just test to display. Literally, only the button value changes, so that the java client can operate on it
function loadMap(elem, reflist) { // tag element + string
	ajaxReq(
			mnReq,
			"application/json",
			function(reply) {
				showMN(reply, reflist);
			},
			function(reply) {
				hideMN(reply,reflist);
			}
			
	);
//	
//	if (elem.value == "false") {
//		elem.value = "true";
//		passenger = reflist;
//		ajaxReq(
//			    mnReq,
//			    "json",
//			    getSuccess,
//			    getError
//			 );
//	}
//	else {
//		elem.value = "false";
//		document.getElementById(reflist).style.display = "none";
//	}
}

function showMN(reply, reflist) {
	if(reply != null) {
        // all went well, prepare the page: the object that I received 
		// is meant to be exactly a json object.
		document.getElementById(reflist).style.display = "block";
		$("\#"+mainInfo).html("Connection to the IN is successful. See below middle-nodes.");
	}
    else {
    	console.log(err);
        window.alert("Ajax wrong response\n"+JSON.stringify(reply));
    }
}

function hideMN(reply, reflist) {
	document.getElementById(reflist).style.display = "block";
	$("\#"+mainInfo).html("Connection failed. Click above to show nodes connected to the cloud.");
}

// "View" button of each middle node
function loadNodes(target, whichMN) {	
	var nodesReq = "{\"command\":\"nodes\", \"mn\":\""+whichMN+"\"}";
	ajaxReq(
			nodesReq,
			"application/json",
			function(reply) {
				if (reply.hasOwnProperty("error")) 
					getError(reply);
				else
					prepareNDiv(reply, target);
			}, // create object inside node div passed
			function (reply) {
				Nerr(reply, target);
			}
	);	
}

// "Users" button of each middle node
function loadUsers(target, whichMN) {
	var usersReq = "{\"command\":\"users\", \"mn\":\""+whichMN+"\"}";
	ajaxReq(
			usersReq,
			"application/json",
			function (reply) {
				if (reply.hasOwnProperty("error")) 
					getError(reply);
				else
					prepareUDiv(reply, target);
			},
			function(reply) {
				Uerr(reply, target);
			}
	);
}

function Nerr(reply, target) {
	console.log("calledNerr");
	var parent = document.getElementById(target);
	var child = document.getElementById(target+"-ul");
	if (child!=null)
		parent.removeChild(child);
	
	// check that the node is registered
	var json = JSON.parse(JSON.stringify(reply)); 
	var p = document.createElement("p");
	p.setAttribute("id", target+"-ul");
	p.setAttribute("class", "small red-text");
	p.appendChild(document.createTextNode(json.responseText));
	parent.appendChild(p);
}

function Uerr(reply, target) {
	console.log("calledUErr");
	var parent = document.getElementById(target);
	var child = document.getElementById(target+"-ul");
	if (child!=null)
		parent.removeChild(child);
	
	// check that the node is registered
	var json = JSON.parse(JSON.stringify(reply)); 
	var p = document.createElement("p");
	p.setAttribute("id", target+"-ul");
	p.setAttribute("class", "small red-text");
	p.appendChild(document.createTextNode(json.responseText));
	parent.appendChild(p);
}

// list visualization
function prepareUDiv(reply, target) {
	console.log("calledUdiv");
	// by first, delete existing ones
	var parent = document.getElementById(target);
	var child = document.getElementById(target+"-ul");
	if (child != null)
		parent.removeChild(child);
//	console.log(target);
	if (reply != null) {
		console.log("Full object:")
		console.log(JSON.stringify(reply));
		if (JSON.stringify(reply) === "{}") {
			// empty
			var p = document.createElement("p");
			p.setAttribute("id", target+"-ul");
			p.setAttribute("class", "small red-text");
			p.appendChild(document.createTextNode("No users connected."));
			parent.appendChild(p);
			return;
		}
		var ul1 = document.createElement("ul");
		ul1.setAttribute("class", "orange-text");
		ul1.setAttribute("id", target+"-ul");
		var li1 = document.createElement("li");
		li1.appendChild(document.createTextNode("USERS:"));
		ul1.appendChild(li1);
		for (var i in reply.users) { // 2 mn
//			console.log("first inner object: still an array");
//			console.log(reply.nodes[i]);			
//			ul2.appendChild(document.createElement("li").appendChild(document.createTextNode("("+i+")")));
			var ul2 = document.createElement("ul");
			var li2 = document.createElement("li");
			li2.appendChild(document.createTextNode("User "+i));
			ul2.appendChild(li2);
			var ul3 = document.createElement("ul");
			ul2.appendChild(ul3);
			for (var j in reply.users[i]) {
//				console.log(j); // already a string! it's an object now
				var li = document.createElement("li"); // primo mn: un ul
				li.appendChild(document.createTextNode(j+": "+reply.users[i][j]));
				ul3.appendChild(li);
			}
			ul1.appendChild(ul2);
		}
		document.getElementById(target).appendChild(ul1);
	}
}

	
function prepareNDiv(reply, target) {
	console.log("calledNdiv");
	// by first, delete existing ones
	var parent = document.getElementById(target);
	var child = document.getElementById(target+"-ul");
	if (child != null)
		parent.removeChild(child);
//	console.log(target);
	if (reply != null) {
//		console.log("Full object:")
//		console.log(reply);
		if (JSON.stringify(reply) === "{}") {
			// empty
			var p = document.createElement("p");
			p.setAttribute("id", target+"-ul");
			p.setAttribute("class", "small red-text");
			p.appendChild(document.createTextNode("No nodes connected."));
			parent.appendChild(p);
			return;
		}
		var ul1 = document.createElement("ul");
		ul1.setAttribute("class", "blue-text");
		ul1.setAttribute("id", target+"-ul");
		var li1 = document.createElement("li");
		li1.appendChild(document.createTextNode("NODES:"));
		ul1.appendChild(li1);
		for (var i in reply.nodes) { // 2 mn
//			console.log("first inner object: still an array");
//			console.log(reply.nodes[i]);			
//			ul2.appendChild(document.createElement("li").appendChild(document.createTextNode("("+i+")")));
			var ul2 = document.createElement("ul");
			var li2 = document.createElement("li");
			li2.appendChild(document.createTextNode("Node "+i));
			ul2.appendChild(li2);
			var ul3 = document.createElement("ul");
			ul2.appendChild(ul3);
			for (var j in reply.nodes[i]) {
//				console.log(j); // already a string! it's an object now
				var li = document.createElement("li"); // primo mn: un ul
				li.appendChild(document.createTextNode(j+": "+reply.nodes[i][j]));
				ul3.appendChild(li);
			}
			ul1.appendChild(ul2);
		}
		document.getElementById(target).appendChild(ul1);
	}
//	var para = document.createElement("p");
//	var node = document.createTextNode("This is new.");
//	para.appendChild(node);
//	<ul class="text-muted">
//	<li>Sensor</li>
//	<ul> 
//		<li>name: heat sensor</li>
//		<li>tagID: #0A15</li>
//		<li>value: 15C°</li>
//		<li>connected: none</li> <!-- some way to set undefined value -->
//	</ul>
//	<li>Sensor</li>
//	<ul> 
//		<li>name: humidity sensor</li>
//		<li>tagID: #0A3F</li>
//		<li>value: 15C°</li>
//		<li>connected: #1A22</li> 
//	</ul>
//	<li>Actuator</li>
//	<ul>
//		<li>name: watercan</li>
//		<li>tagID: #1A22</li>
//		<li>value: off</li>
//		<li>connected: #0A3F</li>
//	</ul>
//<ul>                  	
}

//-------------------------------------
//UTILITY
//-------------------------------------

//Perform an AJAX request
function ajaxReq(info, type, succ, err) {
 $.ajax({
     type: "POST",
     url: SERVLET,
     data: info,
     contentType: type,				
     dataType: "json", 				
     success: succ,
     error: err
 });
}

// response
//function getSuccess(reply) {
//	if(reply != null) {
//        // all went well, prepare the page: the object that I received 
//		// is meant to be exactly a json object.
//		document.getElementById(passenger).style.display = "block";
//		var json = reply;
//		console.log(json);
//	}
//    else {
//    	console.log(err);
//        window.alert("Ajax wrong response");
//    }
//    
//}

function getError(err) {
	console.log(err);
	window.alert("Error "+JSON.stringify(err));
}

