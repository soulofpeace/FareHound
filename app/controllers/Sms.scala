package controllers

import play.api._
import play.api.mvc._
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import com.hoiio.sdk.Hoiio

object Sms extends Controller {

  // The phone number that receives SMS
  val smsPhoneNumber = "+6585372489"	// Singtel Dongle
    
  // Hoiio credentials
  val appId = "Q8EdHclHLbzqhB3I"
  val accessToken = "wejoJ4gRcWYxO3oR"

  
  /**
   * The primary action when SMS is received
   */
  def received(from: String, to: String, msg: String) = Action { implicit request =>
    // Received an SMS
    Logger.info("SMS Received..")
    Logger.info("From (" + countryForNumber(from) + ") : " + from)
    Logger.info("To (" + countryForNumber(to) + ") : " + to)
    Logger.info("SMS: " + msg)
    
    // Parse the SMS message
    Logger.info("Interpreting SMS message..")
    try {
    	val search = parseMsg(msg, to)
    	Logger.info("Flight from: " + search._1)
	    Logger.info("Flight to: " + search._2)
	    Logger.info("When: " + search._3)
	    Logger.info("Budget: " + search._4)
	    
	    // Search confirm
	    val date = new SimpleDateFormat("d MMM yyyy").format(search._3)
	    val text = "Your search for flight is confirmed.\n\nfrom " + search._1 + "\nto " + search._2 + "\non " + date + "\nwith budget $" + search._4
	
	    // Send an SMS to confirm
	    // Uncomment to send out a real SMS
	    // send(from, text)
	    
	    Ok(text)
    } catch {
      	case e => Ok("Unrecognized SMS. Example: Singapore to Tokyo on Dec 25 with $400")
    }
  }

  // For Sently
  def receivedSently(from: String, text: String) = Action {
    received(from, smsPhoneNumber, text)
  }

  // For Nexmo
  def receivedNexmo(msisdn: String, to: String, text: String) = Action {
    received("+" + msisdn, "+" + to, text)
  }
  
  // For Nexmo verification of URL
  def receivedNexmoHead() = Action {
    Ok("")
  }
  
  
  /**
   * Parse an SMS message and returns the flight search detail (from, to, when, budget). 
   * As 'from' could be omitted, it is inferred from the country of the phone number that receives the SMS. 
   * 
   * Recommended Format:
   *	Singapore to Tokyo on Dec 25 with $400
   * 
   * Other Formats:
   *	Tokyo on Dec 25 with $400			// Implicit from (assumed by Hoiio number)
   *	Tokyo on Dec 25						// No budget
   *	
   *    -- others --
   *	Tokyo on December 25				// month format in full
   *	Tokyo on Dec 25 with 399.99			// $ is optional
   *	Tokyo on Feb 14						// 14 Feb 2013
   *	tokyo ON feb  14						// Any letter casing, any amount of whitespaces 
   */
  def parseMsg(msg: String, phoneNumberReceived: String) : (String, String, Date, Double) = {
    // The defaults
	var from: String = null
	var to: String = null
	var when: Date = null
	var budget: Double = -1
	
	try {
		// Convert all to lower case
		// Prefix m for String
	    // Prefix a for Array[String]
		val m = msg toLowerCase

		// Split (from to) and (when budget)
		var aSplitOn = m split " on "
		var mCountries = aSplitOn(0)
		var mDateBudget = aSplitOn(1)
		
		// Handle (from to) 
		var aFromTo = mCountries split " to "
		if (aFromTo.length > 1) {
		  from = aFromTo(0)
		  to = aFromTo(1)
		} else {
		  from = null
		  to = aFromTo(0)
		}
		
		// Handle (when)
		var mDate: String = null
		var mBudget: String = null
		var aDateBudget = mDateBudget split " with "
		if (aDateBudget.length > 1) {
		  mDate = aDateBudget(0)
		  mBudget = aDateBudget(1)
		} else {
		  mDate = aDateBudget(0)
		}
		when = new SimpleDateFormat("MMM d").parse(mDate)
		var whenCal = Calendar.getInstance()
		whenCal.setTime(when)
		// Make sure the when is in the future
		while (whenCal before Calendar.getInstance())
			whenCal.set(Calendar.YEAR,  whenCal.get(Calendar.YEAR) + 1)
		when = whenCal.getTime()
		
		// Handle (budget)
		if (mBudget != null)
		  mBudget = mBudget.replace("$", "")
		  budget = mBudget.toDouble
		
	} catch {
		case e: Exception => {
			Logger.error(e.getMessage())
			throw e
		}
	}
	
	// If from is not specified in msg, we use the inferred
	if (from == null)
		from = countryForNumber(phoneNumberReceived).toLowerCase()
	
	return (from trim, to trim, when, budget)
  }
  
  
  
  /**
   * Return the country for a phone number
   */
  def countryForNumber(phoneNumber: String) = phoneNumber match {
    case p if p.startsWith("+1") => "USA"
    case p if p.startsWith("+65") => "Singapore"
    case p if p.startsWith("+") => "Country X"
    case _ => "Unknown"
  }
  
  
  
  /**
   * Hoiio SMS API to send a message to a phone number
   * phoneNumber in full international format eg. "+6590000000"
   */
  def send(phoneNumber: String, msg: String, senderName: String = smsPhoneNumber) {  
	  val hoiio = new Hoiio(appId, accessToken)
	  hoiio.getSmsService().send(phoneNumber, msg, senderName, null, null);
  }
  
  
}


