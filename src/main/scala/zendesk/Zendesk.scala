package zendesk

import sttp.client4.quick._
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Zendesk extends App {
  case class ApiParameters(ticketStatus: String, startDate: String, endDate: String, market: String)

  val username: String = sys.env("ZENDESK_USERNAME")
  val password: String = sys.env("ZENDESK_PASSWORD")
  val url: String = sys.env("ZENDESK_URL")

  def getTicketFirstDate(mkt: String): String = {
    val requestUri = uri"""$url/api/v2/search.json?query=type%3Aticket+status%3Aopen&brand%3A\"$mkt\"
                            &sort_by=created_at&sort_by=created_at"""
    val firstOpenRequest = quickRequest
      .get(requestUri)
      .auth.basic(user = username, password = password)
      .send()
    val response = ujson.read(firstOpenRequest.body)

    response.obj.values(0)(0)("created_at").str // "2020-02-06T19:14:44Z"
  }

  def generateFirstOpenApiParameters(startDate: String, endDate: String,
                                 market: String, ticketStatus: String
                                ): ApiParameters = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val midnightStartDate = LocalDateTime.parse(startDate, formatter).`with`(LocalTime.MIN).format(formatter)
    val midnightEndDate = LocalDateTime.parse(endDate, formatter).`with`(LocalTime.MIN).format(formatter)

    ApiParameters(ticketStatus, midnightStartDate, midnightEndDate, market)
  }

  println(generateFirstOpenApiParameters(startDate = "2020-02-06T19:14:44Z", endDate="2020-02-06T19:14:44Z",
    market="Test", ticketStatus="open"))

}
