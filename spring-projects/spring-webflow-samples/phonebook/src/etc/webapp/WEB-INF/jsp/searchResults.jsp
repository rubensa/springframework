<%@ include file="includeTop.jsp" %>

<div id="content">
	<div id="insert">
		<img src="images/webflow-logo.jpg"/>
	</div>
	<form action="phonebook.htm">
	<table>
		<tr>
			<td>
				Search Results
			</td>
		</tr>
		<tr>
			<td>
				<hr>
			</td>
		</tr>
		<tr>
			<td>
				<table border="1">
					<tr>
						<th>First Name</th>
						<th>Last Name</th>
						<th>User Id</th>
						<th>Phone</th>
					</tr>
					<tr>
						<c:forEach var="person" items="${persons}">
							<td>${person.firstName}</td>
							<td>${person.lastName}</td>
							<td>
								<a href="phonebook.htm?_flowExecutionId=${flowExecutionId}&_eventId=select&id=${person.id}">
									${person.userId}
								</a>
							</td>
							<td>${person.phone}</td>
						</c:forEach>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td class="buttonBar">
				<input type="hidden" name="_flowExecutionId" value="${flowExecutionId}">
				<input type="submit" class="button" name="_eventId_newSearch" value="New Search">
			</td>
		</tr>
	</table>
	</form>
</div>

<%@ include file="includeBottom.jsp" %>