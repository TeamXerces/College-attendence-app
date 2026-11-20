package com.college.attendance.servlet;

import com.college.attendance.util.Database;
import com.college.attendance.util.Jdbc;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Map;

@WebServlet("/app/courses")
public class CourseServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
        requireRole(req,res,"ADMIN");if(res.isCommitted())return;
        try{
            req.setAttribute("courses",Jdbc.query("SELECT c.id,c.course_code,c.course_name,c.department,c.semester,u.full_name AS faculty_name,(SELECT COUNT(*) FROM enrollments e WHERE e.course_id=c.id) AS student_count FROM courses c JOIN app_users u ON u.id=c.faculty_id ORDER BY c.course_code"));
            req.setAttribute("faculty",Jdbc.query("SELECT u.id,u.full_name,f.employee_id FROM faculty f JOIN app_users u ON u.id=f.user_id ORDER BY u.full_name"));
            page(req,res,"courses","Course Catalogue","courses");
        }catch(Exception ex){getServletContext().log("Unable to load courses",ex);flash(req,"error","Could not load courses.");res.sendRedirect(req.getContextPath()+"/app/dashboard");}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException{
        requireRole(req,res,"ADMIN");if(res.isCommitted())return;
        try{
            if("delete".equals(value(req,"action"))){Jdbc.update("DELETE FROM courses WHERE id=?",longValue(req,"courseId",-1));flash(req,"success","Course and its attendance sessions were removed.");}
            else{
                String code=value(req,"courseCode").toUpperCase(),name=value(req,"courseName"),dept=value(req,"department");int semester=intValue(req,"semester",0);long facultyId=longValue(req,"facultyId",-1);
                if(code.isBlank()||name.isBlank()||dept.isBlank()||semester<1||semester>12||facultyId<1)flash(req,"error","Complete all course fields and select a faculty member.");
                else try(Connection con=Database.getConnection()){
                    try(PreparedStatement check=con.prepareStatement("SELECT COUNT(*) FROM faculty WHERE user_id=?")){
                        check.setLong(1,facultyId);
                        try(ResultSet rs=check.executeQuery()){
                            rs.next();
                            if(rs.getLong(1)==0) throw new IllegalArgumentException("Selected account is not a faculty member.");
                        }
                    }
                    con.setAutoCommit(false);
                    try{
                        long courseId;
                        try(PreparedStatement ps=con.prepareStatement("INSERT INTO courses(course_code,course_name,department,semester,faculty_id) VALUES(?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                            ps.setString(1,code);ps.setString(2,name);ps.setString(3,dept);ps.setInt(4,semester);ps.setLong(5,facultyId);ps.executeUpdate();try(ResultSet rs=ps.getGeneratedKeys()){rs.next();courseId=rs.getLong(1);}
                        }
                        try(PreparedStatement ps=con.prepareStatement("INSERT IGNORE INTO enrollments(course_id,student_id) SELECT ?,id FROM students WHERE department=? AND semester=?")){
                            ps.setLong(1,courseId);ps.setString(2,dept);ps.setInt(3,semester);ps.executeUpdate();
                        }
                        con.commit();flash(req,"success","Course created. Students in the same department and semester were enrolled automatically.");
                    }catch(Exception ex){con.rollback();throw ex;}
                }
            }
        }catch(Exception ex){getServletContext().log("Course update failed",ex);flash(req,"error","Could not save the course. Check its course code is unique.");}
        res.sendRedirect(req.getContextPath()+"/app/courses");
    }
}
