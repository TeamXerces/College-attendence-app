# College Attendance Management System — Project Documentation

## 1. Project overview

The College Attendance Management System is a Java-based web application that digitizes class attendance and academic attendance records. It replaces paper registers and disconnected spreadsheets with a central database that can be accessed by administrators, faculty members and students according to their roles.

**Technology stack:** Core Java 17, Jakarta Servlet 6, JSP, JDBC, MySQL 8+, HTML5, CSS3 and vanilla JavaScript. The application is packaged as a WAR and designed for Apache Tomcat 10.1+.

## 2. Problem understanding

Traditional attendance workflows commonly require faculty to take a roll call, write results into a register and later calculate student percentages manually. This process consumes class time and can lead to transcription mistakes, duplicate records, misplaced registers and delayed identification of students whose attendance is below the institutional threshold.

The system centralizes student, faculty, course, enrollment and attendance information. A faculty member records attendance against a specific course session. The same session can be reopened and corrected without creating duplicate attendance entries. Students can see their own course-wise attendance, while authorized administrators can review institution-wide records.

## 3. Project objectives

- Digitize the process of recording and maintaining class attendance.
- Maintain student, faculty and course information in a relational database.
- Restrict information and actions using Admin, Faculty and Student roles.
- Calculate attendance percentages consistently from recorded sessions.
- Make low-attendance cases easy to identify.
- Reduce manual work and provide downloadable attendance reports.

## 4. User roles and permissions

| Role | Permissions |
|---|---|
| Admin | Manage students, faculty and courses; assign course faculty; inspect all reports; export CSV. |
| Faculty | View assigned courses; mark, review and correct attendance; inspect reports for assigned courses. |
| Student | View own attendance percentage, course-wise breakdown and session history. |

The authentication filter protects application routes. Each controller also checks the role before performing role-specific operations. State-changing forms use per-session CSRF tokens, including sign-in and sign-out.

## 5. Functional modules

### 5.1 Authentication
Users sign in with their college email and password. Passwords are stored as salted PBKDF2-HMAC-SHA256 hashes. A successful sign-in creates a session containing the user's ID, name, email and role. Sign-out invalidates the session.

### 5.2 Student management
Administrators can create and remove student profiles. A student profile stores a unique roll number, department, semester and phone number. A matching student is enrolled automatically in courses from the same department and semester.

### 5.3 Faculty management
Administrators can create faculty accounts with a unique employee ID, department and contact number. A faculty account cannot be removed while it owns courses; those courses must first be reassigned or deleted.

### 5.4 Course management
A course stores its code, name, department, semester and assigned faculty member. When the course is created, students with the same department and semester are enrolled automatically. An enrollment is unique for each course/student pair.

### 5.5 Attendance management
Faculty selects an assigned course, session date and session topic. Each enrolled student is marked as one of:

- **Present** — attended the class.
- **Absent** — did not attend the class.
- **Late** — attended late; counted as attended in the percentage calculation.
- **Excused** — an approved exception; excluded from the percentage denominator.

The database enforces one session per course/date/topic and one attendance record per session/student. Re-saving the same session updates the existing records.

### 5.6 Reporting
Administrators can view all courses and faculty can view their assigned courses. Reports support course and date filters. A CSV export contains roll number, student, course, department, semester, attended sessions, counted sessions and attendance percentage.

### 5.7 Student attendance view
Students can see their overall percentage, course-wise percentage, attendance count and session history. Courses below 75% are highlighted as requiring attention. The 75% figure is a configurable presentation convention in this starter project, not a hard-coded institutional policy.

## 6. Attendance calculation

The application uses the following calculation:

`Attendance % = (Present + Late) / (Present + Late + Absent) × 100`

Excused records are excluded from the denominator. The calculation is grouped by student and course. If a student has no counted sessions, the percentage is displayed as unavailable rather than dividing by zero.

## 7. System architecture

The project follows a straightforward MVC-style structure:

1. **Presentation layer:** JSP pages render data and HTML forms. CSS and JavaScript provide responsive layout and small interactions.
2. **Controller layer:** Jakarta Servlets receive requests, validate input, enforce role checks, call the data-access layer and forward to JSP views or redirect after a form submission.
3. **Data-access layer:** JDBC helper methods use prepared statements and map query results into Java maps for the JSP views.
4. **Database layer:** MySQL stores users, profiles, courses, enrollments, attendance sessions and attendance records.

### Request flow

`Browser → AuthFilter → Servlet Controller → JDBC → MySQL → Servlet Request Attributes → JSP → Browser`

Login is handled separately. After authentication, `/app/*` routes require a valid session.

## 8. Database design

