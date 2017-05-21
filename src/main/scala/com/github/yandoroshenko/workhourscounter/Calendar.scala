package com.github.yandoroshenko.workhourscounter

import java.time.DayOfWeek._
import java.time.LocalDate

import scala.annotation.tailrec

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 15.05.2017.
  */
trait Calendar {

  @tailrec
  private def getDays(i: LocalDate, to: LocalDate, acc: List[LocalDate]): List[LocalDate] =
    if (i.isAfter(to))
      acc
    else
      getDays(i.plusDays(1), to, acc :+ i)


  def getDays(from: LocalDate, to: LocalDate): List[LocalDate] = getDays(from, to, List())

  @tailrec
  private def prependTillMonday(acc: List[LocalDate]): List[LocalDate] =
    if (acc.head.getDayOfWeek == MONDAY)
      acc
    else
      prependTillMonday(acc.head.minusDays(1) :: acc)

  @tailrec
  private def appendTillSunday(acc: List[LocalDate]): List[LocalDate] =
    if (acc.last.getDayOfWeek == SUNDAY)
      acc
    else
      appendTillSunday(acc :+ acc.last.plusDays(1))

  def getDaysAlignedByWeek(from: LocalDate, to: LocalDate): List[LocalDate] =
    appendTillSunday(prependTillMonday(getDays(from, to)))
}