package com.college.attendance.servlet;

import com.college.attendance.util.Database;
import com.college.attendance.util.Jdbc;
import com.college.attendance.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Map;

@WebServlet("/app/students")
public class StudentServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        requireRole(req, res, "ADMIN");
        if (res.isCommitted()) return;
        try {
            String search = value(req, "q");
            String like = "%" + search + "%";
            List<Map<String,Object>> students = Jdbc.query(
                "SELECT s.id AS student_id, s.roll_no, s.department, s.semester, s.phone, u.full_name, u.email " +
                "FROM students s JOIN app_users u ON u.id=s.user_id " +
                "WHERE (?='' OR u.full_name LIKE ? OR u.email LIKE ? OR s.roll_no LIKE ? OR s.department LIKE ?) " +
                "ORDER BY s.roll_no", search, like, like, like, like);
            req.setAttribute("students", students);
            req.setAttribute("search", search);
            req.setAttribute("totalStudents", Jdbc.count("SELECT COUNT(*) FROM students"));
            page(req, res, "students", "Student Directory", "students");
        } catch (Exception ex) { getServletContext().log("Unable to load students", ex); flash(req,"error","Could not load student records."); res.sendRedirect(req.getContextPath()+"/app/dashboard"); }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        requireRole(req, res, "ADMIN"); if (res.isCommitted()) return;
        String action = value(req,"action");
        try {
            if ("delete".equals(action)) {
                long id = longValue(req,"studentId",-1);
                Jdbc.update("DELETE FROM app_users WHERE id=(SELECT user_id FROM students WHERE id=?)", id);
                flash(req,"success","Student record removed.");
            } else {
                String name=value(req,"fullName"), email=value(req,"email").toLowerCase(), roll=value(req,"rollNo"), dept=value(req,"department"), phone=value(req,"phone"), password=value(req,"password");
                int semester=intValue(req,"semester",0);
                if (name.isBlank() || email.isBlank() || roll.isBlank() || dept.isBlank() || semester<1 || semester>12 || password.length()<8) {
                    flash(req,"error","Complete all required fields. Password must contain at least 8 characters.");
                } else {
                    try (Connection con=Database.getConnection()) {
                        con.setAutoCommit(false);
                        try {
                            long userId;
                            try (PreparedStatement ps=con.prepareStatement("INSERT INTO app_users(full_name,email,password_hash,role) VALUES(?,?,?,'STUDENT')",Statement.RETURN_GENERATED_KEYS)) {
                                ps.setString(1,name); ps.setString(2,email); ps.setString(3,PasswordUtil.hash(password)); ps.executeUpdate();
                                try(ResultSet rs=ps.getGeneratedKeys()){rs.next();userId=rs.getLong(1);}
                            }
                            long studentId;
                            try (PreparedStatement ps=con.prepareStatement("INSERT INTO students(user_id,roll_no,department,semester,phone) VALUES(?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                                ps.setLong(1,userId);ps.setString(2,roll);ps.setString(3,dept);ps.setInt(4,semester);ps.setString(5,phone);ps.executeUpdate();
                                try(ResultSet rs=ps.getGeneratedKeys()){rs.next();studentId=rs.getLong(1);}
                            }
                            try (PreparedStatement ps=con.prepareStatement("INSERT IGNORE INTO enrollments(course_id,student_id) SELECT id,? FROM courses WHERE department=? AND semester=?")) {
                                ps.setLong(1,studentId);ps.setString(2,dept);ps.setInt(3,semester);ps.executeUpdate();
                            }
                            con.commit(); flash(req,"success","Student added and matching courses enrolled.");
                        } catch(Exception ex){con.rollback();throw ex;}
                    }
                }
            }
        } catch (Exception ex) { getServletContext().log("Student update failed",ex); flash(req,"error","Could not save this student. Check that the email and roll number are unique."); }
        res.sendRedirect(req.getContextPath()+"/app/students");
    }
}
