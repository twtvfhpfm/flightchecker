package me.xjn.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import me.xjn.dao.FlightInfoDao;
import me.xjn.dao.QueryFlightDao;
import me.xjn.pojo.*;

public class FlightChecker {
    private String from;
    private String to;
    private String date;
    private String referer;

    private FlightInfoDao flightInfoDao;

    public FlightChecker(String from, String to, Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        this.date = df.format(date);
        this.from = from;
        this.to = to;
        this.flightInfoDao = new FlightInfoDao();
    }

    public static void execute() {
        QueryFlightDao queryFlightDao = new QueryFlightDao();
        List<QueryFlight> list = queryFlightDao.getQueries();
        for (QueryFlight query : list) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(query.getFlightDate());
            if (calendar.get(Calendar.DAY_OF_YEAR) <= Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
                continue;
            }

            FlightChecker fc = new FlightChecker(query.getDepartureCityCode(), query.getArrivalCityCode(),
                    query.getFlightDate());
            String cookie = fc.getCookie();
            String flights = fc.getFlights(cookie);
            List<FlightInfo> info = fc.parseFlights(flights);

            // 与历史记录比较，若价格有变动则保存到数据库，并发送邮件
            Iterator<FlightInfo> iterator = info.iterator();
            // 价格变动的阈值
            int diffThreshold = 50;
            while (iterator.hasNext()) {
                FlightInfo item = iterator.next();
                List<FlightInfo> history = fc.flightInfoDao.getFlightHistory(item);
                int priceDiff = diffThreshold;
                if (!history.isEmpty()) {
                    priceDiff = Integer.compare(history.get(history.size() - 1).getPrice(), item.getPrice());
                    if (Math.abs(priceDiff) < diffThreshold) {
                        iterator.remove();
                    }
                }
                if (Math.abs(priceDiff) >= diffThreshold) {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    EmailSender.send(MessageFormat.format("{0}从{1}到{2}的{3}航班{4}", df.format(query.getFlightDate()),
                            item.getDepartureCityName(), item.getArrivalCityName(), item.getFlightNumber(),
                            (priceDiff > 0 ? "降价了" : "涨价了")), fc.historyTable(history, item));
                }
            }
            if (info.isEmpty()) {
                System.out.println(MessageFormat.format("{0}: all flights are the same price",
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
            } else {
                fc.flightInfoDao.recordFlights(info);
            }

        }
    }

    public String getCookie() {
        String httpurl = MessageFormat.format("https://flights.ctrip.com/itinerary/oneway/{0}-{1}?date={2}", from, to,
                date);
        referer = httpurl;
        HttpURLConnection connection = null;
        String cookie = "";
        try {
            URL url = new URL(httpurl);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                for (String c : connection.getHeaderField("set-cookie").split(";")) {
                    if (c.contains("_abtest_userid")) {
                        cookie = c;
                        break;
                    }
                }
                if (cookie.isEmpty()) {
                    System.out.println("no cookie");
                    return "";
                }
            } else {
                System.out.println("get response code " + connection.getResponseCode());
                return "";
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "";
        } finally {
            connection.disconnect();
        }

        return cookie;

    }

    public String getFlights(String cookie) {
        String httpurl = "https://flights.ctrip.com/itinerary/api/12808/products";
        HttpURLConnection connection = null;
        OutputStream out = null;
        InputStream in = null;
        try {
            URL url = new URL(httpurl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("cookie", cookie);
            connection.setRequestProperty("referer", referer);
            connection.setRequestProperty("content-type", "application/json");
            connection.setDoOutput(true);

            out = connection.getOutputStream();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("flightWay", "Oneway");
            jsonObject.put("classType", "ALL");
            jsonObject.put("hasChild", false);
            jsonObject.put("hasBaby", false);
            jsonObject.put("searchIndex", 1);

            JSONArray jsonParams = new JSONArray();
            JSONObject param = new JSONObject();
            param.put("dcity", from.toUpperCase());
            param.put("acity", to.toUpperCase());
            // param.put("dcityname", "杭州");
            // param.put("acityname", "青岛");
            param.put("date", date);
            // param.put("dcityid", 17);
            // param.put("acityid", 7);
            jsonParams.add(param);
            jsonObject.put("airportParams", jsonParams);

            out.write(jsonObject.toJSONString().getBytes("UTF-8"));

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                in = connection.getInputStream();
                byte[] resp = recvBytesFromStream(in);
                return new String(resp);
            } else {
                System.out.println("get response code " + connection.getResponseCode());
                return "";
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "";
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            connection.disconnect();
        }
    }

    private byte[] recvBytesFromStream(InputStream is) throws IOException {
        int readed = 0;
        byte[] result = null;
        byte[] data = new byte[1024];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while ((readed = is.read(data)) != -1) {
            if (readed > 0) {
                outputStream.write(data, 0, readed);
            }
        }
        if (outputStream.size() > 0) {
            result = outputStream.toByteArray();
        }
        return result;
    }

    public List<FlightInfo> parseFlights(String flightsStr) {
        List<FlightInfo> ret = new ArrayList<>();
        JSONObject jsonObject = JSON.parseObject(flightsStr);
        int status = jsonObject.getIntValue("status");
        String msg = jsonObject.getString("msg");
        if (status == 0 && msg.equals("success")) {
            JSONArray routeList = jsonObject.getJSONObject("data").getJSONArray("routeList");

            for (int i = 0; i < routeList.size(); i++) {
                JSONObject route = routeList.getJSONObject(i);
                if (!route.getString("routeType").equals("Flight"))
                    continue;
                JSONObject leg = route.getJSONArray("legs").getJSONObject(0);
                JSONObject flight = leg.getJSONObject("flight");
                JSONObject characteristic = leg.getJSONObject("characteristic");

                FlightInfo info = new FlightInfo.Builder().setAirlineName(flight.getString("airlineName"))
                        .setFlightNumber(flight.getString("flightNumber"))
                        .setDepartureDate(flight.getString("departureDate"))
                        .setArrivalDate(flight.getString("arrivalDate"))
                        .setDepartureCityName(flight.getJSONObject("departureAirportInfo").getString("cityName"))
                        .setArrivalCityName(flight.getJSONObject("arrivalAirportInfo").getString("cityName"))
                        .setPrice(characteristic.getIntValue("lowestPrice")).build();
                ret.add(info);
            }
        }
        return ret;

    }

    public String toTable(List<FlightInfo> list) {
        StringBuffer sb = new StringBuffer();
        sb.append(
                "<table border=\"1\"><tr><th>航班</th><th>名称</th><th>出发时间</th><th>到达时间</th><th>出发地</th><th>目的地</th><th>价格</th></tr>");
        for (FlightInfo info : list) {
            sb.append(MessageFormat.format(
                    "<tr><td>{0}</td><td>{1}</td><td>{2}</td><td>{3}</td><td>{4}</td><td>{5}</td><td>{6}</td></tr>",
                    info.getFlightNumber(), info.getAirlineName(), info.getDepartureDate(), info.getArrivalDate(),
                    info.getDepartureCityName(), info.getArrivalCityName(), info.getPrice()));
        }
        sb.append("</table>");
        return sb.toString();
    }

    public String historyTable(List<FlightInfo> list, FlightInfo now) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer sb = new StringBuffer();
        sb.append(
                "<table border=\"1\"><tr><th>航班</th><th>名称</th><th>出发时间</th><th>到达时间</th><th>出发地</th><th>目的地</th><th>价格</th></tr>");
        sb.append(MessageFormat.format(
                "<tr><td>{0}</td><td>{1}</td><td>{2}</td><td>{3}</td><td>{4}</td><td>{5}</td><td>{6}</td></tr>",
                now.getFlightNumber(), now.getAirlineName(), now.getDepartureDate(), now.getArrivalDate(),
                now.getDepartureCityName(), now.getArrivalCityName(), now.getPrice()));
        sb.append("</table>");
        sb.append("<table border=\"1\"><tr><th>检查时间</th><th>价格</th></tr>");
        for (FlightInfo info : list) {
            sb.append(MessageFormat.format("<tr><td>{0}</td><td>{1}</td></tr>", df.format(info.getCheckTime()),
                    info.getPrice()));
        }
        sb.append("</table>");
        return sb.toString();
    }

}
