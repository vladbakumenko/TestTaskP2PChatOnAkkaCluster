package ru.vladbakumenko.actors;

import akka.actor.AbstractActor;
import akka.actor.Address;
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
import scala.collection.JavaConverters;

import java.util.Set;

public class ClusterListener extends AbstractActor {
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    Cluster cluster = Cluster.get(getContext().getSystem());

    // subscribe to cluster changes
    @Override
    public void preStart() {
        // #subscribe
        cluster.subscribe(
                getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, UnreachableMember.class);
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
                        })
                .match(
                        ChatMessage.class,
                        message -> {
                            Set<Member> members = JavaConverters.setAsJavaSet(cluster.readView().members());

                            for (Member member : members) {
                                context().actorSelection(member.address() + "/user/listener")
                                        .tell(new ChatMessage(message.getValue() + " from: "
                                                + cluster.selfUniqueAddress().toString()), getSelf());
                            }
                        }
                )
                .match(Address.class,
                        address -> {
                            cluster.join(address);
                        })
                .build();
    }
}
