package com.github.yandoroshenko.workhourscounter

import java.io.IOException
import java.time.format.{DateTimeFormatter, TextStyle}
import java.time.{DayOfWeek, LocalDate}
import java.util.Locale

import com.github.yandoroshenko.workhourscounter.calendar.{Calendar, HolidayCalendar}
import com.github.yandoroshenko.workhourscounter.util.{DayHours, FullTime, Holidays, LocalePref}
import play.api.libs.json.JsResultException
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color._
import scalafx.scene.text.Font
import scalafx.util.StringConverter

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 15.05.2017.
  */


object App extends JFXApp with Calendar with HolidayCalendar {

  val countries = Map(Locale.getISOCountries.map(c => new Locale("", c).getDisplayCountry -> new Locale("", c)): _*)

  var holidays: Either[Throwable, Set[Holiday]] = _

  stage = new PrimaryStage {
    maximized = false
    resizable = false
    scene = new Scene {
      title = "Work Hours Counter"
      content = new VBox {
        private val prev = new Button("\u21e0") {
          font = new Font(font.value.getFamily, 19)
          padding = Insets(0, 7, 2, 7)
          margin = Insets(10, 0, 10, 10)
          tooltip = "Previous month"
          onAction = _ =>
            dps.head.value = dps.head.value.value.withDayOfMonth(1).minusMonths(1)
        }
        private val next = new Button("\u21e2") {
          font = new Font(font.value.getFamily, 19)
          padding = Insets(0, 7, 2, 7)
          margin = Insets(10, 10, 10, 0)
          tooltip = "Next month"
          onAction = _ =>
            dps.head.value = dps.head.value.value.withDayOfMonth(1).plusMonths(1)
        }
        private val dps: Seq[DatePicker] = for (i <- 0 to 1) yield
          new DatePicker(LocalDate.now().withDayOfMonth(1).plusMonths(i).minusDays(i)) {
            margin = Insets(10, 0, 10, i * 20)
            tooltip = if (i % 2 == 0)
              "Beginning of the interval"
            else
              "End of the interval"
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
              if (i == 0)
                dps(1).value = value.value.plusMonths(1).withDayOfMonth(1).minusDays(1)
              updateHolidays(dps, considerHolidays, locales)
              updateSum(sum, dps, hours)
            }
          }
        private val holidaysLabel = new Label("Holidays") {
          margin = Insets(14, 0, 10, 74)
          tooltip = "Extract national holidays from working days"
        }
        private val considerHolidays: CheckBox = new CheckBox {
          margin = Insets(14, 10, 10, 10)
          selected = Holidays.get
          tooltip = "Extract national holidays from working days"
          onAction = _ => {
            locales.disable = !selected.value
            updateHolidays(dps, this, locales)
            updateSum(sum, dps, hours)
            Holidays.put(selected.value)
          }
        }

        private val fullTimeSelection = new ToggleGroup()
        private val fullTime = for (i <- 1 to 2) yield
          new RadioButton {
            margin = Insets(10, 20, 20, 20)
            if (i % 2 == 1)
              tooltip = "Use the standard full time work schedule (Mo-Fr 8 hours a day)"
            text = if (i % 2 == 0) "Part time"
            else "Full time"
            toggleGroup = fullTimeSelection
            selected = i % 2 == 0 && FullTime.get || i % 2 != 0 && !FullTime.get
            onAction = _ => {
              hours.foreach(h => h.disable = text.value == "Full time")
              updateSum(sum, dps, hours)
              FullTime.put(i % 2 == 0)
            }
          }
        private val locales = new ComboBox[String](countries.keys.toList.sorted) {
          margin = Insets(10, 10, 10, 200)
          tooltip = "Country to load national holidays for"
          maxWidth = 209
          disable = !considerHolidays.selected.value
          value = LocalePref.get
          onAction = _ => {
            updateHolidays(dps, considerHolidays, this)
            updateSum(sum, dps, hours)
            LocalePref.put(value.value)
          }
        }

        updateHolidays(dps, considerHolidays, locales)

        private val hours: Seq[Spinner[Double]] =
          for (i <- 1 to 7) yield new Spinner[Double](0, 24, DayHours(i).get, 0.5) {
            margin = Insets(10)
            tooltip = "Working hours for the given day of the week"
            maxWidth = 75
            disable = fullTime(0).selected.value
            onMouseClicked = _ => {
              DayHours(i).put(value.value)
              updateSum(sum, dps, hours)
            }
            onKeyReleased = _ => {
              DayHours(i).put(value.value)
              updateSum(sum, dps, hours)
            }
          }
        private val days: Seq[Label] =
          for (i <- 1 to 7) yield new Label(DayOfWeek.of(i).getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()))

        private val sum = new Label {
          margin = Insets(20)
          style = "-fx-font-size: 48pt"
        }

        children = Seq(
          new HBox {
            children = Seq(prev) ++ dps :+ next :+ holidaysLabel :+ considerHolidays
          },
          new HBox {
            children = fullTime :+ locales
          },
          new HBox {
            children = hours.zip(days).map(p => new VBox {
              alignment = Pos.Center
              children = Seq(p._2, p._1)
            })
          },
          new HBox {
            alignment = Pos.Center
            children = Seq(sum)
          }
        )

        updateSum(sum, dps, hours)
      }
    }
  }

  private def updateHolidays(dps: Seq[DatePicker], considerHolidays: CheckBox, locales: ComboBox[String]): Unit =
    holidays = if (considerHolidays.selected.value) {
      log.debug("Updating holidays")
      getHolidays(dps.head.value.value, dps.last.value.value, countries(locales.value.value).getISO3Country)
    }
    else {
      Right(Set())
    }

  private def updateSum(sum: Label, dps: Seq[DatePicker], hours: Seq[Spinner[Double]]): Unit =
    sum.text = holidays match {
      case Right(hs) =>
        val from =dps.head.value.value
        val to = dps.last.value.value
        sum.style = "-fx-font-size: 48pt"
        sum.textFill = Color.web("#303030")
        val s = getDays(
          dps.head.value.value, dps.last.value.value)
          .filterNot(d => hs.map(_.date).contains(d))
          .map(d => {
            val hs = hours(d.getDayOfWeek.getValue - 1) match {
              case h if h.disabled.value =>
                if (d.getDayOfWeek.getValue < 6)
                  8
                else
                  0
              case h =>
                h.value.value
            }
            log.trace(f"""Counting $hs hours on $d""")
            hs
          }
          )
          .sum match {
          case w if w.isWhole =>
            w.toInt.toString
          case f => f.toString
        }
        log.info(f"""Total hours between $from and $to: $s""")
        s
      case Left(e) =>
        sum.style = null
        sum.textFill = Red
        e match {
          case _: JsResultException => "Sorry, selected country is not supported"
          case _: IOException => "Can't load holidays"
          case _ => "Error occured"
        }
    }
}