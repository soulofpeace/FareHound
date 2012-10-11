package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import controllers.Sms
import java.util.Date
import java.util.Calendar
import controllers.Global

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
class SmsSpec extends Specification {
  
  "airport codes with date budget" in {
    val sms = "SIN to SIN on Dec 25 with $400"
    val search = Sms.parseMsg(sms)
    
    search._1 must be equalTo("SIN")
    search._2 must be equalTo("SIN")
    var c:Calendar = Calendar.getInstance()
    c.set(2012, 11, 25, 0, 0, 0)
    search._3.toString() must be equalTo(c.getTime().toString())
    search._4 must be equalTo(400)
  }
  
  
  "airport codes with date budget 2" in {
    val sms = "SIN to SFO on Dec 25 with $400"
    val search = Sms.parseMsg(sms)
    
    search._1 must be equalTo("SIN")
    search._2 must be equalTo("SFO")
    var c:Calendar = Calendar.getInstance()
    c.set(2012, 11, 25, 0, 0, 0)
    search._3.toString() must be equalTo(c.getTime().toString())
    search._4 must be equalTo(400)
  }
  
  
  "1 airport code with date budget" in {
    val sms = "SFO on Dec 25 with $400"
    val search = Sms.parseMsg(sms)
    
    search._1 must be equalTo("SIN")
    search._2 must be equalTo("SFO")
    var c:Calendar = Calendar.getInstance()
    c.set(2012, 11, 25, 0, 0, 0)
    search._3.toString() must be equalTo(c.getTime().toString())
    search._4 must be equalTo(400)
  }
  
  
  "airport codes with date budget mixed caps" in {
    val sms = "sin to Sin oN DEC 25 wITh $400"
    val search = Sms.parseMsg(sms)
    
    search._1 must be equalTo("SIN")
    search._2 must be equalTo("SIN")
    var c:Calendar = Calendar.getInstance()
    c.set(2012, 11, 25, 0, 0, 0)
    search._3.toString() must be equalTo(c.getTime().toString())
    search._4 must be equalTo(400)
  }
  
  
//  "airport names (single match) with date budget 2" in {
//    val sms = "Singapore to San Franciso on Dec 25 with $400"
//    val search = Sms.parseMsg(sms)
//    
//    search._1 must be equalTo("SIN")
//    search._2 must be equalTo("SFO")
//    var c:Calendar = Calendar.getInstance()
//    c.set(2012, 11, 25, 0, 0, 0)
//    search._3.toString() must be equalTo(c.getTime().toString())
//    search._4 must be equalTo(400)
//  }    
  
  
  
  
  ////////////////////////////
  // Test on searchAirport
  ////////////////////////////
  
  "Search Singapore (1 match)" in {
    Global.onStart(null)
    val airports = Sms.searchAirport("Singapore")
    airports(0).code must be equalTo("SIN")
    airports.length must be equalTo(1)
  }
  
  "Search Singapore (1 match) mixed case" in {
    Global.onStart(null)
    val airports = Sms.searchAirport("sinGApoRe")
    airports(0).code must be equalTo("SIN")
    airports.length must be equalTo(1)
  }
  
  "Search San Francisco (1 match)" in {
    Global.onStart(null)
    val airports = Sms.searchAirport("San Francisco")
    airports(0).code must be equalTo("SFO")
    airports.length must be equalTo(1)
  }
  
  "Search Tokyo (3 matches)" in {
    Global.onStart(null)
    val airports = Sms.searchAirport("Tokyo")
    airports.length must be equalTo(3)
  }
  
  "Search Japan (70 matches)" in {
    Global.onStart(null)
    val airports = Sms.searchAirport("Japan")
    airports.length must be equalTo(70)
  }
  
  
  
  ////
  // Test the controller
  ////
  
  "Test airport_from 0 match" in {
    val result = controllers.Sms.received("+6590001111", "+6585372489", "SINXXX to SFO on Dec 25 with $400")(FakeRequest())
	contentAsString(result) must contain(controllers.Sms.textNoMatch)
  }
  
  "Test airport_from n=3 match" in {
    val result = controllers.Sms.received("+6590001111", "+6585372489", "Tokyo to SFO on Dec 25 with $400")(FakeRequest())
	contentAsString(result) must contain(controllers.Sms.textMultipleMatch)
  }
  
  "Test airport_from n=70 match" in {
    val result = controllers.Sms.received("+6590001111", "+6585372489", "Japan to SFO on Dec 25 with $400")(FakeRequest())
	contentAsString(result) must contain(controllers.Sms.textMultipleMatch)
	contentAsString(result) must contain("abc")
  }
  
  
}