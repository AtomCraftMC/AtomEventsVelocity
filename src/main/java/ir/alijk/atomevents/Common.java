package ir.alijk.atomevents;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Common {
        public static TextComponent colorize(String text) {
                return LegacyComponentSerializer.legacyAmpersand().deserialize(AtomEvents.getInstance().PREFIX + text);
        }

        public static void send(CommandSource source, String text) {
                source.sendMessage(colorize(text));
        }
}
