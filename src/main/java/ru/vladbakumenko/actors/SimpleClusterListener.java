package ru.vladbakumenko.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.Member;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import ru.vladbakumenko.model.ChatMessage;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class SimpleClusterListener extends AbstractActor {
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    Cluster cluster = Cluster.get(getContext().getSystem());

    ActorRef messageSender;

    private List<Member> members = new ArrayList<>();

    // subscribe to cluster changes
    @Override
    public void preStart() {
        // #subscribe
        cluster.subscribe(
                getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, UnreachableMember.class);
        // #subscribe
        messageSender = context().actorOf(Props.create(MessageSender.class));
    }

    // re-subscribe when restart
    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        MemberUp.class,
                        mUp -> {
                            members.add(mUp.member());
                            log.info("Member is Up: {}", mUp.member());
                        })
                .match(
                        UnreachableMember.class,
                        mUnreachable -> {
                            log.info("Member detected as unreachable: {}", mUnreachable.member());
                        })
                .match(
                        MemberRemoved.class,
                        mRemoved -> {
                            log.info("Member is Removed: {}", mRemoved.member());
                        })
                .match(
                        MemberEvent.class,
                        message -> {
                            // ignore
//                            System.out.println(message.member().upNumber());
//                            log.info("EVENT IS HAPPENED!");
                        })
                .match(
                        ChatMessage.class,
                        message -> {
                            if (members.isEmpty()) {
                                getContext().system().scheduler().scheduleOnce(Duration.of(1, ChronoUnit.SECONDS),
                                        getSelf(), message, getContext().getDispatcher(), getSelf());
                            }
                            for (Member member : members) {
                                context().actorSelection(member.address() + "/user/listener")
                                        .tell(new ChatMessage(message.getValue() + "from: "
                                                + cluster.selfUniqueAddress().toString()), getSelf());
                            }
                        }
                )
                .build();
    }
}
