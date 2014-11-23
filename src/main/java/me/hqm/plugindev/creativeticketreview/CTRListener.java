package me.hqm.plugindev.creativeticketreview;

import me.hqm.plugindev.creativeticketreview.model.PlayerModel;
import me.hqm.plugindev.creativeticketreview.model.TicketModel;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class CTRListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerModel.of(player);

        // Delete denied tickets
        if(TicketModel.existsFor(player)) {
            TicketModel model = TicketModel.getFor(player.getUniqueId());
            if(TicketModel.State.DENIED.equals(model.state)) {
                player.sendMessage(ChatColor.RED + "Your creative world ticket has been denied.");
                model.delete();
            }
        }
    }
}
