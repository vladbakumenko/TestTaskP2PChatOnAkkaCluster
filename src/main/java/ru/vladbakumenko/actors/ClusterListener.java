package ru.vladbakumenko.actors;

import akka.actor.AbstractActor;
import akka.actor.Address;
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
import ru.vladbakumenko.dto.ChatMembers;
import ru.vladbakumenko.dto.Connection;
import ru.vladbakumenko.dto.GroupMessage;
import ru.vladbakumenko.dto.PrivateMessage;
import scala.collection.JavaConverters;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClusterListener extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private Cluster cluster;
    private ChatMembers chatMembers = new ChatMembers();
    private Set<Member> currentMembers = new HashSet<>();
    private Map<String, Address> users = new HashMap<>();

    public ClusterListener(Cluster cluster) {
        this.cluster = cluster;
    }

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
                            Set<Member> members = JavaConverters.setAsJavaSet(cluster.readView().members());

                            currentMembers.addAll(members);

                            for (Member member : currentMembers) {
                                context()
                                        .actorSelection(member.address() + "/user/manager")
                                        .tell(chatMembers, getSelf());
                            }
                            for (Member member : currentMembers) {
                                context()
                                        .actorSelection(member.address() + "/user/listener")
                                        .tell(chatMembers, getSelf());
                            }

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
                        PrivateMessage.class,
                        message -> {
                            var sender = getAddress(message.getSenderName());
                            var recipient = getAddress(message.getRecipientName());

                            System.out.println("message sender " + message.getSenderName());
                            System.out.println("message recipient " + message.getRecipientName());


                            if (sender.equals(recipient)) {
                                context().actorSelection(sender + "/user/manager")
                                        .tell(message, getSelf());
                            } else {
                                context().actorSelection(sender + "/user/manager")
                                        .tell(message, getSelf());
                                context().actorSelection(recipient + "/user/manager")
                                        .tell(message, getSelf());
                            }
                        })
                .match(
                        GroupMessage.class,
                        message -> {
                            Set<Member> members = JavaConverters.setAsJavaSet(cluster.readView().members());
                            for (Member member : members) {
                                context()
                                        .actorSelection(member.address() + "/user/manager")
                                        .tell(message, getSelf());
                            }
                        })
                .match(
                        Connection.class,
                        connection -> {
                            cluster.join(connection.getConnectionAddress());
                            chatMembers.getMembers().add(connection);
                        })
                .match(ChatMembers.class,
                        message -> {
                            chatMembers.getMembers().addAll(message.getMembers());
                        })
                .build();
    }

    public static Props getProps(Cluster cluster) {
        return Props.create(ClusterListener.class, cluster);
    }

    private Address getAddress(String name) {
        return chatMembers.getMembers().stream()
                .filter(connection -> connection.getName().equals(name))
                .findFirst().orElseThrow().getUserAddress();
    }
}
