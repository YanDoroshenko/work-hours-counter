package com.github.yandoroshenko.workhourscounter.calendar

import java.time.LocalDate

import play.api.libs.json.{JsArray, JsNumber, Json}

import scala.util.{Failure, Success, Try}

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 15.05.2017.
  */
trait HolidayCalendar extends Calendar {

  case class Holiday(date: LocalDate, name: String)

  private val BaseUrl: String = "http://kayaposoft.com/enrico/json/v1.0/"

  def getHolidays(from: LocalDate, to: LocalDate, countryCode: String): Either[Throwable, Set[Holiday]] = {
    Try(getDays(from, to)
      .map(_.withDayOfMonth(1))
      .distinct
      .map(d => {
        val url = f"""$BaseUrl?action=getPublicHolidaysForMonth&month=${d.getMonthValue().toString()}&year=${d.getYear.toString()}&country=$countryCode"""
        log.info(f"""Reading data from $url""")
        val response = io.Source.fromURL(url).mkString
        log.info(f"""Received response from $url""")
        log.debug(response)
        Json.parse(response)
          .as[JsArray].value
          .map(v => v \ "date" -> v \ "localName")
          .map(v => Holiday(
            LocalDate.of(
              (v._1 \ "year").as[JsNumber].value.toIntExact,
              (v._1 \ "month").as[JsNumber].value.toIntExact,
              (v._1 \ "day").as[JsNumber].value.toInt),
            v._2.as[String]))
      })) match {
      case Success(a) =>
        val r = a.reduceLeft(_ ++ _).toSet
        log.info(f"""Holidays between $from and $to: ${r.mkString("[", "\n", "]")}""")
        Right(r)
      case Failure(e) =>
        log.error(f"""Error getting holidays: ${e.getLocalizedMessage()}""", e)
        Left(e)
    }
  }
}