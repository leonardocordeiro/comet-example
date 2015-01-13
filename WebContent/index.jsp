<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<fieldset>
	<legend>Mensagens</legend>
	<div id="mensagens">
	
	</div>
	
</fieldset>
<br>
<input type="text" id="mensagem">
<input type="button" value="Enviar" onclick="send()"> 
	
<script src="resources/js/comet.js"></script>
<script src="resources/js/jquery.js"></script>
<script>
	window.onload = comet();
</script>
</body>
</html>