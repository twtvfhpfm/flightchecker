CREATE TABLE FlightInfo(
    id int not null primary key auto_increment,
    flightNumber varchar(32) not null,
    airlineName varchar(32) not null,
    departureDate varchar(32) not null,
    arrivaldate varchar(32) not null,
    departureCityName varchar(32) not null,
    arrivalCityName varchar(32) not null,
    price int not null,
    checkTime timestamp not null default CURRENT_TIMESTAMP
);
