package pw.byakuren.discord.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import pw.byakuren.discord.DatabaseManager;
import pw.byakuren.discord.objects.cache.Cache;
import pw.byakuren.discord.objects.cache.ServerCache;
import pw.byakuren.discord.objects.cache.datatypes.WatchedUser;

import java.util.List;

public class WatchUser implements Command {

    private Cache c;

    public WatchUser(Cache c) {
        this.c = c;
    }

    @Override
    public String getName() {
        return "watch";
    }

    @Override
    public String getSyntax() {
        return null;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public boolean needsBotOwner() {
        return false;
    }

    @Override
    public void run(Message message, List<String> args) {
        ServerCache sc = c.getServerCache(message.getGuild());
        if (args.size() < 1) return;
        switch (args.get(0)) {
            case "add":
                for (Member m: message.getMentionedMembers()) {
                    if (!sc.userIsWatched(m))
                        sc.getWatchedUsers().getData().add(new WatchedUser(m));
                }
                message.addReaction("\uD83D\uDC4D").queue();
                break;
            case "remove":
                for (Member m: message.getMentionedMembers()) {
                    if (sc.userIsWatched(m)) {
                        for (int i = 0; i < sc.getWatchedUsers().getData().size(); i++) {
                            if (sc.getWatchedUsers().getData().get(i).getUser().getUser().getIdLong()==
                                    m.getUser().getIdLong()) {
                                sc.getWatchedUsers().getData().remove(i);
                                i--;
                            }
                        }
                    }
                }
                message.addReaction("\uD83D\uDC4D").queue();
                break;
            case "list":
                List<WatchedUser> list = sc.getAllValidWatchedUsers();
                StringBuilder s = new StringBuilder();
                s.append("Watched users:\n");
                for (WatchedUser wu : list) {
                    s.append(wu.getUser().getAsMention()).append(" ");
                }
                message.getChannel().sendMessage(s.toString()).queue();
                break;
            default:
                message.getChannel().sendMessage("Available arguments: `add [mention users]`, `remove [mention users]`, `list`").queue();
        }
    }
}
