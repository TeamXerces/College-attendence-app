-- College Attendance Management System
-- MySQL 8.0+ schema. The application also creates these tables at startup.
CREATE DATABASE IF NOT EXISTS college_attendance
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE college_attendance;

CREATE TABLE IF NOT EXISTS app_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_role(role)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS students (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    roll_no VARCHAR(40) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    semester INT NOT NULL,
    phone VARCHAR(25),
    CONSTRAINT fk_student_user FOREIGN KEY(user_id) REFERENCES app_users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS faculty (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    employee_id VARCHAR(40) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    phone VARCHAR(25),
    CONSTRAINT fk_faculty_user FOREIGN KEY(user_id) REFERENCES app_users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(30) NOT NULL UNIQUE,
    course_name VARCHAR(140) NOT NULL,
    department VARCHAR(100) NOT NULL,
    semester INT NOT NULL,
    faculty_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_faculty FOREIGN KEY(faculty_id) REFERENCES app_users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS enrollments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_enrollment(course_id, student_id),
    CONSTRAINT fk_enroll_course FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_enroll_student FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS attendance_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    faculty_id BIGINT NOT NULL,
    session_date DATE NOT NULL,
    topic VARCHAR(180) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_session(course_id, session_date, topic),
    CONSTRAINT fk_session_course FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_session_faculty FOREIGN KEY(faculty_id) REFERENCES app_users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status VARCHAR(12) NOT NULL,
    remarks VARCHAR(255),
    marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_attendance(session_id, student_id),
    INDEX idx_attendance_student(student_id),
    CONSTRAINT fk_record_session FOREIGN KEY(session_id) REFERENCES attendance_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_record_student FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE
) ENGINE=InnoDB;
