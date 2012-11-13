<%@ page language="java" pageEncoding="UTF-8"%>
<body>
    <form id="myAjaxRequestForm" method="post" action="<%=request.getContextPath()%>/formAction/initData.do">
    	<input type="text" name="userId">
    	<br/>
    	<input type="text" name="userName">
    	
    	<input type="submit" value="initData" /> 
    </form>
</body>