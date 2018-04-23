package com.github.yandoroshenko.workhourscounter.util

import com.typesafe.scalalogging.Logger

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 23.04.2018.
  */
trait Logger {
  protected lazy val log = Logger(getClass())
}
