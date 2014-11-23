package me.hqm.plugindev.creativeticketreview.command;

import com.iciql.Db;
import me.hqm.plugindev.creativeticketreview.CTRPlugin;
import me.hqm.plugindev.creativeticketreview.CTRSetting;
import me.hqm.plugindev.creativeticketreview.command.type.BaseCommand;
import me.hqm.plugindev.creativeticketreview.command.type.CommandResult;
import me.hqm.plugindev.creativeticketreview.model.PlayerModel;
import me.hqm.plugindev.creativeticketreview.model.TicketModel;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CTRCommand extends BaseCommand {
    // -- CACHE -- //
    private Map<String, Purge> purgeCache = new HashMap<>();
    private Map<String, UUID> purgeTarget = new HashMap<>();

    // -- ENUM -- //

    enum Purge {
        ALL, POINTS, TICKETS
    }

    // -- COMMAND -- //

    @Override
    public CommandResult onCommand(CommandSender sender, Command command, String[] args) {
        // -- NO-ARGS or INFO -- //
        if(args.length == 0 || "info".equalsIgnoreCase(args[0])) {
            sender.sendMessage(ChatColor.YELLOW + "You are running " + CTRPlugin.getInst().getDescription().getName() + " v" +
                    CTRPlugin.getInst().getDescription().getVersion() + ".");
            sender.sendMessage(ChatColor.YELLOW + "Authored by: " + StringUtils.join(CTRPlugin.getInst().getDescription().getAuthors(), ','));
        }
        // -- PURGE -- //
        else {
            if ("purge".equalsIgnoreCase(args[0])) {
                // -- CONFIRM -- //
                if (args.length == 2 && "confirm".equalsIgnoreCase(args[1])) {
                    if (purgeCache.containsKey(sender.getName())) {
                        Db db = Db.open(CTRSetting.DATABASE_URL.getValue());
                        switch (purgeCache.get(sender.getName())) {
                            case ALL:
                                db.from(new PlayerModel()).delete();
                                db.from(new TicketModel()).delete();
                                for (Player online : Bukkit.getOnlinePlayers()) {
                                    online.kickPlayer(ChatColor.YELLOW + "Sorry about that, purging data.");
                                }
                                purgeCache.remove(sender.getName());
                                break;
                            case POINTS:
                                if (!purgeTarget.containsKey(sender.getName())) {
                                    sender.sendMessage(ChatColor.RED + "Something went wrong, cannot find target.");
                                    purgeCache.remove(sender.getName());
                                    db.close();
                                    return CommandResult.QUIET_ERROR;
                                }

                                PlayerModel player = PlayerModel.getFor(purgeTarget.get(sender.getName()));
                                player.setPoints(0);
                                player.update();

                                purgeCache.remove(sender.getName());
                                purgeTarget.remove(sender.getName());
                                break;
                            case TICKETS:
                                if (!purgeTarget.containsKey(sender.getName())) {
                                    sender.sendMessage(ChatColor.RED + "Something went wrong, cannot find target.");
                                    purgeCache.remove(sender.getName());
                                    db.close();
                                    return CommandResult.QUIET_ERROR;
                                }

                                UUID target = purgeTarget.get(sender.getName());
                                TicketModel alias = new TicketModel();
                                db.from(alias).where(alias.creatorId).is(target.toString()).delete();

                                purgeCache.remove(sender.getName());
                                purgeTarget.remove(sender.getName());
                                break;
                        }
                        db.close();
                        sender.sendMessage(ChatColor.YELLOW + "Purge complete.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You have not selected a purge option.");
                        return CommandResult.QUIET_ERROR;
                    }
                } else if (args.length == 2 && "all".equalsIgnoreCase(args[1])) {
                    try {
                        purgeCache.put(sender.getName(), Purge.ALL);
                        sender.sendMessage(ChatColor.YELLOW + "To confirm, use " + ChatColor.ITALIC + "/ctr purge confirm");
                    } catch (Exception oops) {
                        oops.printStackTrace();
                        return CommandResult.ERROR;
                    }
                } else if (args.length == 3) {
                    // Get the target
                    Db db = Db.open(CTRSetting.DATABASE_URL.getValue());
                    PlayerModel alias = new PlayerModel();
                    List<PlayerModel> targets = db.from(alias).where(alias.lastKnownName).is(args[2]).select();
                    db.close();

                    if(targets.isEmpty()) {
                        sender.sendMessage(ChatColor.RED + "Something went wrong, cannot find target.");
                        return CommandResult.QUIET_ERROR;
                    }

                    // Purge option
                    Purge option = null;

                    // -- ALL -- //
                    if("all".equalsIgnoreCase(args[1])) {
                        option = Purge.ALL;
                    }

                    // -- POINTS -- //
                    else if("point".equalsIgnoreCase(args[1]) || "points".equalsIgnoreCase(args[1])) {
                        option = Purge.POINTS;
                    }

                    // -- TICKETS -- //
                    else if("ticket".equalsIgnoreCase(args[1]) || "tickets".equalsIgnoreCase(args[1])) {
                        option = Purge.TICKETS;
                    }

                    if(option != null) {
                        try {
                            purgeCache.put(sender.getName(), option);
                            purgeTarget.put(sender.getName(), UUID.fromString(targets.get(0).getMinecraftId()));
                            sender.sendMessage(ChatColor.YELLOW + "To confirm, use " + ChatColor.ITALIC + "/ctr purge confirm");
                        } catch (Exception oops) {
                            oops.printStackTrace();
                            return CommandResult.ERROR;
                        }
                    } else {
                        return CommandResult.INVALID_SYNTAX;
                    }
                }
            }
        }
        return CommandResult.SUCCESS;
    }
}
