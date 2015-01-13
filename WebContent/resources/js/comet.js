function comet() { 
	$.ajax({
	   url: 'http://192.168.0.140:8080/CometExample/comet',
	   error: function() {
	      comet();
	   },
	   type: 'GET',
	   cache: false,
	   success: function(msg) {
		   $('#mensagens').append(msg);
		   comet();
	   },
	});
}
function send() {
	var mensagem = $('#mensagem').val();
	$.ajax({
	   url: 'http://192.168.0.140:8080/CometExample/comet',
	   error: function() {
	      comet();
	   },
	   data: {'mensagem' : mensagem},
	   type: 'POST',
	   cache: false
	   
	});
}