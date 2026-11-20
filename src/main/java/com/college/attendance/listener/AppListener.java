package com.college.attendance.listener;

import com.college.attendance.util.Database;
import com.college.attendance.util.PasswordUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebListener
public class AppListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        try (Connection con = Database.getConnection()) {
            createTables(con);
            seedDemoData(con);
            event.getServletContext().log("College Attendance Management System database is ready.");
        } catch (SQLException ex) {
            event.getServletContext().log("Database initialization failed. Check DB_URL, DB_USER, DB_PASSWORD and create the database first.", ex);
        }
    }

    private void createTables(Connection con) throws SQLException {
        String[] ddl = {
            "CREATE TABLE IF NOT EXISTS app_users (id BIGINT PRIMARY KEY AUTO_INCREMENT, full_name VARCHAR(120) NOT NULL, email VARCHAR(160) NOT NULL UNIQUE, password_hash VARCHAR(255) NOT NULL, role VARCHAR(20) NOT NULL, active BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, INDEX idx_users_role(role)) ENGINE=InnoDB",
            "CREATE TABLE IF NOT EXISTS students (id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL UNIQUE, roll_no VARCHAR(40) NOT NULL UNIQUE, department VARCHAR(100) NOT NULL, semester INT NOT NULL, phone VARCHAR(25), CONSTRAINT fk_student_user FOREIGN KEY(user_id) REFERENCES app_users(id) ON DELETE CASCADE) ENGINE=InnoDB",
            "CREATE TABLE IF NOT EXISTS faculty (id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL UNIQUE, employee_id VARCHAR(40) NOT NULL UNIQUE, department VARCHAR(100) NOT NULL, phone VARCHAR(25), CONSTRAINT fk_faculty_user FOREIGN KEY(user_id) REFERENCES app_users(id) ON DELETE CASCADE) ENGINE=InnoDB",
            "CREATE TABLE IF NOT EXISTS courses (id BIGINT PRIMARY KEY AUTO_INCREMENT, course_code VARCHAR(30) NOT NULL UNIQUE, course_name VARCHAR(140) NOT NULL, department VARCHAR(100) NOT NULL, semester INT NOT NULL, faculty_id BIGINT NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, CONSTRAINT fk_course_faculty FOREIGN KEY(faculty_id) REFERENCES app_users(id)) ENGINE=InnoDB",
            "CREATE TABLE IF NOT EXISTS enrollments (id BIGINT PRIMARY KEY AUTO_INCREMENT, course_id BIGINT NOT NULL, student_id BIGINT NOT NULL, enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, UNIQUE KEY uq_enrollment(course_id, student_id), CONSTRAINT fk_enroll_course FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE, CONSTRAINT fk_enroll_student FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE) ENGINE=InnoDB",
            "CREATE TABLE IF NOT EXISTS attendance_sessions (id BIGINT PRIMARY KEY AUTO_INCREMENT, course_id BIGINT NOT NULL, faculty_id BIGINT NOT NULL, session_date DATE NOT NULL, topic VARCHAR(180) NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, UNIQUE KEY uq_session(course_id, session_date, topic), CONSTRAINT fk_session_course FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE, CONSTRAINT fk_session_faculty FOREIGN KEY(faculty_id) REFERENCES app_users(id)) ENGINE=InnoDB",
            "CREATE TABLE IF NOT EXISTS attendance_records (id BIGINT PRIMARY KEY AUTO_INCREMENT, session_id BIGINT NOT NULL, student_id BIGINT NOT NULL, status VARCHAR(12) NOT NULL, remarks VARCHAR(255), marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, UNIQUE KEY uq_attendance(session_id, student_id), INDEX idx_attendance_student(student_id), CONSTRAINT fk_record_session FOREIGN KEY(session_id) REFERENCES attendance_sessions(id) ON DELETE CASCADE, CONSTRAINT fk_record_student FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE) ENGINE=InnoDB"
        };
        try (Statement st = con.createStatement()) {
            for (String sql : ddl) st.executeUpdate(sql);
        }
    }

    private void seedDemoData(Connection con) throws SQLException {
        long adminId = ensureUser(con, "Campus Administrator", "admin@college.edu", "Admin@123", "ADMIN");
        long facultyId = ensureUser(con, "Aarav Sharma", "faculty@college.edu", "Faculty@123", "FACULTY");
        ensureFaculty(con, facultyId, "FAC-001", "Computer Science", "+91 98765 43210");

        String[][] demoStudents = {
            {"Aarav Mehta", "student@college.edu", "25BCS1001", "Student@123"},
            {"Priya Verma", "priya@college.edu", "25BCS1002", "Student@123"},
            {"Rohan Singh", "rohan@college.edu", "25BCS1003", "Student@123"},
            {"Ananya Gupta", "ananya@college.edu", "25BCS1004", "Student@123"},
            {"Kabir Malhotra", "kabir@college.edu", "25BCS1005", "Student@123"},
            {"Ishita Rao", "ishita@college.edu", "25BCS1006", "Student@123"},
            {"Dev Patel", "dev@college.edu", "25BCS1007", "Student@123"},
            {"Meera Joshi", "meera@college.edu", "25BCS1008", "Student@123"}
        };
        List<Long> studentIds = new ArrayList<>();
        for (String[] s : demoStudents) {
            long uid = ensureUser(con, s[0], s[1], s[3], "STUDENT");
            studentIds.add(ensureStudent(con, uid, s[2], "Computer Science", 5, "+91 90000 10001"));
        }

        long course1 = ensureCourse(con, "CS301", "Data Structures & Algorithms", "Computer Science", 5, facultyId);
        long course2 = ensureCourse(con, "CS305", "Database Management Systems", "Computer Science", 5, facultyId);
        for (long studentId : studentIds) {
            enroll(con, course1, studentId);
            enroll(con, course2, studentId);
        }
        try (PreparedStatement check = con.prepareStatement("SELECT COUNT(*) FROM attendance_sessions"); ResultSet rs = check.executeQuery()) {
            if (rs.next() && rs.getInt(1) == 0) {
                seedSessions(con, course1, facultyId, studentIds, "Arrays and Linked Lists");
                seedSessions(con, course2, facultyId, studentIds, "Relational Model and SQL");
            }
        }
    }

    private long ensureUser(Connection con, String name, String email, String password, String role) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT id FROM app_users WHERE email=?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getLong(1); }
        }
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO app_users(full_name,email,password_hash,role) VALUES(?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name); ps.setString(2, email); ps.setString(3, PasswordUtil.hash(password)); ps.setString(4, role); ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        }
    }

    private void ensureFaculty(Connection con, long userId, String empId, String department, String phone) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("INSERT IGNORE INTO faculty(user_id,employee_id,department,phone) VALUES(?,?,?,?)")) {
            ps.setLong(1, userId); ps.setString(2, empId); ps.setString(3, department); ps.setString(4, phone); ps.executeUpdate();
        }
    }

    private long ensureStudent(Connection con, long userId, String roll, String dept, int semester, String phone) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT id FROM students WHERE user_id=?")) {
            ps.setLong(1, userId); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getLong(1); }
        }
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO students(user_id,roll_no,department,semester,phone) VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId); ps.setString(2, roll); ps.setString(3, dept); ps.setInt(4, semester); ps.setString(5, phone); ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        }
    }

    private long ensureCourse(Connection con, String code, String name, String dept, int semester, long facultyId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT id FROM courses WHERE course_code=?")) {
            ps.setString(1, code); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getLong(1); }
        }
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO courses(course_code,course_name,department,semester,faculty_id) VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code); ps.setString(2, name); ps.setString(3, dept); ps.setInt(4, semester); ps.setLong(5, facultyId); ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        }
    }

    private void enroll(Connection con, long courseId, long studentId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("INSERT IGNORE INTO enrollments(course_id,student_id) VALUES(?,?)")) {
            ps.setLong(1, courseId); ps.setLong(2, studentId); ps.executeUpdate();
        }
    }

    private void seedSessions(Connection con, long courseId, long facultyId, List<Long> studentIds, String topic) throws SQLException {
        for (int day = 7; day >= 1; day -= 3) {
            LocalDate date = LocalDate.now().minusDays(day);
            long sessionId;
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO attendance_sessions(course_id,faculty_id,session_date,topic) VALUES(?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, courseId); ps.setLong(2, facultyId); ps.setDate(3, Date.valueOf(date)); ps.setString(4, topic + " - " + date);
                ps.executeUpdate(); try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); sessionId = rs.getLong(1); }
            }
            int index = 0;
            for (long studentId : studentIds) {
                String status = (index + day) % 7 == 0 ? "ABSENT" : ((index + day) % 5 == 0 ? "LATE" : "PRESENT");
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO attendance_records(session_id,student_id,status) VALUES(?,?,?)")) {
                    ps.setLong(1, sessionId); ps.setLong(2, studentId); ps.setString(3, status); ps.executeUpdate();
                }
                index++;
            }
        }
    }
}
