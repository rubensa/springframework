			<c:if test="${!empty data.guessHistory}">
				Guess history:
				<table border="1">
				    <th>Guess</th>
				    <th>Right Position</th>
				    <th>Present But Wrong Position</th>
				    <c:forEach var="guessData" items="${data.guessHistory}">
				    	<tr>
				    		<td>${guessData.guess}</td>
				    		<td>${guessData.rightPosition}</td>
				    		<td>${guessData.correctButWrongPosition}</td>
				    	</tr>
				    </c:forEach>
				</table>
			</c:if>