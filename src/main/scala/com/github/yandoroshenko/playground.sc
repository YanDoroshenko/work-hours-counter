import java.time.LocalDate

import play.api.libs.json.{JsArray, JsNumber, Json}

import scala.util.{Failure, Success, Try}

val url = "http://kayaposoft.com/enrico/json/v1.0/?action=getPublicHolidaysForMonth&month=5&year=2017&country=cze"

Try(Json.parse(io.Source.fromURL(url).mkString).as[JsArray]) match {
  case Success(a) =>
    Right(a.as[JsArray].value
      .map(v => v \ "date" -> v \ "localName")
      .map(v => LocalDate.of((v._1 \ "year").as[JsNumber].value.toIntExact,
        (v._1 \ "month").as[JsNumber].value.toIntExact,
        (v._1 \ "day").as[JsNumber].value.toInt)))
  case Failure(e) => Left(e)
};
javafx.scene.text.Font.getFamilies.asScala.mkString("\n")
