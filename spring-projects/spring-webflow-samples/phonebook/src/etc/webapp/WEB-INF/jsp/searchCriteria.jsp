<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert">
		<img src="images/webflow-logo.jpg"/>
	</div>
	<form action="phonebook.htm">
	<table>
		<tr>
			<td>Search Criteria</td>
		</tr>
		<tr>
			<td colspan="2">
				<hr>
			</td>
		</tr>
		<spring:hasBindErrors>
		<tr>
			<td colspan="2">
				<div class="error">Please provide valid search criteria!</div>
			</td>
		</tr>
		</spring:hasBindErrors>
		<spring:bind path="firstName">
		<tr>
			<td>First Name</td>
			<td>
				<input type="text" name="${status.expression}" value="${status.value}">
			</td>
		</tr>
		</spring:bind>		
		<spring:bind path="lastName">
		<TR>
			<td>Last Name</td>
			<td>
				<input type="text" name="${status.expression}" value="${status.value}/>">
			</td>
		</TR>
		</spring:bind>
		<tr>
			<td colspan="2">
				<hr>
			</td>
		</tr>
		<tr>
			<td colspan="2" class="buttonBar">
				<input type="hidden" name="_flowExecutionId" value="${flowExecutionId}">
				<input type="submit" class="button" name="_eventId_search" value="Search">
			</td>
		</tr>
	</table>
	</form>
</div>

<%@ include file="includeBottom.jsp" %>