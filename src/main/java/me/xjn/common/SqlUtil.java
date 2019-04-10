package me.xjn.common;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class SqlUtil{
    public static SqlSessionFactory sqlSessionFactory;
    static{
        try{
            SqlSessionFactory f = new SqlSessionFactoryBuilder()
            .build(Resources.getResourceAsStream("config/mybatis.xml"));
            sqlSessionFactory = f;
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}