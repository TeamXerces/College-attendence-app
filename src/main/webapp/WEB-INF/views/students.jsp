<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="_header.jspf" %>
<section class="page-heading compact-heading"><div><span class="eyebrow">CAMPUS DIRECTORY</span><h1>Student directory</h1><p>Manage student profiles, academic details and course enrolments.</p></div><span class="total-count"><b><c:out value="${totalStudents}"/></b> total students</span></section>
<details class="panel form-panel"><summary><span class="summary-plus">＋</span><span><b>Add a student</b><small>Create a student login and profile</small></span><span class="summary-chevron">⌄</span></summary>
<form method="post" action="${ctx}/app/students" class="form-grid"><input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
    <div class="field"><label>Full name *</label><input name="fullName" required placeholder="e.g. Aisha Kapoor"></div>
    <div class="field"><label>College email *</label><input name="email" type="email" required placeholder="student@college.edu"></div>
    <div class="field"><label>Roll number *</label><input name="rollNo" required placeholder="25BCS1009"></div>
    <div class="field"><label>Department *</label><input name="department" required value="Computer Science"></div>
    <div class="field"><label>Semester *</label><select name="semester" required><option value="">Select semester</option><c:forEach begin="1" end="8" var="n"><option value="${n}">Semester ${n}</option></c:forEach></select></div>
    <div class="field"><label>Phone</label><input name="phone" placeholder="+91 98XXXXXXXX"></div>
    <div class="field"><label>Temporary password *</label><input name="password" type="password" required minlength="8" placeholder="At least 8 characters"></div>
    <div class="form-actions"><span class="form-hint">Matching courses are enrolled automatically.</span><button class="btn btn-primary" type="submit">Create student <span>→</span></button></div>
</form></details>
<section class="panel section-panel directory-panel">
    <div class="panel-heading"><div><span class="eyebrow">DIRECTORY</span><h3>All students <span class="heading-count"><c:out value="${students.size()}"/></span></h3></div>
        <form class="search-form" method="get" action="${ctx}/app/students"><span>⌕</span><input name="q" value="<c:out value='${search}'/>" placeholder="Search name, roll no..." aria-label="Search students"><button type="submit">Search</button></form>
    </div>
    <div class="table-wrap"><table><thead><tr><th>STUDENT</th><th>ROLL NUMBER</th><th>DEPARTMENT</th><th>SEMESTER</th><th>CONTACT</th><th>ACTION</th></tr></thead><tbody>
    <c:forEach items="${students}" var="student"><tr><td><div class="person-cell"><span class="person-avatar"><c:out value="${student.full_name.substring(0,1)}"/></span><span><b><c:out value="${student.full_name}"/></b><small class="cell-sub"><c:out value="${student.email}"/></small></span></div></td><td><span class="code-chip"><c:out value="${student.roll_no}"/></span></td><td><c:out value="${student.department}"/></td><td><span class="pill pill-soft">Sem <c:out value="${student.semester}"/></span></td><td><c:out value="${student.phone}" default="—"/></td><td><form method="post" action="${ctx}/app/students" onsubmit="return confirm('Remove this student profile? Attendance records linked to this student will also be removed.');"><input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="delete"><input type="hidden" name="studentId" value="${student.student_id}"><button class="icon-button danger-button" title="Remove student" type="submit">⌫</button></form></td></tr></c:forEach>
    <c:if test="${empty students}"><tr><td colspan="6" class="empty-cell">No students match this search.</td></tr></c:if>
    </tbody></table></div>
</section>
<%@ include file="_footer.jspf" %>
