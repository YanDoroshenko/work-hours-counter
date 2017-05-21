package com.github.yandoroshenko.workhourscounter

import java.time.LocalDate

import play.api.libs.json.{JsArray, JsNumber, Json}

import scala.util.{Failure, Success, Try}

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 15.05.2017.
  */
trait HolidayCalendar extends Calendar {

  case class Holiday(date: LocalDate, name: String)

  private val url: String = "http://kayaposoft.com/enrico/json/v1.0/?action=getPublicHolidaysForMonth&month=%s&year=%s&country=%s"

  def getHolidays(from: LocalDate, to: LocalDate, countryCode: String): Either[Throwable, Set[Holiday]] =
    Try(getDays(from, to)
      .map(_.withDayOfMonth(1))
      .distinct
      .map(d =>
        Json.parse(io.Source.fromURL(String.format(url, d.getMonthValue.toString, d.getYear.toString, countryCode)).mkString)
          .as[JsArray].value
          .map(v => v \ "date" -> v \ "localName")
          .map(v => Holiday(
            LocalDate.of(
              (v._1 \ "year").as[JsNumber].value.toIntExact,
              (v._1 \ "month").as[JsNumber].value.toIntExact,
              (v._1 \ "day").as[JsNumber].value.toInt),
            v._2.as[String])))) match {
      case Success(a) => Right(a.reduceLeft(_ ++ _).toSet)
      case Failure(e) => Left(e)
    }
}