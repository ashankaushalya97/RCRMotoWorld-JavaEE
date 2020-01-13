package lk.ijse.dep.rcr.service;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(urlPatterns = "/delivery")
public class DeliveryService extends HttpServlet {
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
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        try {
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM delivery");
            ResultSet rst = pstm.executeQuery();
            while (rst.next()){
                JsonObjectBuilder obj = Json.createObjectBuilder();
                obj.add("deliveryId",rst.getString(1));
                obj.add("orderId",rst.getString(2));
                obj.add("address",rst.getString(3));
                obj.add("status",rst.getString(4));
                arrayBuilder.add(obj.build());
            }
            JsonArray delivery = arrayBuilder.build();
            resp.setContentType("application/json");
            resp.setIntHeader("X-Count",delivery.size());
            resp.getWriter().write(delivery.toString());

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
        Connection connection =getConnection();
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject delivery = reader.readObject();
        try {
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO delivery VALUES (?,?,?,?)");
            try {
                pstm.setObject(1,delivery.getString("deliveryId"));
                pstm.setObject(2,delivery.getString("orderId"));
                pstm.setObject(3,delivery.getString("address"));
                pstm.setObject(4,delivery.getString("status"));
                if(pstm.executeUpdate()>0){
                    resp.setStatus(HttpServletResponse.SC_CREATED);
                }else{
                    resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
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
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection connection = getConnection();
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject delivery = reader.readObject();
        try {
            PreparedStatement pstm = connection.prepareStatement("UPDATE delivery SET orderId=?,address=?,states=? WHERE deliveryId=?");
            try {
                pstm.setObject(1,delivery.getString("orderId"));
                pstm.setObject(2,delivery.getString("address"));
                pstm.setObject(3,delivery.getString("status"));
                pstm.setObject(4,delivery.getString("deliveryId"));
                if(pstm.executeUpdate()>0){
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }else{
                    resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
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
        Connection connection = getConnection();
        String deliveryId = req.getParameter("deliveryId");
        try {
            PreparedStatement pstm = connection.prepareStatement("DELETE  FROM delivery WHERE deliveryId=?");
            if(deliveryId.trim()==null || deliveryId.trim().length()==0){
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            pstm.setObject(1,deliveryId);
            if(pstm.executeUpdate()>0){
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }else{
                resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
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
