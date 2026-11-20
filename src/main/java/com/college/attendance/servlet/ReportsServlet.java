package com.college.attendance.servlet;

import com.college.attendance.util.Jdbc;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@WebServlet("/app/reports")
public class ReportsServlet extends BaseServlet {
    private String reportSql(boolean facultyOnly) {
        return "SELECT s.roll_no,u.full_name,c.course_code,c.course_name,c.department,c.semester, " +
            "SUM(ar.status IN ('PRESENT','LATE')) AS attended, SUM(ar.status <> 'EXCUSED') AS total_sessions, " +
            "ROUND(100*SUM(ar.status IN ('PRESENT','LATE'))/NULLIF(SUM(ar.status <> 'EXCUSED'),0),1) AS percentage " +
            "FROM attendance_records ar JOIN attendance_sessions se ON se.id=ar.session_id " +
            "JOIN courses c ON c.id=se.course_id JOIN students s ON s.id=ar.student_id JOIN app_users u ON u.id=s.user_id " +
            "WHERE (?=0 OR c.id=?) AND (? IS NULL OR se.session_date>=?) AND (? IS NULL OR se.session_date<=?) " +
            (facultyOnly ? "AND c.faculty_id=? " : "") +
            "GROUP BY s.id,s.roll_no,u.full_name,c.id,c.course_code,c.course_name,c.department,c.semester ORDER BY c.course_code,s.roll_no";
    }

    private Object[] params(HttpServletRequest req, boolean facultyOnly) {
        String from=value(req,"from"),to=value(req,"to");long course=longValue(req,"courseId",0);
        Date fromDate=from.isBlank()?null:Date.valueOf(LocalDate.parse(from));
        Date toDate=to.isBlank()?null:Date.valueOf(LocalDate.parse(to));
        if(facultyOnly)return new Object[]{course,course,fromDate,fromDate,toDate,toDate,userId(req)};
        return new Object[]{course,course,fromDate,fromDate,toDate,toDate};
    }

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
        requireRole(req,res,"ADMIN","FACULTY");if(res.isCommitted())return;
        try{
            boolean facultyOnly="FACULTY".equals(role(req));
            List<Map<String,Object>> courses=facultyOnly?Jdbc.query("SELECT id,course_code,course_name FROM courses WHERE faculty_id=? ORDER BY course_code",userId(req)):Jdbc.query("SELECT id,course_code,course_name FROM courses ORDER BY course_code");
            List<Map<String,Object>> rows=Jdbc.query(reportSql(facultyOnly),params(req,facultyOnly));
            if("csv".equalsIgnoreCase(value(req,"format"))){
                res.setContentType("text/csv; charset=UTF-8");
                res.setHeader("Content-Disposition","attachment; filename=attendance-report.csv");
                var out=res.getWriter();out.write("Roll Number,Student,Course Code,Course,Department,Semester,Attended,Counted Sessions,Attendance Percentage\r\n");
                for(Map<String,Object> row:rows){
                    out.write(csv(row.get("roll_no"))+","+csv(row.get("full_name"))+","+csv(row.get("course_code"))+","+csv(row.get("course_name"))+","+csv(row.get("department"))+","+csv(row.get("semester"))+","+csv(row.get("attended"))+","+csv(row.get("total_sessions"))+","+csv(row.get("percentage"))+"%\r\n");
                }
                out.flush();return;
            }
            req.setAttribute("courses",courses);req.setAttribute("rows",rows);req.setAttribute("from",value(req,"from"));req.setAttribute("to",value(req,"to"));req.setAttribute("selectedCourse",longValue(req,"courseId",0));
            long atRisk=rows.stream().filter(r->r.get("percentage")!=null && ((Number)r.get("percentage")).doubleValue()<75).count();
            req.setAttribute("atRisk",atRisk);req.setAttribute("rowCount",rows.size());
            page(req,res,"reports","Attendance Reports","reports");
        }catch(Exception ex){getServletContext().log("Unable to create report",ex);flash(req,"error","Could not create the attendance report.");res.sendRedirect(req.getContextPath()+"/app/dashboard");}
    }
    private String csv(Object value) {
        String s = value == null ? "" : String.valueOf(value);
        // Escape formula-leading text so exported CSV is safer to open in spreadsheet software.
        if (!s.isEmpty() && "=+-@\t\r".indexOf(s.charAt(0)) >= 0) s = "'" + s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
