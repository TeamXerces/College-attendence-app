<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="_header.jspf" %>
<section class="page-heading compact-heading"><div><span class="eyebrow">CAMPUS DIRECTORY</span><h1>Faculty directory</h1><p>Manage teaching staff and their access to the attendance workspace.</p></div><span class="total-count"><b><c:out value="${faculty.size()}"/></b> faculty members</span></section>
<details class="panel form-panel"><summary><span class="summary-plus">＋</span><span><b>Add a faculty member</b><small>Create a faculty login and employee profile</small></span><span class="summary-chevron">⌄</span></summary>
<form method="post" action="${ctx}/app/faculty" class="form-grid"><input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
    <div class="field"><label>Full name *</label><input name="fullName" required placeholder="e.g. Dr. Neha Gupta"></div>
    <div class="field"><label>College email *</label><input name="email" type="email" required placeholder="faculty@college.edu"></div>
    <div class="field"><label>Employee ID *</label><input name="employeeId" required placeholder="FAC-010"></div>
    <div class="field"><label>Department *</label><input name="department" required placeholder="Computer Science"></div>
    <div class="field"><label>Phone</label><input name="phone" placeholder="+91 98XXXXXXXX"></div>
    <div class="field"><label>Temporary password *</label><input name="password" type="password" minlength="8" required placeholder="At least 8 characters"></div>
    <div class="form-actions"><span class="form-hint">Faculty can access assigned courses only.</span><button class="btn btn-primary" type="submit">Create faculty account <span>→</span></button></div>
</form></details>
<section class="panel section-panel directory-panel"><div class="panel-heading"><div><span class="eyebrow">DIRECTORY</span><h3>Teaching staff <span class="heading-count"><c:out value="${faculty.size()}"/></span></h3></div><form class="search-form" method="get" action="${ctx}/app/faculty"><span>⌕</span><input name="q" value="<c:out value='${search}'/>" placeholder="Search faculty..." aria-label="Search faculty"><button type="submit">Search</button></form></div>
<div class="table-wrap"><table><thead><tr><th>FACULTY MEMBER</th><th>EMPLOYEE ID</th><th>DEPARTMENT</th><th>COURSES</th><th>PHONE</th><th>ACTION</th></tr></thead><tbody>
<c:forEach items="${faculty}" var="member"><tr><td><div class="person-cell"><span class="person-avatar faculty-avatar"><c:out value="${member.full_name.substring(0,1)}"/></span><span><b><c:out value="${member.full_name}"/></b><small class="cell-sub"><c:out value="${member.email}"/></small></span></div></td><td><span class="code-chip"><c:out value="${member.employee_id}"/></span></td><td><c:out value="${member.department}"/></td><td><span class="count-chip"><c:out value="${member.course_count}"/> courses</span></td><td><c:out value="${member.phone}" default="—"/></td><td><form method="post" action="${ctx}/app/faculty" onsubmit="return confirm('Remove this faculty account? Faculty with assigned courses cannot be removed.');"><input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="delete"><input type="hidden" name="facultyId" value="${member.faculty_id}"><button class="icon-button danger-button" title="Remove faculty" type="submit">⌫</button></form></td></tr></c:forEach>
<c:if test="${empty faculty}"><tr><td colspan="6" class="empty-cell">No faculty members match this search.</td></tr></c:if>
</tbody></table></div></section>
<%@ include file="_footer.jspf" %>
