package ir.alijk.atomevents;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import ir.alijk.atomevents.commands.EventCommand;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

public final class AtomEvents {
        private static AtomEvents instance;
        public String PREFIX = "&3[&bAtomEvents&3] ";
        public HashMap<UUID, String> eventQueue = new HashMap<>();
        public String eventServer = null;
        public int maxPlayers = 0;
        public boolean isCreated = false;
        public boolean isStarted = false;
        public boolean isRejoinEnabled = false;
        private LuckPerms luckPerms;



        private final ProxyServer proxyServer;
        private final Logger logger;

        @Inject
        public AtomEvents(ProxyServer proxyServer, Logger logger) {
                this.proxyServer = proxyServer;
                this.logger = logger;
        }

        @Subscribe
        public void onInit(ProxyInitializeEvent event) {
                instance = this;


                luckPerms = LuckPermsProvider.get();

                getProxyServer().getCommandManager().register("event", new EventCommand());

        }

        public ProxyServer getProxyServer() {
                return proxyServer;
        }


        public static AtomEvents getInstance() {
                return instance;
        }

        public LuckPerms getLuckPerms() {
                return luckPerms;
        }
}
