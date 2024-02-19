package zendesk

import sttp.client4.quick._
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration

object Zendesk extends App {
  case class ApiParameters(ticketStatus: String, startDate: String, endDate: String, market: String)

  val username: String = sys.env("ZENDESK_USERNAME")
  val password: String = sys.env("ZENDESK_PASSWORD")
  val url: String = sys.env("ZENDESK_URL")

  def getTicketFirstDate(mkt: String): String = {
    val requestUri =
      uri"""$url/api/v2/search.json?query=type%3Aticket+status%3Aopen&brand%3A\"$mkt\"
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
                                    ): List[ApiParameters] = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val midnightStartDate = LocalDateTime.parse(startDate, formatter).`with`(LocalTime.MIN).format(formatter)
    val midnightEndDate = LocalDateTime.parse(endDate, formatter).`with`(LocalTime.MIN).format(formatter)

    List(ApiParameters(ticketStatus, midnightStartDate, midnightEndDate, market))
  }

  def generateSecondOpenApiParameters(startDate: String, endDate: String,
                                      market: String, ticketStatus: String
                                     ): List[ApiParameters] = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val midnightStartDate = LocalDateTime.parse(startDate, formatter).`with`(LocalTime.MIN)
    val midnightEndDate = LocalDateTime.parse(endDate, formatter).`with`(LocalTime.MIN)
    val numberOfDays = Duration.between(midnightStartDate, midnightEndDate).toDays.toInt

    (0 until numberOfDays).map { i =>
      val start = midnightStartDate.plusDays(i).format(formatter)
      val end = midnightStartDate.plusDays(i + 1).format(formatter)

      ApiParameters(ticketStatus, start, end, market)
    }.toList
  }

  def getTickets(apiParameters: ApiParameters): ujson.Value = {
    val status = apiParameters.ticketStatus
    val start = apiParameters.startDate
    val end = apiParameters.endDate
    val mkt = apiParameters.market

    val requestUri =
      uri"""$url/api/v2/search.json?query=type%3Aticket+created>${start}
           +created<${end}+status%3A${status}&brand%3A"${mkt}"&sort_by=created_at&sort_order=asc"""
    val request = quickRequest
      .get(requestUri)
      .auth.basic(user = username, password = password)
      .send()

    val response = ujson.read(request.body)

    response("next_page")
  }

  val allParameters = generateFirstOpenApiParameters("2022-02-10T00:00:00Z", "2022-04-10T00:00:00Z", "Barbados",
    "open") //++ generateSecondOpenApiParameters("2022-04-10T00:00:00Z", "2022-04-14T00:00:00Z", "Barbados","open"//)

  val test = allParameters.map(i => getTickets(i))

  println(test)
}
