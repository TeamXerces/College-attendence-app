<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="_header.jspf" %>
<c:if test="${not empty dashboardError}"><div class="alert alert-error"><span class="alert-dot"></span><c:out value="${dashboardError}"/></div></c:if>
<section class="page-heading">
    <div><span class="eyebrow"><c:out value="${sessionScope.role}"/> DASHBOARD</span><h1>Good to see you, <c:out value="${sessionScope.userName}"/> <span class="wave">✦</span></h1><p><c:out value="${subheading}"/></p></div>
    <c:if test="${sessionScope.role ne 'STUDENT'}"><a class="btn btn-primary" href="${ctx}/app/attendance">＋ Take attendance</a></c:if>
    <c:if test="${sessionScope.role eq 'STUDENT'}"><a class="btn btn-primary" href="${ctx}/app/my-attendance">View attendance <span>→</span></a></c:if>
</section>

<c:choose>
    <c:when test="${sessionScope.role eq 'STUDENT'}">
        <c:choose>
            <c:when test="${studentMissing}"><div class="empty-state"><div class="empty-icon">♙</div><h3>Student profile not found</h3><p>Please contact the college administrator to link your account to a student record.</p></div></c:when>
            <c:otherwise>
                <section class="student-summary-grid">
                    <div class="panel attendance-overview-panel">
                        <div class="panel-heading"><div><span class="eyebrow">YOUR ATTENDANCE</span><h3>Overall attendance</h3></div><span class="pill pill-soft">Roll no. <c:out value="${student.roll_no}"/></span></div>
                        <div class="attendance-overview-body">
                            <div class="donut" style="--progress:${empty studentSummary.percentage ? 0 : studentSummary.percentage}%"><div class="donut-inner"><strong><c:choose><c:when test="${not empty studentSummary.percentage}"><c:out value="${studentSummary.percentage}"/>%</c:when><c:otherwise>—</c:otherwise></c:choose></strong><small>attendance</small></div></div>
                            <div class="attendance-overview-copy"><h2><c:choose><c:when test="${studentSummary.percentage ge 75}">You're on track</c:when><c:when test="${not empty studentSummary.percentage}">Needs your attention</c:when><c:otherwise>Just getting started</c:otherwise></c:choose></h2><p>Keep your attendance above the 75% requirement to stay in good academic standing.</p><div class="legend-row"><span class="legend-dot present-dot"></span>Present / Late <b><c:out value="${studentSummary.attended}" default="0"/></b><span class="legend-dot absent-dot"></span>Counted sessions <b><c:out value="${studentSummary.total}" default="0"/></b></div></div>
                        </div>
                    </div>
                    <div class="panel student-profile-panel"><span class="eyebrow">ACADEMIC PROFILE</span><div class="profile-avatar">ST</div><h3><c:out value="${sessionScope.userName}"/></h3><p><c:out value="${student.department}"/></p><div class="profile-meta"><span>Semester</span><b><c:out value="${student.semester}"/></b></div><div class="profile-meta"><span>Roll number</span><b><c:out value="${student.roll_no}"/></b></div></div>
                </section>
                <section class="panel section-panel">
                    <div class="panel-heading"><div><span class="eyebrow">COURSE BREAKDOWN</span><h3>Attendance by course</h3></div><a class="text-link" href="${ctx}/app/my-attendance">Full history →</a></div>
                    <div class="course-progress-list">
                        <c:forEach items="${courseAttendance}" var="course">
                            <div class="course-progress-item"><div class="course-progress-title"><div><b><c:out value="${course.course_code}"/></b><span><c:out value="${course.course_name}"/></span></div><strong class="${course.percentage lt 75 ? 'text-warning' : 'text-positive'}"><c:choose><c:when test="${not empty course.percentage}"><c:out value="${course.percentage}"/>%</c:when><c:otherwise>—</c:otherwise></c:choose></strong></div><div class="progress-track"><div class="progress-fill ${course.percentage lt 75 ? 'progress-warning' : ''}" style="width:${empty course.percentage ? 0 : course.percentage}%"></div></div><div class="progress-foot"><span><c:out value="${course.attended}" default="0"/> of <c:out value="${course.sessions}" default="0"/> sessions attended</span><c:if test="${course.percentage lt 75}"><span class="warning-label">Below 75% target</span></c:if></div></div>
                        </c:forEach>
                        <c:if test="${empty courseAttendance}"><div class="empty-inline">No enrolled courses yet.</div></c:if>
                    </div>
                </section>
                <section class="panel section-panel">
                    <div class="panel-heading"><div><span class="eyebrow">RECENT ACTIVITY</span><h3>Latest attendance records</h3></div><a class="text-link" href="${ctx}/app/my-attendance">See all →</a></div>
                    <div class="table-wrap"><table><thead><tr><th>DATE</th><th>COURSE</th><th>TOPIC / SESSION</th><th>STATUS</th></tr></thead><tbody>
                    <c:forEach items="${recentStudentRecords}" var="record"><tr><td><c:out value="${record.session_date}"/></td><td><b><c:out value="${record.course_code}"/></b><small class="cell-sub"><c:out value="${record.course_name}"/></small></td><td><c:out value="${record.topic}"/></td><td><span class="status-pill status-${record.status}"><c:out value="${record.status}"/></span></td></tr></c:forEach>
                    <c:if test="${empty recentStudentRecords}"><tr><td colspan="4" class="empty-cell">No attendance has been recorded yet.</td></tr></c:if>
                    </tbody></table></div>
                </section>
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:otherwise>
        <section class="stats-grid">
            <c:forEach items="${stats}" var="stat"><article class="stat-card"><div class="stat-top"><span class="stat-icon tone-${stat.tone}"><c:out value="${stat.icon}"/></span><span class="stat-kicker">LIVE</span></div><strong class="stat-value"><c:out value="${stat.value}"/></strong><span class="stat-label"><c:out value="${stat.label}"/></span></article></c:forEach>
        </section>
        <section class="dashboard-lower-grid">
            <div class="panel recent-panel"><div class="panel-heading"><div><span class="eyebrow">LATEST ACTIVITY</span><h3>Recent attendance sessions</h3></div><a class="text-link" href="${ctx}/app/reports">View reports →</a></div>
                <div class="table-wrap"><table><thead><tr><th>COURSE</th><th>SESSION</th><th>DATE</th><th>RECORDS</th></tr></thead><tbody>
                <c:forEach items="${recentSessions}" var="session"><tr><td><b><c:out value="${session.course_code}"/></b><small class="cell-sub"><c:out value="${session.course_name}"/></small></td><td><c:out value="${session.topic}"/></td><td><c:out value="${session.session_date}"/></td><td><span class="count-chip"><c:out value="${session.marked_count}"/> marked</span></td></tr></c:forEach>
                <c:if test="${empty recentSessions}"><tr><td colspan="4" class="empty-cell">No attendance sessions recorded yet.</td></tr></c:if>
                </tbody></table></div>
            </div>
            <div class="panel pulse-panel"><div class="panel-heading"><div><span class="eyebrow">CAMPUS PULSE</span><h3>Attendance health</h3></div><span class="pulse-indicator">●</span></div>
                <div class="pulse-visual"><div class="donut small-donut" style="--progress:${empty attendanceAverage ? 0 : attendanceAverage}%"><div class="donut-inner"><strong><c:choose><c:when test="${not empty attendanceAverage}"><c:out value="${attendanceAverage}"/>%</c:when><c:otherwise>—</c:otherwise></c:choose></strong><small>average</small></div></div></div>
                <p class="pulse-caption">Average attendance across the records you can access.</p>
                <div class="pulse-divider"></div><a class="btn btn-secondary btn-full" href="${ctx}/app/reports">Explore reports <span>→</span></a>
            </div>
        </section>
        <section class="quick-actions"><div><span class="eyebrow">QUICK ACTIONS</span><h3>Keep things moving</h3></div>
            <a class="quick-action-card" href="${ctx}/app/attendance"><span class="quick-action-icon">☑</span><span><b>Mark attendance</b><small>Record a class session</small></span><span class="quick-arrow">→</span></a>
            <c:if test="${sessionScope.role eq 'ADMIN'}"><a class="quick-action-card" href="${ctx}/app/students"><span class="quick-action-icon">♙</span><span><b>Add a student</b><small>Update the directory</small></span><span class="quick-arrow">→</span></a><a class="quick-action-card" href="${ctx}/app/courses"><span class="quick-action-icon">▤</span><span><b>Manage courses</b><small>Keep classes organized</small></span><span class="quick-arrow">→</span></a></c:if>
        </section>
    </c:otherwise>
</c:choose>
<%@ include file="_footer.jspf" %>
