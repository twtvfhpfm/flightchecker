CREATE TABLE QueryFlight (
    id int not null primary key auto_increment,
    departureCityName varchar(32),
    arrivalCityName varchar(32),
    departureCityCode varchar(32),
    arrivalCityCode varchar(32),
    flightDate date not null,
    createTime timestamp not null default CURRENT_TIMESTAMP
);
