package com.github.mboogerd.labbowling

import scalaz.ValidationNel
import scalaz.syntax.semigroup._
import scalaz.std.option._
import scalaz.std.anyVal._
import scalaz.syntax.std.boolean._
import scalaz.syntax.validation._

object LabbowlingApp extends App {

  type Valid[T] = ValidationNel[String, T]


  case class Game(scoreCard: ScoreCard, finished: Boolean) {
    def addRoll(score: Int): Valid[Game] = scoreCard.headOption match {
      case Some(f@Frame09(_, _, _)) if f.finished ⇒ addFrame(this, score)
      case Some(f@Frame09(_, _, _)) ⇒ updateFrame(this, score)
      case Some(f@Frame10(_, _, _, _)) if f.finished ⇒ "You little cheater!".failureNel
      case Some(f@Frame10(_, _, _, _)) ⇒ updateFrame(this, score)
      case None ⇒ addFrame(this, score)
    }
  }

  sealed trait Frame {
    def total: Option[Int]
    def finished: Boolean
  }
  case class Frame09(first: Int, second: Option[Int], bonus: Bonus) extends Frame {
    override def total: Option[Int] = bonus.canCalculateTotal ? second.map(_ + first) | None
    override def finished: Boolean = second.isDefined || first == 10
  }
  case class Frame10(first: Int, second: Option[Int], third: Option[Int], bonus: Bonus) extends Frame {
    override def total: Option[Int] = bonus.canCalculateTotal ? (third |+| second).map(_ + first) | None
    override def finished: Boolean = third.isDefined || (second.isDefined && first < 10)
  }

  type ScoreCard = List[Frame]


  sealed trait Bonus {
    def canCalculateTotal: Boolean
  }
  case object NoBonus extends Bonus {
    override def canCalculateTotal: Boolean = true
  }
  case object SpareUndefined extends Bonus {
    override def canCalculateTotal: Boolean = false
  }
  case class SpareDefined(point: Int) extends Bonus {
    override def canCalculateTotal: Boolean = true
  }
  case object StrikeUndefined extends Bonus {
    override def canCalculateTotal: Boolean = false
  }
  case class StrikeFirstDefined(point: Int) extends Bonus {
    override def canCalculateTotal: Boolean = false
  }
  case class StrikeDefined(points1: Int, points2: Int) extends Bonus {
    override def canCalculateTotal: Boolean = true
  }


  def isValidScore(score: Int): Boolean = score <= 10 && score >= 0


  // TODO: Capture case of last roll
  def addFrame(game: Game, score: Int): Valid[Game] = {
    if (isValidScore(score))
      if (score == 10) game.copy(scoreCard = Frame09(score, None, StrikeUndefined) :: game.scoreCard).successNel
      else game.copy(scoreCard = Frame09(score, None, NoBonus) :: game.scoreCard).successNel
    else
      s"Illegal score: $score".failureNel[Game]
  }

  def updateFrame(game: Game, score: Int): Valid[Game] = Game(game.scoreCard.head match {
    case f@Frame09(first, None, bonus) ⇒ f.copy(second = Some(score), determineBonus(first, second))
    case Frame10(first, None, _, bonus) ⇒
    case Frame10(first, Some(_), None, bonus) ⇒
  } :: game.scoreCard.tail)

  def determineBonus(first: Int, second: Int): Bonus = ???
}