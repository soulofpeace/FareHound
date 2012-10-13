FareHound
=========

FareHound is an application that uses the Wego Flight Api <http://www.wego.com/api/flights/docs> and the Hoiio Api <http://developer.hoiio.com/docs/> to help user to find fares which matches their criteria

* SFO on Dec 25
* SIN to SFO on Dec 25 with $600
* Singapore to Tokyo Narita On Dec 25

## Developers
---
1. Choon Kee, Developer @ Wego
2. Junda, Developer @ Hoiio


## Technology Stack
***
All this is make possible by the Typesafe Stack

* Scala 2.9.1
* Akka 2.0.3
* Play! 2.0.4

with data persisted on Redis <http://redis.io>, a key value store


## Overview of the solution
***
These are the component that made up the solution

### Sms Controller
Responsible for parsing the SMS request
  
### Scheduler Component
Responsible for the running hourly update

### Search Component
Run as a Akka FSM. Conduct the Search and the Pull operation and Finding the minimum price of each search

### Checker Component
Receive the minimum price of each search request and check against the user criteria. If it matches the user criteria, send the cheapest price to the user

### BestPrice Component
Call the Wego airfare API to get the cheapest price for the search for the past 3 month

### Notifier Component
Send out the sms to notify the user

### Exchange Rate Component
Update the exchange rate using the Open Exchange rate api <http://openexchangerates.org>

## Requirements
---
Need to set these environment variables in your .bashrc, .zshrc etc,

1. API Key From Hoiio  <http://developer.hoiio.com/docs/> 
	* HOIIO_APP_ID
	* HOIIO_ACCESS_TOKEN
2. API Key From Wego   <http://www.wego.com/api/flights/docs>
	* WEGO_API_KEY
3. API Key From Open Exchange <http://openexchangerates.org>
	* OPEN_EXCHANGE_KEY
4. REDIS_URL e.g. redis://user:password@localhost:6379/
	* REDIS_URL

Then just run it

**play run**

##Future Works
---
1. Support more form of queries
2. Web Interface
3. Email Notification






