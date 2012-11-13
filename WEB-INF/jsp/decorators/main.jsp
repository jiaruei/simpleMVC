<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/decorators/taglib.jsp" %>
<html>
	<head>
    	<title><decorator:title default="Welcome!" /></title>
    	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/main.css" />
	    <decorator:head />
    </head>
    <body>
   	<script src="<%=request.getContextPath()%>/js/jquery-1.8.2.min.js"></script>
    <div id="container">
    	<div id="header">
    		<div id="navigate">
    			<%@ include file="/WEB-INF/jsp/decorators/navigate.jsp"%>
    		</div>
    	</div>
		<div id="content">
        <decorator:body />
		</div>
		<div id="footer">
			<%@ include file="/WEB-INF/jsp/decorators/footer.jsp"%>
		</div>
		</div>
    </body>
</html>