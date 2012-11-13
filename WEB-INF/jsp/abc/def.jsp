<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/decorators/taglib.jsp" %>
<%
	String x = "大家好";
%>
<body>
   	<script>
		$(document).ready(function(){
		
			$("#ajaxFormAction").click(function(){
 				var url = '<%=request.getContextPath()%>/formAction/ajaxCamel.do';
				 
				 $.ajax({
				 	type :'post',
				 	url: url,
				 	data: {name:'<%=x%>'},
				 	success: function(data) {
    					$.each(data,function(index,val){
				 			$('#console').html(val.col2);
				 		});
	 				}
				 });
			});
 		});
   	</script>
<table>
	<tr>
		<td>col1</td><td>col2</td><td>col3</td>
	</tr>
	<c:forEach var="camel" items="${camelList}">
		<tr>
			<td>${camel.col1}</td>
			<td>${camel.col2}</td>
			<td>
				<fmt:formatNumber value="${camel.col3}" type="currency"/>   
			</td>
		</tr>
	</c:forEach>
</table>
		<input type="button" id="ajaxFormAction" value="Ajax formAction" /> 
		<div id="console"></div>
</body>