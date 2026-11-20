package com.college.attendance.servlet;

import com.college.attendance.util.Database;
import com.college.attendance.util.Jdbc;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

@WebServlet("/app/attendance")
public class AttendanceServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
        requireRole(req,res,"ADMIN","FACULTY");if(res.isCommitted())return;
        try{
            String courseSql="SELECT c.id,c.course_code,c.course_name,c.department,c.semester FROM courses c"+("FACULTY".equals(role(req))?" WHERE c.faculty_id=?":"")+" ORDER BY c.course_code";
            List<Map<String,Object>> courses="FACULTY".equals(role(req))?Jdbc.query(courseSql,userId(req)):Jdbc.query(courseSql);
            req.setAttribute("courses",courses);
            long selected=longValue(req,"courseId",courses.isEmpty()?-1:((Number)courses.get(0).get("id")).longValue());
            Map<String,Object> selectedCourse=null;
            for(Map<String,Object> c:courses)if(((Number)c.get("id")).longValue()==selected){selectedCourse=c;break;}
            if(selectedCourse!=null){
                req.setAttribute("selectedCourse",selectedCourse);
                long requestedSession=longValue(req,"sessionId",-1);
                Map<String,Object> editing=null;
                if(requestedSession>0) editing=Jdbc.one("SELECT id,session_date,topic FROM attendance_sessions WHERE id=? AND course_id=?",requestedSession,selected);
                if(editing==null) requestedSession=-1;
                req.setAttribute("editingSession",editing);
                List<Map<String,Object>> students=Jdbc.query("SELECT s.id AS student_id,s.roll_no,u.full_name,u.email,ar.status,ar.remarks FROM enrollments e JOIN students s ON s.id=e.student_id JOIN app_users u ON u.id=s.user_id LEFT JOIN attendance_records ar ON ar.session_id=? AND ar.student_id=s.id WHERE e.course_id=? ORDER BY s.roll_no",requestedSession,selected);
                req.setAttribute("students",students);
                req.setAttribute("sessions",Jdbc.query("SELECT id,session_date,topic,(SELECT COUNT(*) FROM attendance_records ar WHERE ar.session_id=asess.id) AS marked_count FROM attendance_sessions asess WHERE course_id=? ORDER BY session_date DESC,id DESC LIMIT 12",selected));
            }else req.setAttribute("students",List.of());
            req.setAttribute("today",LocalDate.now().toString());
            page(req,res,"attendance","Take Attendance","attendance");
        }catch(Exception ex){getServletContext().log("Unable to load attendance screen",ex);flash(req,"error","Could not load attendance data.");res.sendRedirect(req.getContextPath()+"/app/dashboard");}
    }

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException{
        requireRole(req,res,"ADMIN","FACULTY");if(res.isCommitted())return;
        long courseId=longValue(req,"courseId",-1);
        String dateText=value(req,"sessionDate"),topic=value(req,"topic");
        try{
            Map<String,Object> course=Jdbc.one("SELECT id,faculty_id FROM courses WHERE id=?",courseId);
            if(course==null || ("FACULTY".equals(role(req)) && ((Number)course.get("faculty_id")).longValue()!=userId(req))){
                flash(req,"error","You do not have access to this course.");res.sendRedirect(req.getContextPath()+"/app/attendance");return;
            }
            LocalDate date=LocalDate.parse(dateText);
            if(topic.isBlank()||topic.length()>180)throw new IllegalArgumentException("Enter a topic of 1–180 characters.");
            try(Connection con=Database.getConnection()){
                con.setAutoCommit(false);
                try{
                    long sessionId;
                    try(PreparedStatement ps=con.prepareStatement("INSERT INTO attendance_sessions(course_id,faculty_id,session_date,topic) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id),faculty_id=VALUES(faculty_id)",Statement.RETURN_GENERATED_KEYS)){
                        ps.setLong(1,courseId);ps.setLong(2,userId(req));ps.setDate(3,java.sql.Date.valueOf(date));ps.setString(4,topic);ps.executeUpdate();
                        try(ResultSet rs=ps.getGeneratedKeys()){rs.next();sessionId=rs.getLong(1);}
                    }
                    try(PreparedStatement students=con.prepareStatement("SELECT student_id FROM enrollments WHERE course_id=?")){
                        students.setLong(1,courseId);
                        try(ResultSet rs=students.executeQuery()){
                            while(rs.next()){
                                long studentId=rs.getLong(1);
                                String status=value(req,"status_"+studentId).toUpperCase(Locale.ROOT);
                                if(!Set.of("PRESENT","ABSENT","LATE","EXCUSED").contains(status))status="ABSENT";
                                String remarks=value(req,"remarks_"+studentId);
                                try(PreparedStatement save=con.prepareStatement("INSERT INTO attendance_records(session_id,student_id,status,remarks) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE status=VALUES(status),remarks=VALUES(remarks)")){
                                    save.setLong(1,sessionId);save.setLong(2,studentId);save.setString(3,status);save.setString(4,remarks);save.executeUpdate();
                                }
                            }
                        }
                    }
                    con.commit();flash(req,"success","Attendance saved for the selected class. You can reopen this session to make corrections.");
                }catch(Exception ex){con.rollback();throw ex;}
            }
        }catch(Exception ex){getServletContext().log("Attendance save failed",ex);flash(req,"error","Unable to save attendance. Check the date, topic and database connection.");}
        res.sendRedirect(req.getContextPath()+"/app/attendance?courseId="+courseId);
    }
}
