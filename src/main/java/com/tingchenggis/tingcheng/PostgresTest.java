package com.tingchenggis.tingcheng;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class PostgresTest {
    public static void main(String[] args) {
        try {
            // 连接到PostgreSQL
            Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/postgres",
                "postgres",
                "postgres"
            );
            
            Statement stmt = conn.createStatement();
            
            // 检查数据库是否存在
            ResultSet rs = stmt.executeQuery(
                "SELECT datname FROM pg_database WHERE datname='tingchenggis_pg'"
            );
            
            if (rs.next()) {
                System.out.println("数据库 tingchenggis_pg 已存在");
            } else {
                System.out.println("数据库 tingchenggis_pg 不存在，需要创建");
                // 尝试创建数据库
                stmt.execute("CREATE DATABASE tingchenggis_pg");
                System.out.println("已创建数据库 tingchenggis_pg");
            }
            
            // 关闭连接
            rs.close();
            stmt.close();
            conn.close();
            
            // 再次连接到新数据库检查PostGIS
            conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/tingchenggis_pg",
                "postgres",
                "postgres"
            );
            
            stmt = conn.createStatement();
            
            // 检查PostGIS扩展是否存在
            rs = stmt.executeQuery(
                "SELECT extname FROM pg_extension WHERE extname='postgis'"
            );
            
            if (rs.next()) {
                System.out.println("PostGIS 扩展已安装");
            } else {
                System.out.println("PostGIS 扩展未安装，需要安装");
                try {
                    stmt.execute("CREATE EXTENSION IF NOT EXISTS postgis");
                    System.out.println("已安装 PostGIS 扩展");
                } catch (Exception e) {
                    System.out.println("安装 PostGIS 扩展失败: " + e.getMessage());
                }
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println("连接PostgreSQL失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}