package lk.ijse.dep.rcr.service;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(urlPatterns = "/item")
public class ItemServlet extends HttpServlet {

    private Connection getConnection(){
        try {
            return ((BasicDataSource)getServletContext().getAttribute("pool")).getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong!");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection connection = getConnection();
        try {
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM item");
            ResultSet rst = pstm.executeQuery();
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            while (rst.next()){
                JsonObjectBuilder item = Json.createObjectBuilder();
                item.add("itemId",rst.getString(1));
                item.add("categoryId",rst.getString(2));
                item.add("brand",rst.getString(3));
                item.add("description",rst.getString(4));
                item.add("qtyOnHand",rst.getInt(5));
                item.add("buyPrice",rst.getDouble(6));
                item.add("unitPrice",rst.getDouble(7));
                arrayBuilder.add(item.build());
            }
            JsonArray item = arrayBuilder.build();
            resp.setContentType("application/json");
            resp.setIntHeader("X-Count",item.size());
            resp.getWriter().write(item.toString());

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
        Connection connection = getConnection();
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject item = reader.readObject();
        try {
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO item VALUES (?,?,?,?,?,?,?)");
            try {
                pstm.setObject(1,item.getString("itemId"));
                pstm.setObject(2,item.getString("categoryId"));
                pstm.setObject(3,item.getString("brand"));
                pstm.setObject(4,item.getString("description"));
                pstm.setObject(5,item.getInt("qtyOnHand"));
                pstm.setObject(6,item.getInt("buyPrice"));
                pstm.setObject(7,item.getInt("unitPrice"));
                int i = pstm.executeUpdate();
                if(i>0){
                    resp.setStatus(HttpServletResponse.SC_CREATED);
                }else{
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }catch (NullPointerException e){
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

        }catch (SQLException e){
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
        Connection connection = getConnection();
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject item = reader.readObject();
        try {
            PreparedStatement pstm = connection.prepareStatement("UPDATE item SET categoryId=?,brand=?,description=?,qtyOnHand=?,buyPrice=?,unitPrice=? WHERE itemId=?");
            try {
                pstm.setObject(1,item.getString("categoryId"));
                pstm.setObject(2,item.getString("brand"));
                pstm.setObject(3,item.getString("description"));
                pstm.setObject(4,item.getInt("qtyOnHand"));
                pstm.setObject(5,item.getInt("buyPrice"));
                pstm.setObject(6,item.getInt("unitPrice"));
                pstm.setObject(7,item.getString("itemId"));
                if(pstm.executeUpdate()>0){
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }else{
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }

            }catch (NullPointerException e){
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String itemId = req.getParameter("itemId");
        Connection connection = getConnection();
        try {
            PreparedStatement pstm = connection.prepareStatement("DELETE FROM item WHERE itemId=?");
            if(itemId.trim()==null || itemId.trim().length()==0){
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            pstm.setObject(1,itemId);
            if(pstm.executeUpdate()>0){
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }else{
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
