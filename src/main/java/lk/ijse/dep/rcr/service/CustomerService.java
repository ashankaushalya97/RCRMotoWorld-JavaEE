package lk.ijse.dep.rcr.service;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(urlPatterns = "/customer")
public class CustomerService extends HttpServlet {


    private Connection getConnection(){
        try {
           return  ((BasicDataSource)getServletContext().getAttribute("pool")).getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Connection not found!");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write("Get Customer");
        Connection connection = getConnection();
        try {
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Customer");
            ResultSet rst = pstm.executeQuery();
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            while (rst.next()){
                JsonObjectBuilder obj = Json.createObjectBuilder();
                obj.add("id",rst.getString(1));
                obj.add("name",rst.getString(2));
                obj.add("address",rst.getString(3));
                arrayBuilder.add(obj.build());
            }
            JsonArray customer = arrayBuilder.build();
            resp.setContentType("applcation/json");
            resp.setIntHeader("X-Count",customer.size());
            resp.getWriter().write(customer.toString());


        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject customer = reader.readObject();
        Connection connection= getConnection();
        try {
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO customer VALUES (?,?,?)");
            try{
                pstm.setObject(1,customer.getString("id"));
                pstm.setObject(2,customer.getString("name"));
                pstm.setObject(3,customer.getString("contact"));
                int i = pstm.executeUpdate();
                if(i>0){
                    resp.setStatus(HttpServletResponse.SC_CREATED);
                    System.out.println("Customer inserted!");
                }else{
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }catch (NullPointerException e){
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject customer = reader.readObject();
        Connection connection= getConnection();
        try {
            PreparedStatement pstm = connection.prepareStatement("UPDATE customer SET name=?,contact=? WHERE customerId=?");
            try{
                pstm.setObject(1,customer.getString("name"));
                pstm.setObject(2,customer.getString("contact"));
                pstm.setObject(3,customer.getString("id"));
                int i = pstm.executeUpdate();
                if(i>0){
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    System.out.println("Customer updated!");
                }else{
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }

            }catch (NullPointerException e){
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            Connection connection= getConnection();
        try {
            PreparedStatement pstm = connection.prepareStatement("DELETE FROM customer WHERE customerId=?");
            String customerId = req.getParameter("customerId");
            if(customerId.trim()==null || customerId.trim().length()==0){
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            pstm.setObject(1,customerId);
            int i = pstm.executeUpdate();
            if(i>0){
                resp.setStatus(HttpServletResponse.SC_CREATED);
                System.out.println("Deleted");
            }else{
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
}
