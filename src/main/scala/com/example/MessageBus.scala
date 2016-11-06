package com.example

import akka.actor.Actor.Receive
import akka.actor._

object MessageBusDriver extends CompletableApp(9) {
}

case class CommandHandler(applicationId: String, handler: ActorRef)
case class ExecuteBuyOrder(portfolioId: String, symbol: String, quantitly: Int, price: Double)
case class BuyOrderExecuted(portfolioId: String, symbol: String, quantitly: Int, price: Double)
case class ExecuteSellOrder(portfolioId: String, symbol: String, quantitly: Int, price: Double)
case class SellOrderExecuted(portofolioId: String, symbol: String, quantity: Int, price: Double)
case class NotificationInterest(applicationId: String, interested: ActorRef)
case class RegisterCommandHandler(applicationId: String, commandId: String, handler: ActorRef)
case class RegisterNotificationInterest(applicationId: String, notificationId: String, interested: ActorRef)
case class TradingCommand(commandId: String, command: Any)
case class TradingNotification(notificationId: String, notification: Any)

case class Status()

class TradingBus(canStartAfterRegistered: Int) extends Actor {
  val commandHandlers = scala.collection.mutable.Map[String, Vector[CommandHandler]]()
  val notificationInterests = scala.collection.mutable.Map[String, Vector[NotificationInterest]]()

  var totalRegistered = 0

  def dispatchCommand(command: TradingCommand) = {
    if (commandHandlers.contains(command.commandId)) {
      commandHandlers(command.commandId) map { commandHandler =>
        commandHandler.handler ! command.command
      }
    }
  }

  def dispatchNotification(notification: TradingNotification) = {
    if (notificationInterests.contains(notification.notificationId)) {
      notificationInterests(notification.notificationId) map { notificationInterest =>
        notificationInterest.interested ! notification.notification
      }
    }
  }

  def notifyStartWhenReady() = {
    totalRegistered += 1

    if (totalRegistered == this.canStartAfterRegistered) {
      println(s"TradingBus: is ready with registered actors: $totalRegistered")
      MessageBusDriver.completedStep()
    }
  }

  override def receive: Receive = {
    case register: RegisterCommandHandler =>
      println(s"TradingBus: registering: $register")
      registerCommandHandler(register.commandId, register.applicationId, register.handler)
      notifyStartWhenReady()
    case register: RegisterNotificationInterest =>
      println(s"TradingBus: registering: $register")
      registerNotificationInterest(register.notificationId, register.applicationId, register.interested)
      notifyStartWhenReady()
    case command: TradingCommand =>
      println(s"TradingBus: dispatching command: $command")
      dispatchCommand(command)
    case notification: TradingNotification =>
      println(s"TradingBus: dispatching notification: $notification")
      dispatchNotification(notification)
    case status: Status =>
      println(s"TradingBus: STATUS: has commandHandlers: $commandHandlers")
      println(s"TradingBus: STATUS: has notificationInterests: $notificationInterests")
    case message: Any =>
      println(s"TradingBus: received unexpected: $message")
  }

  def registerCommandHandler(commandId: String, applicationId: String, handler: ActorRef) = {
    if (!commandHandlers.contains(commandId)) {
      commandHandlers(commandId) = Vector[CommandHandler]()
    }

    commandHandlers(commandId) =
      commandHandlers(commandId) :+ CommandHandler(applicationId, handler)
  }

  def registerNotificationInterest(notificationId: String, applicationId: String, interested: ActorRef) = {
    if (!notificationInterests.contains(notificationId)) {
      notificationInterests(notificationId) = Vector[NotificationInterest]()
    }

    notificationInterests(notificationId) =
      notificationInterests(notificationId) :+ NotificationInterest(applicationId, interested)
  }
}

class MarketAnalysisTools(tradingBus: ActorRef) extends Actor {
  val applicationId = self.path.name

  tradingBus ! RegisterNotificationInterest(applicationId, "BuyOrderExecuted", self)
  tradingBus ! RegisterNotificationInterest(applicationId, "SellOrderExecuted", self)

  override def receive: Receive = {
    case executed: BuyOrderExecuted =>
      println(s"MarketAnalysisTools: adding analysis for: $executed")
      MessageBusDriver.completedStep()
    case executed: SellOrderExecuted =>
      println(s"MarketAnalysisTools: adding analysis for: $executed")
      MessageBusDriver.completedStep()
    case message: Any =>
      println(s"MarketAnalysisTools: received unexpected: $message")
  }
}