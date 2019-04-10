package me.xjn.pojo;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

public class QueryFlight{
    @Setter @Getter private int id;
    @Setter @Getter private String departureCityName;
    @Setter @Getter private String arrivalCityName;
    @Setter @Getter private String departureCityCode;
    @Setter @Getter private String arrivalCityCode;
    @Setter @Getter private Date flightDate;
    @Setter @Getter private Date createTime;
}