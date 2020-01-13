package lk.ijse.dep.rcr.service;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet(urlPatterns = "/order")
public class OrderService extends HttpServlet {

    private Connection getConnection(){
        try {
            return ((BasicDataSource)getServletContext().getAttribute("pool")).getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong!");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection connection = getConnection();
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject order = reader.readObject();
        try {
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO orders VALUES (?,?,?)");
            boolean result = false;
            try {
                connection.setAutoCommit(false);
                pstm.setObject(1,order.getString("orderId"));
                pstm.setObject(2, Date.valueOf(order.getString("date")));
                pstm.setObject(3,order.getString("customerId"));
                if (pstm.executeUpdate()>0){
                    result=true;
                }else{
                    result=false;
                    connection.rollback();
                }
                JsonArray orderDetails = order.getJsonArray("orderDetails");
                PreparedStatement pstm2 = connection.prepareStatement("INSERT INTO orderDetail VALUES (?,?,?,?)");
                for (JsonValue orderDetail : orderDetails) {
                    JsonObject obj = orderDetail.asJsonObject();
                    pstm2.setObject(1,order.getString("orderId"));
                    pstm2.setObject(2,obj.getString("itemId"));
                    pstm2.setObject(3,obj.getInt("qty"));
                    pstm2.setObject(4,obj.getInt("unitPrice"));
                    if(pstm2.executeUpdate()>0){
                        result = true;
                    }else{
                        result=false;
                        connection.rollback();
                    }
                    PreparedStatement pstm3 = connection.prepareStatement("UPDATE item SET qtyOnHand=(qtyOnHand-?) WHERE itemId=?");
                    pstm3.setObject(1,obj.getInt("qty"));
                    pstm3.setObject(2,obj.getString("itemId"));
                    if(pstm3.executeUpdate()>0){
                        result=true;
                        System.out.println("Item updated");
                    }else{
                        connection.rollback();
                        result=false;
                    }


                }
                if(result){
                    resp.setStatus(HttpServletResponse.SC_CREATED);
                }else{
                    connection.rollback();
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                connection.setAutoCommit(true);

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
}
