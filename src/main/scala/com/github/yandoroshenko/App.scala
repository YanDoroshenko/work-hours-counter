package com.github.yandoroshenko

import java.io.IOException
import java.time.format.{DateTimeFormatter, TextStyle}
import java.time.{DayOfWeek, LocalDate}
import java.util.Locale

import play.api.libs.json.JsResultException

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout.{HBox, VBox}
import scalafx.util.StringConverter

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 15.05.2017.
  */


object App extends JFXApp with Calendar with HolidayCalendar {

  val countries = Map(Locale.getISOCountries.map(c => new Locale("", c).getDisplayCountry -> new Locale("", c)): _*)

  var holidays: Either[Throwable, Set[Holiday]] = _

  stage = new PrimaryStage {
    maximized = false
    scene = new Scene {
      title = "Work Hours Count"
      content = new VBox {
        val fullTimeSelection = new ToggleGroup()
        val fullTime = for (i <- 1 to 2) yield
          new RadioButton {
            margin = Insets(10, 20, 20, 20)
            text = if (i % 2 == 0) "Part time" else "Full time"
            toggleGroup = fullTimeSelection
            selected = i % 2 == 0
            onAction = _ => {
              hours.foreach(h => h.disable = text.value == "Full time")
              updateSum(sum, dps, hours)
            }
          }

        val dps: Seq[DatePicker] = for (i <- 0 to 1) yield
          new DatePicker(LocalDate.now().withDayOfMonth(1).plusMonths(i).minusDays(i)) {
            margin = Insets(10)
            converter = new StringConverter[LocalDate] {
              private final val f = DateTimeFormatter.ofPattern("dd.MM.yyyy")

              override def toString(t: LocalDate): String = f.format(t)

              override def fromString(string: String): LocalDate =
                if (string != null && !string.isEmpty)
                  LocalDate.parse(string, f)
                else
                  null
            }
            onAction = _ => {
              updateHolidays(dps, locales)
              updateSum(sum, dps, hours)
            }
          }

        val locales = new ComboBox[String](countries.keys.toList.sorted) {
          margin = Insets(10)
          maxWidth = 209
          value = Locale.getDefault().getDisplayCountry
          onAction = _ => {
            updateHolidays(dps, this)
            updateSum(sum, dps, hours)
          }
        }

        updateHolidays(dps, locales)

        val sum = new Label {
          margin = Insets(20)
        }
        val hours: Seq[Spinner[Double]] =
          for (_ <- 1 to 7) yield new Spinner[Double](0, 24, 0, 0.5) {
            margin = Insets(10)
            maxWidth = 75
            onMouseClicked = _ => updateSum(sum, dps, hours)
            onKeyReleased = _ => updateSum(sum, dps, hours)
          }
        val days: Seq[Label] =
          for (i <- 1 to 7) yield new Label(DayOfWeek.of(i).getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()))

        children = Seq(
          new HBox {
            children = dps :+ locales
          },
          new HBox {
            children = fullTime
          },
          new HBox {
            children = hours.zip(days).map(p => new VBox {
              alignment = Pos.Center
              children = Seq(p._2, p._1)
            })
          },
          new HBox {
            children = Seq(sum)
          }
        )
      }
    }
  }

  private def updateHolidays(dps: Seq[DatePicker], locales: ComboBox[String]) =
    holidays = getHolidays(dps.head.value.value, dps.last.value.value, countries(locales.value.value).getISO3Country)

  private def updateSum(sum: Label, dps: Seq[DatePicker], hours: Seq[Spinner[Double]]) =
    sum.text = holidays match {
      case Right(hs) =>
        getDays(
          dps.head.value.value, dps.last.value.value)
          .filterNot(d => hs.map(_.date).contains(d))
          .map(d => hours(d.getDayOfWeek.getValue - 1) match {
            case h if h.disabled.value =>
              if (d.getDayOfWeek.getValue < 6)
                8
              else
                0
            case h => h.value.value
          }
          )
          .sum match {
          case w if w.isWhole => w.toInt.toString
          case f => f.toString
        }

      case Left(_: JsResultException) => "Sorry, selected country is not supported"
      case Left(_: IOException) => "Can't load holidays"
      case Left(_) => "Error occured"
    }
}