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

@WebServlet("/app/faculty")
public class FacultyServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
        requireRole(req,res,"ADMIN");if(res.isCommitted())return;
        try{
            String search=value(req,"q"),like="%"+search+"%";
            List<Map<String,Object>> faculty=Jdbc.query("SELECT f.id AS faculty_id,f.employee_id,f.department,f.phone,u.full_name,u.email,COUNT(DISTINCT c.id) AS course_count FROM faculty f JOIN app_users u ON u.id=f.user_id LEFT JOIN courses c ON c.faculty_id=u.id WHERE (?='' OR u.full_name LIKE ? OR u.email LIKE ? OR f.employee_id LIKE ? OR f.department LIKE ?) GROUP BY f.id,f.employee_id,f.department,f.phone,u.full_name,u.email ORDER BY u.full_name",search,like,like,like,like);
            req.setAttribute("faculty",faculty);req.setAttribute("search",search);page(req,res,"faculty","Faculty Directory","faculty");
        }catch(Exception ex){getServletContext().log("Unable to load faculty",ex);flash(req,"error","Could not load faculty records.");res.sendRedirect(req.getContextPath()+"/app/dashboard");}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException{
        requireRole(req,res,"ADMIN");if(res.isCommitted())return;
        try{
            if("delete".equals(value(req,"action"))){
                long facultyId=longValue(req,"facultyId",-1);
                long userId=Jdbc.count("SELECT user_id FROM faculty WHERE id=?",facultyId);
                if(userId>0 && Jdbc.count("SELECT COUNT(*) FROM courses WHERE faculty_id=?",userId)>0) flash(req,"error","Reassign or delete this faculty member's courses before removing the account.");
                else Jdbc.update("DELETE FROM app_users WHERE id=?",userId);
            }else{
                String name=value(req,"fullName"),email=value(req,"email").toLowerCase(),emp=value(req,"employeeId"),dept=value(req,"department"),phone=value(req,"phone"),password=value(req,"password");
                if(name.isBlank()||email.isBlank()||emp.isBlank()||dept.isBlank()||password.length()<8)flash(req,"error","Complete all required fields. Password must contain at least 8 characters.");
                else try(Connection con=Database.getConnection()){
                    con.setAutoCommit(false);
                    try{
                        long uid;
                        try(PreparedStatement ps=con.prepareStatement("INSERT INTO app_users(full_name,email,password_hash,role) VALUES(?,?,?,'FACULTY')",Statement.RETURN_GENERATED_KEYS)){
                            ps.setString(1,name);ps.setString(2,email);ps.setString(3,PasswordUtil.hash(password));ps.executeUpdate();try(ResultSet rs=ps.getGeneratedKeys()){rs.next();uid=rs.getLong(1);}
                        }
                        try(PreparedStatement ps=con.prepareStatement("INSERT INTO faculty(user_id,employee_id,department,phone) VALUES(?,?,?,?)")){
                            ps.setLong(1,uid);ps.setString(2,emp);ps.setString(3,dept);ps.setString(4,phone);ps.executeUpdate();
                        }
                        con.commit();flash(req,"success","Faculty account created successfully.");
                    }catch(Exception ex){con.rollback();throw ex;}
                }
            }
        }catch(Exception ex){getServletContext().log("Faculty update failed",ex);flash(req,"error","Could not save this faculty member. Check unique email and employee ID.");}
        res.sendRedirect(req.getContextPath()+"/app/faculty");
    }
}
