package controllers

import play.api._
import play.api.mvc._
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import com.hoiio.sdk.Hoiio
import scala.io.Source
import scala.collection.mutable.HashMap
import models.Airport

object Sms extends Controller {

  // The phone number that receives SMS
  val smsPhoneNumber = "+6585372489"	// Singtel Dongle
    
  // Hoiio credentials
  val appId = System.getenv("HOIIO_APP_ID")
  val accessToken = System.getenv("HOIIO_ACCESS_TOKEN")

  // Reply texts
  val textOk = "Flight alert is confirmed."
  val textNoMatch = "Sorry, we could not recognize "
  val textMultipleMatch = "Sorry, we matched multiple airports for " 
  val textTryAgain = "Please refine your input."
  val textErrorFormat = "Unrecognized format. SMS Example: Singapore to Tokyo on Dec 25 with $400"

    // A representation for no budget
  var nobudget: Double = -99999.99999
  val currencyFormat = new java.text.DecimalFormat("##,###")
  
  /**
   * The primary action when SMS is received
   */
  def received(from: String, to: String, msg: String) = Action { implicit request =>
    // Received an SMS
    Logger.info("SMS Received..")
    Logger.info("From : " + from)
    Logger.info("To : " + to)
    Logger.info("SMS: " + msg)
    
    // Parse the SMS message
    var text:String = null  // SMS reply
    try {
      // Extract the 4 pieces of information. Possible to throw exception in parseMsg.
    	Logger.info("Interpreting SMS message..")
    	val search = parseMsg(msg, to)
    	
    	// Make sure the airports are exact
      var airportFrom:Airport = null
      var airportTo:Airport = null
    	for (s <- List(search._1, search._2)) {
    	  // Make sure we have 1 exact match
        var airports: List[Airport] = searchAirport(s)
        if (airports.length == 0) {
           text = textNoMatch + s
        } else if (airports.length > 1) {
           var airportList = ""
           for (airport <- airports)
             airportList = airportList + airport.name + " (" + airport.code + ")\n"
           if (airportList.length > 80) 
             airportList = airportList.substring(0, 80) + " ..."
           text = textMultipleMatch + s + ". " + textTryAgain + "\n\n" + airportList        
        } else {
          // Else it's 1 exact match. Good.
          if (s == search._1) 
            airportFrom = airports(0)
          else 
            airportTo = airports(0)
        } 
    	}
    	
    	if (airportFrom != null && airportTo != null) {
    	  // Airports are good. Now we deal with date and budget
    	  val date = new SimpleDateFormat("d MMM yyyy").format(search._3)
    	  var budget:String = null
    	  if (search._4 != nobudget) {
      	  if (search._4 < 0) 
            throw new Exception("Budget cannot be negative")
          budget = currencyFormat.format(search._4)  
    	  }
        
        // Search confirmed
    	  Logger.info("Flight from: " + airportFrom.name)
        Logger.info("Flight to: " + airportTo.name)
        Logger.info("When: " + date)
        text = textOk + "\n\nDepart: " + airportFrom.name + "\nArrive: " + airportTo.name + "\nOn: " + date
        if (budget != null) {
          Logger.info("Budget: " + budget)
          text = text + "\nBudget: $" + budget
        }
    	  
        // TODO: Pass to CK
    	}

    } catch {
      	case e => {
      	  e.printStackTrace()
      	  Logger.error(e.toString())
      	  text = textErrorFormat
      	}
    }
    
    // Send an SMS (ok or error text)
    Logger("Final text: " + text)
    send(from, text)
    Ok(text)
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
   * 	SIN to SFO on Dec 25 with $400
   *	Singapore to San Francisco on Dec 25 with $400
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
  def parseMsg(msg: String, phoneNumberReceived: String=smsPhoneNumber) : (String, String, Date, Double) = {
    // The defaults
	var from: String = null
	var to: String = null
	var when: Date = null
	var budget: Double = nobudget
  
	try {
		// Convert all to upper case
		// Prefix m for String
	    // Prefix a for Array[String]
		val m = msg toUpperCase()

		// Split (from to) and (when budget)
		var aSplitOn = m split " ON "
		var mCountries = aSplitOn(0)
		var mDateBudget = aSplitOn(1)
		
		// Handle (from to) 
		var aFromTo = mCountries split " TO "
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
		var aDateBudget = mDateBudget split " WITH "
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
		if (mBudget != null) {
		  mBudget = mBudget.replace("$", "")
		  budget = mBudget.toDouble
		}
		
	} catch {
		case e: Exception => {
			Logger.error(e.getMessage())
			throw e
		}
	}
	
	// If from is not specified in msg, we use the inferred
	if (from == null)
		from = airportFromNumber(phoneNumberReceived)
	
	return (from trim, to trim, when, budget)
  }
  
  
  
  /**
   * Return the country for a phone number
   */
  def airportFromNumber(phoneNumber: String) = phoneNumber match {
    case p if p.startsWith("+1") => "SFO"
    case p if p.startsWith("+65") => "SIN"
    case p if p.startsWith("+") => "SIN"
    case _ => "Unknown"
  }
  
  
  
  /**
   * Hoiio SMS API to send a message to a phone number
   * phoneNumber in full international format eg. "+6590000000"
   */
  def send(phoneNumber: String, msg: String, senderName: String = smsPhoneNumber) {
    Logger.info("Sending SMS to " + phoneNumber + ": " + msg)
    // Comment out to send a real SMS
//    return
    
    if (appId == null || accessToken == null) {
      val error = "Hoiio app_id and access_token missing. Enter them in .env as environment variables."
      Logger.error(error)
      throw new Exception(error)
    }
	  val hoiio = new Hoiio(appId, accessToken)
	  val res = hoiio.getSmsService().send(phoneNumber, msg, senderName, null, null);
    Logger.info("Hoiio res: " + res.getContent())
  }
  
  
  /**
   * Send an SMS for price alert
   */
  def sendPriceAlert(phoneNumber: String, currentPrice: Double, bestPrice: Double, link: String) {
    val text = ( 
        "Current Price: $" + currencyFormat.format(currentPrice) + 
        "\nLowest in last 3 mths: $" + currencyFormat.format(bestPrice) +
        "\nBuy at " + link +
        "\n\nHey, I'll keep a tight watch and SMS on any price drop ^^" )
        
    send(phoneNumber, text)
  }
  
  
  /**
   * Returns list of Airports matched. Great if return 1.
   * 
   * Try to match by airport code (0/1)
   * Else match by airport name (0..n)
   * Else match by country name (0..n)
   * Else empty List
   */
  def searchAirport(m: String) : List[Airport] = {
    val M = m toUpperCase
    val airports = Global.airports
    
    // 1. Check if exact match of airport code
    for ((code, airport) <- airports) {
      if (code == M) {
        return List(airport)
      }
    }
    
    // 2. Match by airport name
    var matches = List[Airport]()
    for ((code, airport) <- airports) {
      if (airport.name.toUpperCase() contains M) {
        matches = airport :: matches 
      }
    }
    if (matches.length > 0)
      return matches
    
    // 3. Match by country name
    matches = List[Airport]()
    for ((code, airport) <- airports) {
      if (airport.countryName.toUpperCase() contains M) {
        matches = airport :: matches 
      }
    }

    return matches
  }

  
}


