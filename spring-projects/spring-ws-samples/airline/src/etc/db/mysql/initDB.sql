CREATE DATABASE airline;

GRANT ALL ON airline.* TO airline@localhost IDENTIFIED BY 'airline';

USE airline;

CREATE TABLE CUSTOMER (
	 ID INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
	 FIRST_NAME VARCHAR(30),
	 LAST_NAME VARCHAR(30)
);

CREATE TABLE FLIGHT (
	ID INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
	NUMBER VARCHAR(20) NOT NULL,
	DEPARTURE_TIME DATETIME NOT NULL,
	DEPARTURE_AIRPORT CHAR(3) NOT NULL REFERENCES AIRPORT(CODE),
	ARRIVAL_TIME DATETIME NOT NULL,
	ARRIVAL_AIRPORT CHAR(3) NOT NULL REFERENCES AIRPORT(CODE),
	SERVICE_CLASS VARCHAR(10) NOT NULL,
	UNIQUE KEY IDX_NUMBER (NUMBER)
);

CREATE TABLE TICKET (
	ID INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
	ISSUE_DATE DATE NOT NULL,
	CUSTOMER_ID INT(4) UNSIGNED NOT NULL REFERENCES CUSTOMER(ID),
	FLIGHT_ID INT(4) UNSIGNED NOT NULL REFERENCES FLIGHT(ID)
);

CREATE TABLE AIRPORT (
	CODE CHAR(3) NOT NULL PRIMARY KEY,
	NAME VARCHAR(20) NOT NULL,
	CITY VARCHAR(20) NOT NULL
);