package akv5.acore.libs;

import akv5.acore.ACore;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

public class PluginHider {

    public static void initialize() {
        if (ACore.getInstance().getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            return;
        }

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                ACore.getInstance(),
                ListenerPriority.HIGHEST,
                PacketType.Play.Server.CHAT
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() != PacketType.Play.Server.CHAT) return;

                PacketContainer packet = event.getPacket();
                WrappedChatComponent component = packet.getChatComponents().readSafely(0);
                if (component == null) return;

                String json = component.getJson();
                if (json == null) return;

                if (json.contains("CMI")) {
                    if (json.contains("-----")) {
                        event.setCancelled(true);
                        return;
                    }
                }

                if (json.contains("is now available") && json.contains("Your version")) {
                    event.setCancelled(true);
                    return;
                }

                if (json.contains("----------------------------------------")) {
                    event.setCancelled(true);
                    return;
                }
            }
        });
    }
}