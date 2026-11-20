package com.college.attendance.servlet;

import com.college.attendance.util.Jdbc;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/app/dashboard")
public class DashboardServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
        try{
            if("ADMIN".equals(role(req)))loadAdmin(req);
            else if("FACULTY".equals(role(req)))loadFaculty(req);
            else loadStudent(req);
            page(req,res,"dashboard","Overview","dashboard");
        }catch(Exception ex){getServletContext().log("Unable to load dashboard",ex);req.setAttribute("dashboardError","Dashboard data could not be loaded. Check your database connection.");page(req,res,"dashboard","Overview","dashboard");}
    }

    private void loadAdmin(HttpServletRequest req)throws Exception{
        req.setAttribute("stats",List.of(
            Map.of("label","Total students","value",Jdbc.count("SELECT COUNT(*) FROM students"),"icon","♙","tone","violet"),
            Map.of("label","Faculty members","value",Jdbc.count("SELECT COUNT(*) FROM faculty"),"icon","♧","tone","blue"),
            Map.of("label","Active courses","value",Jdbc.count("SELECT COUNT(*) FROM courses"),"icon","▤","tone","mint"),
            Map.of("label","Attendance sessions","value",Jdbc.count("SELECT COUNT(*) FROM attendance_sessions"),"icon","☑","tone","orange")
        ));
        req.setAttribute("recentSessions",Jdbc.query("SELECT se.id,se.session_date,se.topic,c.course_code,c.course_name,u.full_name AS faculty_name,(SELECT COUNT(*) FROM attendance_records ar WHERE ar.session_id=se.id) AS marked_count FROM attendance_sessions se JOIN courses c ON c.id=se.course_id JOIN app_users u ON u.id=se.faculty_id ORDER BY se.session_date DESC,se.id DESC LIMIT 7"));
        req.setAttribute("attendanceAverage",Jdbc.one("SELECT ROUND(100*SUM(status IN ('PRESENT','LATE'))/NULLIF(SUM(status <> 'EXCUSED'),0),1) AS avg_pct FROM attendance_records").get("avg_pct"));
        req.setAttribute("subheading","Here is what's happening across your campus today.");
    }

    private void loadFaculty(HttpServletRequest req)throws Exception{
        long uid=userId(req);
        req.setAttribute("stats",List.of(
            Map.of("label","Assigned courses","value",Jdbc.count("SELECT COUNT(*) FROM courses WHERE faculty_id=?",uid),"icon","▤","tone","violet"),
            Map.of("label","Enrolled students","value",Jdbc.count("SELECT COUNT(DISTINCT e.student_id) FROM enrollments e JOIN courses c ON c.id=e.course_id WHERE c.faculty_id=?",uid),"icon","♙","tone","blue"),
            Map.of("label","Sessions recorded","value",Jdbc.count("SELECT COUNT(*) FROM attendance_sessions WHERE faculty_id=?",uid),"icon","☑","tone","mint"),
            Map.of("label","Courses below target","value",Jdbc.count("SELECT COUNT(*) FROM (SELECT c.id,ROUND(100*SUM(ar.status IN ('PRESENT','LATE'))/NULLIF(SUM(ar.status <> 'EXCUSED'),0),1) p FROM courses c JOIN attendance_sessions se ON se.course_id=c.id JOIN attendance_records ar ON ar.session_id=se.id WHERE c.faculty_id=? GROUP BY c.id HAVING p<75) x",uid),"icon","!","tone","orange")
        ));
        req.setAttribute("recentSessions",Jdbc.query("SELECT se.id,se.session_date,se.topic,c.course_code,c.course_name,u.full_name AS faculty_name,(SELECT COUNT(*) FROM attendance_records ar WHERE ar.session_id=se.id) AS marked_count FROM attendance_sessions se JOIN courses c ON c.id=se.course_id JOIN app_users u ON u.id=se.faculty_id WHERE c.faculty_id=? ORDER BY se.session_date DESC,se.id DESC LIMIT 7",uid));
        req.setAttribute("attendanceAverage",Jdbc.one("SELECT ROUND(100*SUM(ar.status IN ('PRESENT','LATE'))/NULLIF(SUM(ar.status <> 'EXCUSED'),0),1) AS avg_pct FROM attendance_records ar JOIN attendance_sessions se ON se.id=ar.session_id JOIN courses c ON c.id=se.course_id WHERE c.faculty_id=?",uid).get("avg_pct"));
        req.setAttribute("subheading","Manage your classes, record attendance and review student progress.");
    }

    private void loadStudent(HttpServletRequest req)throws Exception{
        long uid=userId(req);
        Map<String,Object> student=Jdbc.one("SELECT id,roll_no,department,semester FROM students WHERE user_id=?",uid);
        if(student==null){req.setAttribute("studentMissing",true);req.setAttribute("courseAttendance",List.of());return;}
        long sid=((Number)student.get("id")).longValue();
        req.setAttribute("student",student);
        Map<String,Object> summary=Jdbc.one("SELECT SUM(ar.status IN ('PRESENT','LATE')) AS attended,SUM(ar.status <> 'EXCUSED') AS total,ROUND(100*SUM(ar.status IN ('PRESENT','LATE'))/NULLIF(SUM(ar.status <> 'EXCUSED'),0),1) AS percentage FROM attendance_records ar WHERE ar.student_id=?",sid);
        req.setAttribute("studentSummary",summary);
        req.setAttribute("courseAttendance",Jdbc.query("SELECT c.id,c.course_code,c.course_name,COALESCE(SUM(ar.status <> 'EXCUSED'),0) AS sessions,COALESCE(SUM(ar.status IN ('PRESENT','LATE')),0) AS attended,ROUND(100*SUM(ar.status IN ('PRESENT','LATE'))/NULLIF(SUM(ar.status <> 'EXCUSED'),0),1) AS percentage FROM enrollments e JOIN courses c ON c.id=e.course_id LEFT JOIN attendance_sessions se ON se.course_id=c.id LEFT JOIN attendance_records ar ON ar.session_id=se.id AND ar.student_id=e.student_id WHERE e.student_id=? GROUP BY c.id,c.course_code,c.course_name ORDER BY c.course_code",sid));
        req.setAttribute("recentStudentRecords",Jdbc.query("SELECT se.session_date,se.topic,c.course_code,c.course_name,ar.status FROM attendance_records ar JOIN attendance_sessions se ON se.id=ar.session_id JOIN courses c ON c.id=se.course_id WHERE ar.student_id=? ORDER BY se.session_date DESC,se.id DESC LIMIT 8",sid));
        req.setAttribute("subheading","Track your attendance and stay on top of every course.");
    }
}
