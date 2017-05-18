package com.github.yandoroshenko

import java.time.format.{DateTimeFormatter, TextStyle}
import java.time.{DayOfWeek, LocalDate}
import java.util.Locale

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{ComboBox, DatePicker, Label, Spinner}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.util.StringConverter

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 15.05.2017.
  */


object App extends JFXApp with Calendar with HolidayCalendar with WorkHourCounter {

  val isoLocales = Map(Locale.getISOCountries.map(c => c -> new Locale("", c)): _*)

  var holidays: Either[Throwable, Set[Holiday]] = _

  stage = new PrimaryStage {
    maximized = false
    scene = new Scene {
      title = "Work Hours Count"
      content = new VBox {
        val dps: Seq[DatePicker] = for (i <- 0 to 1) yield
          new DatePicker(LocalDate.now().withDayOfMonth(1).plusMonths(i).minusDays(i)) {
            margin = Insets(20)
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

        val locales = new ComboBox[String](Locale.getISOCountries) {
          margin = Insets(20)
          value = Locale.getDefault().getCountry
          onAction = _ => {
            updateHolidays(dps, this)
            updateSum(sum, dps, hours)
          }
        }

        updateHolidays(dps, locales)

        val sum = new Label {
          margin = Insets(20)
          text = dps.head.value.value.toString
        }
        val hours: Seq[Spinner[Double]] =
          for (_ <- 1 to 7) yield new Spinner[Double](0, 24, 0, 0.5) {
            editable = true
            margin = Insets(10)
            maxWidth = 75
            onMouseClicked = _ =>
              updateSum(sum, dps, hours)

            onKeyReleased = _ =>
              updateSum(sum, dps, hours)
          }
        val days: Seq[Label] =
          for (i <- 1 to 7) yield new Label(DayOfWeek.of(i).getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()))

        children = Seq(
          new HBox {
            children = List(dps.head, dps.last, locales)
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
    holidays = getHolidays(dps.head.value.value, dps.last.value.value, isoLocales(locales.value.value).getISO3Country)

  private def updateSum(sum: Label, dps: Seq[DatePicker], hours: Seq[Spinner[Double]]) =
    sum.text = holidays match {
      case Right(h) =>
        getDays(
          dps.head.value.value, dps.last.value.value)
          .filterNot(d => h.map(_.date).contains(d))
          .map(d => hours(d.getDayOfWeek.getValue - 1).value.value)
          .sum
          .toString
      case Left(_) => "Error"
    }
}