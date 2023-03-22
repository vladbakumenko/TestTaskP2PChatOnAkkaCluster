package ru.vladbakumenko.model;

import akka.actor.Actor;
import akka.actor.ActorContext;
import akka.actor.ActorLogging;
import akka.actor.ActorRef;
import akka.event.LoggingAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

@Data
@AllArgsConstructor
public class Message {
    private String value;


}
