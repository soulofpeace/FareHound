package controllers

import play.api._
import scala.collection.mutable.HashMap
import scala.io.Source
import models.Airport

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started!!!")
    setupAirports()
  }  
  
  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }  
    
  // A Map of airportCode -> Airport
  var airports = new HashMap[String, Airport]
  
  /**
   * Read the airports.csv
   */
  def setupAirports() {
    if (airports.size == 0) {
	    val is = Application.getClass().getResourceAsStream("/public/airports.csv")    
	    val src = Source.fromInputStream(is)
	    val iter = src.getLines
	    for (s <- iter) {
	      // "Airport name","Airport code","Country code","Country name"
	      val data = s.split("\",\"")
	      if (data.length == 4) {
	        val airportCode:String = data(1).replaceAll("\"", "")
	        val airport = new Airport(
	            airportCode, 
	            data(0).replaceAll("\"", ""), 
	            data(2).replaceAll("\"", ""), 
	            data(3).replaceAll("\"", "") 
	            )
	    	airports += airportCode -> airport 
	      } else {
	    	println("Must be 4 element in CSV. Data: " + s)
	      }
	    }
    }
    // println(airports)
  }
    
}