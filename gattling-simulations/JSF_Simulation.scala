
import scala.concurrent.duration._

import scala.util.Random

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class JSF_Simulation extends Simulation {

	val viewStateRegexExpression = "<input\\stype=\"hidden\"\\sname=\"javax.faces.ViewState\"\\sid=\".+:javax.faces.ViewState:0\"\\svalue=\"([\\-\\d:]+)\""

	val httpProtocol = http
		.baseUrl("http://localhost:8080")
		.inferHtmlResources()
		.acceptHeader("*/*")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-US,en;q=0.5")
		.userAgentHeader("PuppetMaster Browser")

	val headers_default = Map(
		"Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
		"Upgrade-Insecure-Requests" -> "1")

	val headers_accept_css = Map("Accept" -> "text/css,*/*;q=0.1")

	val headers_JSF = Map(
		"Accept" -> "application/xml, text/xml, */*; q=0.01",
		"Content-Type" -> "application/x-www-form-urlencoded; charset=UTF-8",
		"Faces-Request" -> "partial/ajax",
		"X-Requested-With" -> "XMLHttpRequest")

	val headers_logout = Map("Upgrade-Insecure-Requests" -> "1")

	object JsfRequestHandler {

		def doInitialRequest(get : String) = http("InitialRequest")
			.get(get)
			.check(bodyString.saveAs("response"))
			.check(regex(viewStateRegexExpression).find.saveAs("viewState"))
			.headers(headers_default)
            .check(status.is(200))

		def select(selected : Int) =  
            http("Select")
			.post("/experiment-lazy-filtering/index.jsf")
			.headers(headers_JSF)
			.formParam("javax.faces.partial.ajax", "true")
			.formParam("javax.faces.source", "j_idt16:dummyTable")
			.formParam("javax.faces.partial.execute", "j_idt16:dummyTable")
			.formParam("javax.faces.partial.render", "growl")
			.formParam("javax.faces.behavior.event", "rowSelect")
			.formParam("javax.faces.partial.event", "rowSelect")
			.formParam("j_idt16:dummyTable_instantSelectedRowKey", selected)
			.formParam("j_idt16:dummyTable:j_idt20:filter", "")
			.formParam("j_idt16:dummyTable_selection", selected)
			.formParam("j_idt16:dummyTable_scrollState", "0,0")
			.formParam("javax.faces.ViewState", "${viewState}")
            .check(status.is(200))
    }

	val scn = scenario("Simulation_Select")
		.exec(
			JsfRequestHandler.doInitialRequest("/experiment-lazy-filtering/index.jsf")
		).exec( session => {
  			println( "JSF ViewState: " + session( "viewState" ).as[String] )
  			session
			}
		)
		.exec(JsfRequestHandler.select(Random.nextInt(10000)))
		.pause(1)
		.exec(JsfRequestHandler.select(Random.nextInt(10000)))
		.pause(1)
		.exec(JsfRequestHandler.select(Random.nextInt(10000)))
		.pause(1)

	setUp(scn.inject(rampUsersPerSec(100/60) to(200/60) during(2 minutes))).protocols(httpProtocol)
	//setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}

