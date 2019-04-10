package me.xjn.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import me.xjn.common.SqlUtil;
import me.xjn.pojo.*;
public class QueryFlightDao{

    public List<QueryFlight> getQueries(){
        SqlSession session = SqlUtil.sqlSessionFactory.openSession();
        List<QueryFlight> list = new ArrayList<>();
        try {
            list = session.selectList("me.xjn.dao.QueryFlightMapper.getQueries");
        } finally {
            session.close();
        }
        return list;
    }
}