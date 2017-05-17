package com.github.yandoroshenko

import java.time.LocalDate

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 16.05.2017.
  */
trait WorkHourCounter {
  def count(days: Seq[LocalDate]): Int = days.length
}
