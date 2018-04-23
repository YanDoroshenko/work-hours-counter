package com.github.yandoroshenko.workhourscounter.util

import java.util.prefs.Preferences

import com.github.yandoroshenko.workhourscounter.App

/**
  * Created by Yan Doroshenko (yandoroshenko@protonmail.com) on 19.01.2018.
  */
object PersistenceProvider {

  final val preferenceNode = Preferences.userRoot().node(App.getClass.getName)
}

sealed trait Preference[A] {
  val name: String

  def get: A

  def put(value: A)
}

case object LocalePref extends Preference[String] {
  override val name: String = "locale"

  override def get: String = PersistenceProvider.preferenceNode.get(name, java.util.Locale.getDefault().getDisplayCountry())

  override def put(value: String): Unit = PersistenceProvider.preferenceNode.put(name, value)
}

case class DayHours(number: Int) extends Preference[Double] {
  if (number < 1 || number > 7)
    throw new IllegalArgumentException("Day of the week number should be between 1 and 7.")

  override val name: String = "dayOfTheWeek" + number

  override def get: Double = PersistenceProvider.preferenceNode.getDouble(name, 0)

  override def put(value: Double): Unit = PersistenceProvider.preferenceNode.putDouble(name, value)
}

case object FullTime extends Preference[Boolean] {
  override val name: String = "fullTime"

  override def get: Boolean = PersistenceProvider.preferenceNode.getBoolean(name, true)

  override def put(fullTime: Boolean): Unit = PersistenceProvider.preferenceNode.putBoolean(name, fullTime)
}

case object Holidays extends Preference[Boolean] {
  override val name: String = "holidays"

  override def get: Boolean = PersistenceProvider.preferenceNode.getBoolean(name, true)

  override def put(holidays: Boolean): Unit = PersistenceProvider.preferenceNode.putBoolean(name, holidays)
}