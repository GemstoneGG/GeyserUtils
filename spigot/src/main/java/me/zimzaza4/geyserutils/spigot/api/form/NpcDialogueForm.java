package me.zimzaza4.geyserutils.spigot.api.form;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.zimzaza4.geyserutils.common.channel.GeyserUtilsChannels;
import me.zimzaza4.geyserutils.common.form.element.NpcDialogueButton;
import me.zimzaza4.geyserutils.common.packet.NpcDialogueFormDataCustomPayloadPacket;
import me.zimzaza4.geyserutils.spigot.GeyserUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Accessors(fluent = true)
public class NpcDialogueForm {

    public static Map<String, NpcDialogueForm> FORMS = new HashMap<>();

    String title;
    String dialogue;
    String skinData;
    Entity bindEntity;
    boolean hasNextForm = false;
    List<NpcDialogueButton> buttons;
    BiConsumer<String, Integer> handler;
    Consumer<String> closeHandler;

    public static void closeForm(FloodgatePlayer floodgatePlayer) {
        NpcDialogueFormDataCustomPayloadPacket data = new NpcDialogueFormDataCustomPayloadPacket(null, null, null, null, -1, null, "CLOSE", false);
        Player p = Bukkit.getPlayer(floodgatePlayer.getCorrectUniqueId());
        if (p != null) {
            p.sendPluginMessage(GeyserUtils.getInstance(), GeyserUtilsChannels.MAIN, GeyserUtils.getPacketManager().encodePacket(data));
        }
    }

    public void send(FloodgatePlayer floodgatePlayer) {
        UUID formId = UUID.randomUUID();
        NpcDialogueFormDataCustomPayloadPacket data = new NpcDialogueFormDataCustomPayloadPacket(formId.toString(), title, dialogue, skinData, bindEntity.getEntityId(), buttons, "OPEN", hasNextForm);
        Player p = Bukkit.getPlayer(floodgatePlayer.getCorrectUniqueId());
        if (p != null) {

            FORMS.put(formId.toString(), this);

            p.sendPluginMessage(GeyserUtils.getInstance(), GeyserUtilsChannels.MAIN, GeyserUtils.getPacketManager().encodePacket(data));

            Bukkit.getAsyncScheduler().runAtFixedRate(GeyserUtils.getInstance(), (ScheduledTask task) -> {
                if (!FORMS.containsKey(formId.toString())) {
                    task.cancel();
                    return;
                }
                if (!p.isOnline()) {
                    FORMS.remove(formId.toString());
                }
            }, 500L, 500L, TimeUnit.MILLISECONDS);
        }
    }
}
