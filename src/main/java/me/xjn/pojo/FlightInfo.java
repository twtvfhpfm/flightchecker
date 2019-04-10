package me.xjn.pojo;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

public class FlightInfo
{
    @Getter @Setter private String flightNumber;
    @Getter @Setter private String airlineName;
    @Getter @Setter private String departureDate;
    @Getter @Setter private String arrivalDate;
    @Getter @Setter private String departureCityName;
    @Getter @Setter private String arrivalCityName;
    @Getter @Setter private int price;
    @Getter @Setter private Date checkTime;

    public static class Builder{
        private String flightNumber;
        private String airlineName;
        private String departureDate;
        private String arrivalDate;
        private String departureCityName;
        private String arrivalCityName;
        private int price;
        private Date checkTime;

        public Builder(){
            checkTime = new Date();
        }
        public Builder setFlightNumber(String flightNumber){
            this.flightNumber = flightNumber;
            return this;
        }
        public Builder setAirlineName(String airlineName){
            this.airlineName = airlineName;
            return this;
        }
        public Builder setDepartureDate(String departureDate){
            this.departureDate = departureDate;
            return this;
        }
        public Builder setArrivalDate(String arrivalDate){
            this.arrivalDate = arrivalDate;
            return this;
        }
        public Builder setDepartureCityName(String departureCityName){
            this.departureCityName = departureCityName;
            return this;
        }
        public Builder setArrivalCityName(String arrivalCityName){
            this.arrivalCityName = arrivalCityName;
            return this;
        }
        public Builder setPrice(int price){
            this.price = price;
            return this;
        }
        public Builder setCheckTime(Date checkTime){
            this.checkTime = checkTime;
            return this;
        }
        public FlightInfo build(){
            FlightInfo info = new FlightInfo();
            info.setFlightNumber(flightNumber);
            info.setAirlineName(airlineName);
            info.setArrivalDate(arrivalDate);
            info.setDepartureDate(departureDate);
            info.setDepartureCityName(departureCityName);
            info.setArrivalCityName(arrivalCityName);
            info.setPrice(price);
            info.setCheckTime(checkTime);
            return info;
        }
    }
}