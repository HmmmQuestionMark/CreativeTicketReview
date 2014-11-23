package me.hqm.plugindev.creativeticketreview.command;

import com.iciql.Db;
import me.hqm.plugindev.creativeticketreview.CTRSetting;
import me.hqm.plugindev.creativeticketreview.command.type.BaseCommand;
import me.hqm.plugindev.creativeticketreview.command.type.CommandResult;
import me.hqm.plugindev.creativeticketreview.model.PlayerModel;
import me.hqm.plugindev.creativeticketreview.model.TicketModel;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class ReviewCommand extends BaseCommand {
    @Override
    public CommandResult onCommand(CommandSender sender, Command command, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            return CommandResult.PLAYER_ONLY;
        } else {
            Player player = (Player) sender;

            // -- NO ARGS -- //
            if(args.length == 0) {
                ((Player) sender).performCommand("review list");
                return CommandResult.SUCCESS;
            }

            if (args.length == 1) {
                // -- SUBMIT -- //
                if("submit".equalsIgnoreCase(args[0])) {
                    if(TicketModel.existsFor(player)) {
                        if(TicketModel.State.ACCEPTED.equals(TicketModel.getFor(player.getUniqueId()).state)) {
                            player.sendMessage(ChatColor.YELLOW + "Your ticket has already been accepted.");
                        } else {
                            player.sendMessage(ChatColor.YELLOW + "You already have a ticket pending submission.");
                        }
                    } else {
                        TicketModel.of(player);
                        player.sendMessage(ChatColor.YELLOW + "Your ticket is now in our queue, we will review it shortly.");
                        Bukkit.broadcast(ChatColor.YELLOW + "There is a new creative world ticket up for review.", "ctr.review");
                    }
                }
                // -- LIST -- //
                else if("list".equalsIgnoreCase(args[0])) {
                    // Alias and DB
                    TicketModel ticketAlias = new TicketModel();
                    Db db = Db.open(CTRSetting.DATABASE_URL.getValue());

                    // Start the list
                    player.sendMessage(new String[]{ChatColor.DARK_AQUA + "TICKETS:", "-------------------------"});
                    for (TicketModel ticket : db.from(ticketAlias).where(ticketAlias.state).
                            is(TicketModel.State.OPEN).orderBy(ticketAlias.openTime).select()) {
                        FancyMessage message = new FancyMessage("- ").
                                then(ticket.ticketName).
                                color(ChatColor.YELLOW).
                                command("/review tp " + ticket.ticketName).
                                tooltip(ticket.openTime.toString());
                        message.send(player);
                    }

                    // Close the DB
                    db.close();
                } else {
                    // Alias and DB
                    PlayerModel alias = new PlayerModel();
                    TicketModel ticketAlias = new TicketModel();
                    Db db = Db.open(CTRSetting.DATABASE_URL.getValue());
                    List<PlayerModel> found = db.from(alias).where(alias.lastKnownName).is(args[0]).select();

                    try {
                        if (!found.isEmpty()) {
                            PlayerModel about = found.get(0);

                            // Start the data
                            player.sendMessage(new String[]{ChatColor.DARK_AQUA + about.lastKnownName + ChatColor.WHITE + ":", "--------"});
                            player.sendMessage("- " + ChatColor.YELLOW + "Points: " + ChatColor.WHITE + about.points);
                            List<TicketModel> tickets = db.from(ticketAlias).where(ticketAlias.creatorId).is(about.minecraftId).select();
                            String status = "NO STATUS";
                            if (!tickets.isEmpty()) {
                                status = tickets.get(0).state.name();
                            }
                            player.sendMessage("- " + ChatColor.YELLOW + "Ticket Status: " + ChatColor.WHITE + status);
                        } else {
                            return CommandResult.INVALID_SYNTAX;
                        }
                    } finally {
                        db.close();
                    }
                }
            } else if (args.length == 2) {
                // -- TP -- //
                if ("tp".equalsIgnoreCase(args[0])) {
                    TicketModel ticket = TicketModel.getFor(args[1]);
                    if (ticket != null) {
                        player.teleport(ticket.getLocation());
                    }
                }
                // -- ACCEPT / DENY -- //
                else if("accept".equalsIgnoreCase(args[0]) || "deny".equalsIgnoreCase(args[0])) {
                    TicketModel.State newState;
                    if("deny".equalsIgnoreCase(args[0])) {
                        newState = TicketModel.State.DENIED;
                    } else {
                        newState = TicketModel.State.ACCEPTED;
                    }

                    TicketModel ticket = TicketModel.getFor(args[1]);
                    if (ticket != null) {
                        ticket.setCloseTime(new Timestamp(System.currentTimeMillis()));
                        ticket.setModeratorId(player.getUniqueId().toString());
                        ticket.setState(newState);
                        ticket.update();

                        PlayerModel moderator = PlayerModel.of(player);
                        moderator.points++;
                        moderator.update();

                        sender.sendMessage(ChatColor.YELLOW + "You have closed this ticket.");


                        OfflinePlayer creator = Bukkit.getOfflinePlayer(UUID.fromString(ticket.creatorId));
                        if (creator.isOnline()) {
                            if(TicketModel.existsFor(creator.getPlayer())) {
                                TicketModel model = TicketModel.getFor(UUID.fromString(ticket.creatorId));
                                // Delete denied tickets
                                if(TicketModel.State.DENIED.equals(model.state)) {
                                    creator.getPlayer().sendMessage(ChatColor.RED + "Your creative world ticket has been denied.");
                                    model.delete();
                                } else {
                                    creator.getPlayer().sendMessage(ChatColor.YELLOW + "Your creative world ticket has been accepted!");
                                }
                            }
                        }
                    }
                }
                else {
                    return CommandResult.INVALID_SYNTAX;
                }
            } else {
                return CommandResult.INVALID_SYNTAX;
            }
        }
        return CommandResult.SUCCESS;
    }
}
