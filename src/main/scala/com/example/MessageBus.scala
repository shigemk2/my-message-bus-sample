package com.example

import akka.actor._

object MessageBusDriver extends CompletableApp(9) {
}

case class CommandHandler(applicationId: String, handler: ActorRef)
case class ExecuteBuyOrder(portfolioId: String, symbol: String, quantitly: Int, price: Double)
case class BuyOrderExecuted(portfolioId: String, symbol: String, quantitly: Int, price: Double)
case class ExecuteSellOrder(portfolioId: String, symbol: String, quantitly: Int, price: Double)
case class NotificationInterest(applicationId: String, interested: ActorRef)
case class RegisterCommandHandler(applicationId: String, commandId: String, interested: ActorRef)
case class RegisterNotificationInterest(applicationId: String, notificationId: String, interested: ActorRef)
case class TradingCommand(commandId: String, command: Any)
case class TradingNotification(notificationId: String, notification: Any)

case class Status()
