<%@ include file="/WEB-INF/jsp/includes.jsp" %>

<P>
<H2>Veterinarians:</H2>
<TABLE border="true">
  <TH>Name</TH><TH>Specialties</TH>
  <c:forEach var="vet" items="${vets}">
    <TR>
      <TD><c:out value="${vet.firstName}"/> <c:out value="${vet.lastName}"/></TD>
      <TD>
				<c:forEach var="specialty" items="${vet.specialties}">
					<c:out value="${specialty.name}"/>
				</c:forEach>
        <c:if test="${vet.nrOfSpecialties == 0}">
				  none
				</c:if>
      </TD>
    </TR>
  </c:forEach>
</TABLE>
<P>
<BR>
<A href="<c:url value="/welcome.htm"/>">Home</A>
