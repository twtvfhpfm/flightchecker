package me.xjn.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import me.xjn.common.SqlUtil;
import me.xjn.pojo.FlightInfo;

public class FlightInfoDao {

    public int recordFlights(List<FlightInfo> list) {
        SqlSession session = SqlUtil.sqlSessionFactory.openSession();
        try {
            for (FlightInfo info : list) {
                session.insert("me.xjn.dao.FlightInfoMapper.insertFlight", info);
            }
        } finally {
            session.close();
        }
        return 0;
    }

    public List<FlightInfo> getFlightHistory(FlightInfo info){
        SqlSession session = SqlUtil.sqlSessionFactory.openSession();
        List<FlightInfo> list = new ArrayList<>();
        try {
            list = session.selectList("me.xjn.dao.FlightInfoMapper.getFlightHistory", info.getFlightNumber());
        } finally {
            session.close();
        }
        list.removeIf(i -> !(i.getDepartureDate().split(" ")[0].equals(info.getDepartureDate().split(" ")[0])));
        list.sort((obj1, obj2) -> obj1.getCheckTime().compareTo(obj2.getCheckTime()));

        Iterator<FlightInfo> iterator = list.iterator();
        FlightInfo current = null;
        while(iterator.hasNext()){
            FlightInfo next = iterator.next();
            if (current == null) current = next;
            if (current != next && current.getPrice() == next.getPrice()){
                iterator.remove();
            }
            else{
                current = next;
            }
        }
        return list;
    }

}