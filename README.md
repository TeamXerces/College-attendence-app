# College Attendance Management System

A responsive, role-based Java web application for managing college attendance. The project uses **Core Java 17, Jakarta Servlets, JSP, JDBC, MySQL 8+, HTML, CSS and vanilla JavaScript**. It is packaged as a WAR for Apache Tomcat 10.1+.

## Features

### Admin
- Dashboard with student, faculty, course and attendance-session statistics.
- Create and remove student records and faculty accounts.
- Create and remove courses and assign a faculty member.
- Automatically enroll matching students when a course is created (same department and semester).
- View institution-wide attendance reports and export report rows to CSV.

### Faculty
- View assigned courses and class statistics.
- Create an attendance session for a course, date and topic.
- Mark students Present, Absent, Late or Excused.
- Re-open the same course/date/topic to update attendance; records are upserted rather than duplicated.
- View course-wise attendance reports and export CSV.

### Student
- Personal dashboard with overall attendance percentage.
- Course-wise attendance percentage and session history.
- Clear low-attendance indicator for courses below 75%.

### Application
- Role-based login and protected `/app/*` routes.
- Per-session CSRF validation for state-changing forms.
- Passwords stored as salted PBKDF2-HMAC-SHA256 hashes (not plaintext).
- Prepared statements for database access and transaction handling for multi-table changes.
- Responsive layout for desktop, tablet and mobile.
- Search/filter controls, success/error feedback and CSV export.

## Requirements

- JDK 17 or later
- Apache Maven 3.8+
- MySQL 8.0 or later
- Apache Tomcat 10.1 or later (Jakarta Servlet 6)

## Run locally (Windows)

1. Install and start MySQL.
2. Open MySQL Workbench or the MySQL command line and run:

   ```sql
   CREATE DATABASE college_attendance CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

   Or execute `database/schema.sql`.

3. Set database environment variables. The defaults are shown below; change them to match your MySQL installation:

   **PowerShell (default VS Code terminal):**

   ```powershell
   $env:DB_URL = "jdbc:mysql://localhost:3306/college_attendance?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
   $env:DB_USER = "root"
   $env:DB_PASSWORD = "your_mysql_password"
   ```

   **Command Prompt:**

   ```bat
   set DB_URL=jdbc:mysql://localhost:3306/college_attendance?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   set DB_USER=root
   set DB_PASSWORD=your_mysql_password
   ```

   On macOS/Linux use `export DB_URL=...`, `export DB_USER=...` and `export DB_PASSWORD=...`.

4. Build the project from this folder:

   ```bash
   mvn clean package
   ```

5. Copy `target/attendance-management.war` into Tomcat's `webapps` directory and start Tomcat.
6. Open `http://localhost:8080/attendance-management/`.

The application creates its tables and inserts demo users on first startup. The MySQL database itself must already exist (run `database/schema.sql` first), and the configured database user must have permission to create and modify tables in it.

## Demo accounts

| Role | Email | Password |
|---|---|---|
| Admin | `admin@college.edu` | `Admin@123` |
| Faculty | `faculty@college.edu` | `Faculty@123` |
| Student | `student@college.edu` | `Student@123` |

Change demo passwords and remove sample data before using this application with real student records.

## Configuration

| Environment variable | Default |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/college_attendance?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USER` | `root` |
| `DB_PASSWORD` | empty string |

## Project structure

```text
college-attendance-management/
├── database/
│   └── schema.sql
├── docs/
│   └── PROJECT_DOCUMENTATION.md
├── src/main/java/com/college/attendance/
│   ├── filter/              # Authentication, authorization and CSRF
│   ├── listener/            # Database bootstrap and demo data
│   ├── servlet/             # HTTP controllers / MVC request handling
│   └── util/                # JDBC helper, database and password utilities
├── src/main/webapp/
│   ├── assets/              # CSS and JavaScript
│   ├── WEB-INF/views/       # JSP views and shared fragments
│   └── index.jsp
├── pom.xml
└── README.md
```

## Important notes

- This is an educational starter project. Review it for your institution's privacy, retention, audit and security requirements before production use.
- The demo account credentials are public by design. Replace them before deployment.
- Use HTTPS, backups, a dedicated least-privilege MySQL account and a managed secret store in production.
- The application uses session-based authentication. Deploy behind HTTPS and configure secure session cookies in production.

## GitHub

Create a new empty GitHub repository, then run these commands from the project directory:

```bash
git init
git add .
git commit -m "Initial college attendance management system"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/college-attendance-management.git
git push -u origin main
```

Replace `YOUR_USERNAME` with your GitHub username. The project is repository-ready, but it has not been published to a GitHub account from this environment.
