<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="_header.jspf" %>
<section class="page-heading compact-heading"><div><span class="eyebrow">ACADEMIC CATALOGUE</span><h1>Courses</h1><p>Organize classes, assign faculty and connect students to the right courses.</p></div><span class="total-count"><b><c:out value="${courses.size()}"/></b> active courses</span></section>
<details class="panel form-panel" open><summary><span class="summary-plus">＋</span><span><b>Create a course</b><small>Set course details and assign a faculty member</small></span><span class="summary-chevron">⌄</span></summary>
<form method="post" action="${ctx}/app/courses" class="form-grid"><input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
    <div class="field"><label>Course code *</label><input name="courseCode" required placeholder="e.g. CS401"></div>
    <div class="field"><label>Course name *</label><input name="courseName" required placeholder="e.g. Operating Systems"></div>
    <div class="field"><label>Department *</label><input name="department" required value="Computer Science"></div>
    <div class="field"><label>Semester *</label><select name="semester" required><option value="">Select semester</option><c:forEach begin="1" end="8" var="n"><option value="${n}">Semester ${n}</option></c:forEach></select></div>
    <div class="field"><label>Assigned faculty *</label><select name="facultyId" required><option value="">Select faculty member</option><c:forEach items="${faculty}" var="member"><option value="${member.id}"><c:out value="${member.full_name}"/> · <c:out value="${member.employee_id}"/></option></c:forEach></select></div>
    <div class="form-actions"><span class="form-hint">Students with the same department and semester are enrolled automatically.</span><button class="btn btn-primary" type="submit">Create course <span>→</span></button></div>
</form></details>
<section class="course-card-grid">
<c:forEach items="${courses}" var="course"><article class="course-card"><div class="course-card-top"><span class="course-code-large"><c:out value="${course.course_code}"/></span><form method="post" action="${ctx}/app/courses" onsubmit="return confirm('Delete this course and all linked attendance sessions?');"><input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><input type="hidden" name="action" value="delete"><input type="hidden" name="courseId" value="${course.id}"><button type="submit" class="icon-button danger-button" title="Delete course">⌫</button></form></div><h3><c:out value="${course.course_name}"/></h3><p><c:out value="${course.department}"/> · Semester <c:out value="${course.semester}"/></p><div class="course-card-divider"></div><div class="course-card-meta"><span><small>FACULTY</small><b><c:out value="${course.faculty_name}"/></b></span><span><small>ENROLLED</small><b><c:out value="${course.student_count}"/> students</b></span></div><a class="course-card-link" href="${ctx}/app/attendance?courseId=${course.id}">Open attendance <span>→</span></a></article></c:forEach>
<c:if test="${empty courses}"><div class="empty-state"><div class="empty-icon">▤</div><h3>No courses yet</h3><p>Create a course to get started with attendance.</p></div></c:if>
</section>
<%@ include file="_footer.jspf" %>
