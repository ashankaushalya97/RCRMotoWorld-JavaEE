package lk.ijse.dep.rcr.service;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(urlPatterns = "/customer")
public class CustomerServlet extends HttpServlet {


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
            resp.getWriter().write(arrayBuilder.build().toString());


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
