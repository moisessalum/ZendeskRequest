package zendesk

import sttp.client4.quick._
import sttp.client4.upicklejson.default._
import upickle.default._

object Zendesk extends App {
  val username: String = sys.env("ZENDESK_USERNAME")
  val password: String = sys.env("ZENDESK_PASSWORD")

  val response = quickRequest
    .get(uri"https://support-digicel-bb-play.zendesk.com/api/v2/search.json?query=type%3Aticket+status%3Aopen&brand%3A\"Barbados\"&sort_by=created_at&sort_order=asc")
    .auth.basic(user = username, password = password)
    .send()

  val jsonString = response.body
  val json: ujson.Value  = ujson.read(jsonString)

  println(json)

  // println(json.body.getClass)
  // prints the type of the body

  // get all the keys on an json
  // keys: Iterable[String] = json.obj.keySet

  // prints some JSON string
}
