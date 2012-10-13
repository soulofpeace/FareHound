FareHound
=========

FareHound is an application that uses the Wego Flight Api <http://www.wego.com/api/flights/docs> and the Hoiio Api <http://developer.hoiio.com/docs/> to help user to find fares which matches their criteria

* SFO on Dec 25
* SIN to SFO on Dec 25 with $600
* Singapore to Tokyo Narita On Dec 25

## How It works
---
Steps

1. User send in an SMS with a query
	* SFO on Dec 25 with $600
2. Confirmation SMS will be sent to confirm successful registration of query
3. An initial search will be started and if the search price is below the user expected price, an SMS is sent to the user with current search price. In addition, the best price for the same search for the past 3 months and a booking url will also be sms back to the user
4. Every 1 hour, A scheduler run all the registered user queries and fire off the searches.


User can at anytime send back the same search with a different price expectation. 

The system will only allow 1 price expectation for the same search query for the same user 


## Developers
---
1. Choon Kee, Developer @ Wego
2. Junda, Developer @ Hoiio


## Technology Stack
---

All this is make possible by the Typesafe Stack

* Scala 2.9.1 <http://scala-lang.org>
* Akka 2.0.3 <http://akka.io>
* Play! 2.0.4 <http://www.playframework.org/>
* Scala Redis 2.5 <https://github.com/debasishg/scala-redis/>
* Dispatch 0.9.2 <http://dispatch.databinder.net/Dispatch.html>
* Kryo 2.19 <http://code.google.com/p/kryo/>

with data persisted on Redis <http://redis.io>, a key value store


## Overview of the solution
---
These are the component that made up the solution. AlertService is using Cake pattern for dependency injection

### Communication Component and Sms Parsing Component

#### Sms Controller
---
Responsible for parsing the SMS request and sending the SMS. Created using Play Framework Controller
 
### Alert Service
---
#### Scheduler Component
Responsible for the running hourly update

#### Search Component
LoadBalancedActor: Run as a Akka FSM. Conduct the Search and the Pull operation and Finding the minimum price of each search. Queued Pending Search Request. 

#### Checker Component
LoadBalancedActor: Receive the minimum price of each search request and check against the user criteria. If it matches the user criteria, send the cheapest price to the user

#### BestPrice Component
LoadBalancedActor: Call the Wego airfare API to get the cheapest price for the search for the past 3 month

#### Notifier Component
LoadBalancedActor: Send out the sms to notify the user

#### Exchange Rate Component
Update the exchange rate using the Open Exchange rate api <http://openexchangerates.org>

## Benefits
---
* Asynchonous Polling for Flight Prices
* Communication between various components are done through Actor message passing
* Small Memory Footprint. Only User Criteria and the Cheapest Price are stored


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