| Table | Purpose | Key relationships |
|---|---|---|
| `app_users` | Login identity, role, password hash and active flag. | Parent for student/faculty profiles; referenced by courses and sessions. |
| `students` | Student roll number, department, semester and phone. | One-to-one with `app_users`. |
| `faculty` | Employee ID, department and phone. | One-to-one with `app_users`. |
| `courses` | Course code, name, department, semester and faculty owner. | References faculty user. |
| `enrollments` | Course-to-student mapping. | Many-to-many relationship between courses and students. |
| `attendance_sessions` | A dated class meeting and its topic. | References course and faculty; unique by course/date/topic. |
| `attendance_records` | Status and remarks for one student in one session. | References session and student; unique by session/student. |

The schema uses primary keys, unique constraints and foreign keys. Cascading deletes are used for dependent enrollment and attendance rows. The SQL schema is in `database/schema.sql`; the application also creates the same tables at startup.

## 9. Core Java and JDBC concepts demonstrated

- Classes and objects for servlet controllers and utility components.
- Encapsulation through utility classes and shared servlet helpers.
- Inheritance from `HttpServlet` and a shared `BaseServlet` class.
- Exception handling with `try/catch` and try-with-resources.
- Collections (`List`, `Map`, `Set`) to process database results.
- JDBC `Connection`, `PreparedStatement`, `ResultSet` and generated keys.
- Transactions for multi-table student/faculty/course/attendance operations.
- Prepared statements for database input, reducing SQL injection risk.
- Password derivation using the standard Java cryptography API.

## 10. Servlet and web integration

- `LoginServlet` handles sign-in and session creation.
- `LogoutServlet` invalidates the session.
- `AuthFilter` protects `/app/*` routes, issues session CSRF tokens and validates POST requests.
- `DashboardServlet` loads role-specific dashboard information.
- `StudentServlet`, `FacultyServlet` and `CourseServlet` handle administrative create/remove workflows.
- `AttendanceServlet` records and updates class attendance.
- `ReportsServlet` displays filtered reports and streams CSV files.
- `MyAttendanceServlet` displays the authenticated student's own records.

JSP views are stored under `src/main/webapp/WEB-INF/views` and therefore cannot be requested directly from the browser. They are rendered through the controllers.

## 11. User interface and UX

The interface uses a persistent desktop sidebar, a compact top navigation bar, dashboard statistic cards, table-based directories, course cards, status chips, attendance progress bars and responsive layouts for smaller screens. Forms provide labels and basic browser-side validation. Attendance has quick “All present” and “All absent” controls, while each student can still be adjusted individually. Destructive operations ask for confirmation.

## 12. Setup and execution

1. Install JDK 17+, Maven 3.8+, MySQL 8+ and Tomcat 10.1+.
2. Create the database by running `database/schema.sql` in MySQL.
3. Set `DB_URL`, `DB_USER` and `DB_PASSWORD` environment variables.
4. Run `mvn clean package` in the project root.
5. Deploy `target/attendance-management.war` into Tomcat's `webapps` directory.
6. Start Tomcat and open `http://localhost:8080/attendance-management/`.

The application creates its tables and inserts demo accounts at startup. See `README.md` for the demo credentials and GitHub publishing commands.

## 13. Sample test cases

| Test | Action | Expected result |
|---|---|---|
| Valid login | Sign in with a demo account. | User reaches the dashboard for their role. |
| Invalid login | Enter an incorrect password. | Error message is shown; no session is created. |
| Route protection | Open `/app/students` without login. | Browser is redirected to sign-in. |
| Role protection | A student opens `/app/courses`. | Request is rejected with HTTP 403. |
| Add student | Admin submits a valid unique student profile. | User and student rows are created; matching courses are enrolled. |
| Duplicate student | Reuse an email or roll number. | Transaction is rolled back and a clear error message is shown. |
| Mark attendance | Faculty records statuses for a course/date/topic. | One session and its student records are saved. |
| Correct attendance | Reopen and re-save the same session. | Existing student statuses are updated, not duplicated. |
| Attendance report | Filter by course/date. | Rows and percentages reflect the filter. |
| CSV export | Select Export CSV. | Browser receives a CSV download. |
| Student privacy | Student signs in. | Only their own attendance records are shown. |

## 14. Limitations and future improvements

This is a functional academic project starter. Before production use, add institution-specific admission/enrollment workflows, audit logs for attendance edits, password reset and password-change flows, configurable attendance rules, pagination, database connection pooling, automated integration tests and HTTPS-only secure cookie configuration. A production deployment should use a dedicated least-privilege database account, secret management, backup/restore procedures and a data-retention policy.
