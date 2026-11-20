package com.college.attendance.servlet;

import com.college.attendance.util.Jdbc;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/app/my-attendance")
public class MyAttendanceServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
        requireRole(req,res,"STUDENT");if(res.isCommitted())return;
        try{
            Map<String,Object> student=Jdbc.one("SELECT id,roll_no FROM students WHERE user_id=?",userId(req));
            if(student==null){req.setAttribute("records",List.of());page(req,res,"my-attendance","My Attendance","my-attendance");return;}
            long sid=((Number)student.get("id")).longValue();
            req.setAttribute("records",Jdbc.query("SELECT se.session_date,se.topic,c.course_code,c.course_name,ar.status,ar.remarks FROM attendance_records ar JOIN attendance_sessions se ON se.id=ar.session_id JOIN courses c ON c.id=se.course_id WHERE ar.student_id=? ORDER BY se.session_date DESC,se.id DESC",sid));
            req.setAttribute("courseAttendance",Jdbc.query("SELECT c.course_code,c.course_name,COALESCE(SUM(ar.status <> 'EXCUSED'),0) AS sessions,COALESCE(SUM(ar.status IN ('PRESENT','LATE')),0) AS attended,ROUND(100*SUM(ar.status IN ('PRESENT','LATE'))/NULLIF(SUM(ar.status <> 'EXCUSED'),0),1) AS percentage FROM enrollments e JOIN courses c ON c.id=e.course_id LEFT JOIN attendance_sessions se ON se.course_id=c.id LEFT JOIN attendance_records ar ON ar.session_id=se.id AND ar.student_id=e.student_id WHERE e.student_id=? GROUP BY c.id,c.course_code,c.course_name ORDER BY c.course_code",sid));
            page(req,res,"my-attendance","My Attendance","my-attendance");
        }catch(Exception ex){getServletContext().log("Unable to load student attendance",ex);flash(req,"error","Could not load your attendance history.");res.sendRedirect(req.getContextPath()+"/app/dashboard");}
    }
}
