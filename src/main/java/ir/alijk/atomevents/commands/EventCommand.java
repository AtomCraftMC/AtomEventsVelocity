package ir.alijk.atomevents.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import ir.alijk.atomevents.AtomEvents;
import ir.alijk.atomevents.Common;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class EventCommand implements SimpleCommand {
        private final List<String> ranksOrder = Arrays.asList(
                "rgb",
                "co-owner",
                "core",
                "builder",
                "youtuber3",
                "streamer3",
                "aparater3",
                "atom",
                "mvp+",
                "mvp",
                "youtuber2",
                "streamer2",
                "aparater2",
                "vip+",
                "vip",
                "youtuber",
                "streamer",
                "aparater",
                "default"
        );

        @Override
        public void execute(Invocation invocation) {
                if (!(invocation.source() instanceof Player))
                        return;

                Player player = (Player) invocation.source();

                if (invocation.arguments().length == 0) {
                        Common.send(invocation.source(), "&bBa estefade az &3/event join &bmitoonid vared safe event beshid!");
                        return;
                } else if (invocation.arguments().length == 1) {
                        if (invocation.arguments()[0].equalsIgnoreCase("join")) {
                                if (!AtomEvents.getInstance().isCreated) {
                                        Common.send(invocation.source(), "&cHich eventi dar hale bargozari nist!");
                                } else if (AtomEvents.getInstance().isStarted) {
                                        Common.send(invocation.source(), "&cEvent ghablan shoroo shode va emkan vared shodan digar vojud nadarad!");
                                } else if (AtomEvents.getInstance().eventQueue.containsKey(player.getUniqueId())) {
                                        Common.send(invocation.source(), "&cShoma az ghabl dakhel saf hastid va niazi be dobare join nist!");
                                } else {
                                        try {
                                                 User user = AtomEvents.getInstance().getLuckPerms().getUserManager().getUser(player.getUniqueId());
                                                 Collection<Group> groups = user.getInheritedGroups(user.getQueryOptions());

                                                String playerRank = null;

                                                for (String rank: ranksOrder) {
                                                        if (groups.stream().anyMatch(g -> g.getName().equalsIgnoreCase(rank))) {
                                                                playerRank = rank;
                                                                break;
                                                        }
                                                }

                                                if (playerRank == null) {
                                                        playerRank = "default";
                                                }

                                                AtomEvents.getInstance().eventQueue.put(player.getUniqueId(), playerRank);
                                                Common.send(invocation.source(), "&aBa movafaghiat vared safe event shodid!");
                                                System.out.println(player.getUsername() + " is " + playerRank);


                                        } catch (NullPointerException exception) {
                                                Common.send(invocation.source(), "&cMoshkeli dar join shodan be event vojood dasht, Lotfan dobare emtehan konid!");
                                        }
                                }
                                return;
                        } else if (invocation.arguments()[0].equalsIgnoreCase("rejoin")) {
                                if (player.hasPermission("atomevents.rejoin")) {
                                        if (!AtomEvents.getInstance().isStarted) {
                                                Common.send(player, "&cEvent hanooz shoroo nashode...");
                                                return;
                                        }
                                        if (!AtomEvents.getInstance().isRejoinEnabled) {
                                                Common.send(player, "&cEmkan rejoin dar in event vojood nadare :(");
                                                return;
                                        }
                                        Common.send(player, "&aDarhale ersal shoma be server event :)...");

                                        RegisteredServer eventServer = AtomEvents.getInstance().getProxyServer().getServer(AtomEvents.getInstance().eventServer).get();
                                        player.createConnectionRequest(eventServer).connect();
                                } else {
                                        Common.send(player, "&cShoma rank donor (mesle vip, mvp, ...) nadarid!In command makhsoos hamian server hast.");
                                }
                        }
                }



                if (invocation.source().hasPermission("atomevent.admin")) {
                        switch (invocation.arguments()[0].toLowerCase()) {
                                case "create":
                                        if (invocation.arguments().length != 4) {
                                                Common.send(invocation.source(), "&cRavesh dorost estefade: &4/event create <EventServer> <MaxPlayers> <RejoinEnabled>");
                                                return;
                                        }
                                        if (!AtomEvents.getInstance().isCreated) {
                                                AtomEvents.getInstance().isCreated = true;
                                                AtomEvents.getInstance().eventServer = invocation.arguments()[1];
                                                AtomEvents.getInstance().maxPlayers = Integer.parseInt(invocation.arguments()[2]);
                                                AtomEvents.getInstance().isRejoinEnabled = invocation.arguments()[3].equalsIgnoreCase("true");
                                                Common.send(invocation.source(), "&aEvent ijad shod ...");
                                        } else {
                                                Common.send(invocation.source(), "&cEvent ghablan sakhte shode, emkan dobare sazi nist!");
                                        }
                                        break;
                                case "destroy":
                                        if (AtomEvents.getInstance().isCreated) {
                                                AtomEvents.getInstance().eventServer = null;
                                                AtomEvents.getInstance().maxPlayers = 0;
                                                AtomEvents.getInstance().isCreated = false;
                                                AtomEvents.getInstance().isStarted = false;
                                                AtomEvents.getInstance().eventQueue.clear();
                                                Common.send(invocation.source(), "&aEvent ba movafaghiat az bein raft...");
                                        } else {
                                                Common.send(invocation.source(), "&cHich event i dar in server shoroo nashode!");
                                        }
                                        break;
                                case "start":
                                        if (!AtomEvents.getInstance().isStarted && AtomEvents.getInstance().isCreated) {
                                                AtomEvents.getInstance().isStarted = true;
                                                Common.send(invocation.source(), "&aEvent ba movafaghiat shoroo shod, dar hale ersal player ha...");
                                                sendPlayers();
                                                Common.send(invocation.source(), "&aTamami player ha ba movafaghiat ersal shodand...");
                                        } else {
                                                Common.send(invocation.source(), "&cHich event i dar in server sakhte nashode ya event ghablan start shode!");
                                        }
                                        break;
                                case "send":
                                        if (!AtomEvents.getInstance().isStarted && AtomEvents.getInstance().isCreated) {
                                                if (invocation.arguments().length != 2) {
                                                        Common.send(invocation.source(), "&cRavesh dorost estefade: &4/event send <Amount>");
                                                        return;
                                                }
                                                Integer amount = Integer.parseInt(invocation.arguments()[1]);

                                                Common.send(invocation.source(), "&aDarhale ersal %amount% player be event...".replace("%amount%", amount.toString()));
                                                sendPlayers(amount);
                                                Common.send(invocation.source(), "&aTedad %amount% player be event ersal shodand...".replace("%amount%", amount.toString()));
                                        } else {
                                                Common.send(invocation.source(), "&cHich event i dar in server sakhte nashode ya event ghablan start shode!");
                                        }
                                        break;
                                case "count":
                                        if (AtomEvents.getInstance().isCreated) {
                                                int count = AtomEvents.getInstance().eventQueue.size();
                                                Common.send(invocation.source(), "&aTedad afradi ke dar safe event hastand: &2&l" + count);
                                        } else {
                                                Common.send(invocation.source(), "&cHich event i dar in server sakhte nashode!");
                                        }
                                        break;
                                case "togglerejoin":
                                        if (AtomEvents.getInstance().isRejoinEnabled) {
                                                AtomEvents.getInstance().isRejoinEnabled = false;
                                                Common.send(player, "&cRejoin gheire faal shod.");
                                        } else {
                                                AtomEvents.getInstance().isRejoinEnabled = true;
                                                Common.send(player, "&aRejoin faal shod.");
                                        }
                                        break;
                        }
                }
        }

        public HashMap<String, List<UUID>> sortByRanks() {
                HashMap<UUID, String> queuePlayers = AtomEvents.getInstance().eventQueue;
                HashMap<String, List<UUID>> queuePlayersOrganized = new HashMap<>();

                for (String rank: ranksOrder) {
                        queuePlayersOrganized.put(rank, new LinkedList<>());
                }

                queuePlayers.forEach((uuid, rank) -> {
                        if (queuePlayersOrganized.containsKey(rank)) {
                                queuePlayersOrganized.get(rank).add(uuid);
                        } else {
                                queuePlayersOrganized.get("default").add(uuid);
                        }
                });

                return queuePlayersOrganized;
        }

        public void sendPlayers(int amount) {
                HashMap<String, List<UUID>> sortedPlayers = sortByRanks();
                HashMap<String, List<UUID>> sentPlayers = new HashMap<>();
                for (String rank: ranksOrder) {
                        sentPlayers.put(rank, new LinkedList<>());
                }

                RegisteredServer eventServer = AtomEvents.getInstance().getProxyServer().getServer(AtomEvents.getInstance().eventServer).get();

                AtomicInteger count = new AtomicInteger();

                sortedPlayers.forEach((rank, uuidList) -> {
                        for (UUID uuid: uuidList) {
                                if (count.intValue() < amount) {
                                        Optional<Player> user = AtomEvents.getInstance().getProxyServer().getPlayer(uuid);

                                        if (user.isPresent()) {
                                                Player p = user.get();
                                                p.createConnectionRequest(eventServer).connect();
                                                Common.send(p, "&aDarhale enteghal shoma be server event...");
                                                count.getAndIncrement();
                                                sentPlayers.get(rank).add(uuid);
                                        }
                                } else {
                                        break;
                                }
                        }
                });

                // Cleaning up sent players
                sentPlayers.forEach((rank, uuidList) -> {
                        for (UUID uuid: uuidList) {
                                AtomEvents.getInstance().eventQueue.remove(uuid);
                        }
                });
        }

        public void sendPlayers() {
                sendPlayers(AtomEvents.getInstance().eventQueue.size());
        }

}
