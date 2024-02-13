package zendesk

import sttp.client4.quick._

object Zendesk extends App {
  val username: String = sys.env("ZENDESK_USERNAME")
  val password: String = sys.env("ZENDESK_PASSWORD")
  val url: String = sys.env("ZENDESK_URL")

  def getTicketFirstDate(mkt: String): String = {
    val requestUri = uri"$url/api/v2/search.json?query=type%3Aticket+status%3Aopen&brand%3A\"$mkt\"&sort_by=created_at&sort_by=created_at"
    val firstOpenRequest = quickRequest
      .get(requestUri)
      .auth.basic(user = username, password = password)
      .send()
    val response = ujson.read(firstOpenRequest.body)

    response.obj.values(0)(0)("created_at").str.split("T")(0)
  }


  val firstDate = getTicketFirstDate(mkt = "Barbados")
  println(firstDate)
}
