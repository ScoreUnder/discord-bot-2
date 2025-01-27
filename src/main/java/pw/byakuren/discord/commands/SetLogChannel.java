package pw.byakuren.discord.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import pw.byakuren.discord.commands.permissions.CommandPermission;
import pw.byakuren.discord.commands.subcommands.Subcommand;
import pw.byakuren.discord.commands.subcommands.SubcommandList;
import pw.byakuren.discord.objects.cache.Cache;
import pw.byakuren.discord.objects.cache.ServerCache;
import pw.byakuren.discord.objects.cache.datatypes.ServerParameter;
import pw.byakuren.discord.objects.cache.datatypes.ServerSettings;

import java.util.List;

import static pw.byakuren.discord.objects.cache.WriteState.PENDING_WRITE;

public class SetLogChannel extends Command {

    private final @NotNull Cache c;

    public SetLogChannel(@NotNull Cache c) {
        this.c = c;

        subcommands.add(new SubcommandList(this));
        subcommands.add(new Subcommand(new String[]{"set", "here"}, "Set the log channel to the current channel.", null, this) {
            @Override
            public void run(@NotNull Message message, @NotNull List<String> args) {
                set(message, args);
            }
        });
        subcommands.add(new Subcommand(new String[]{"view", "v"}, "See the existing log channel.", null, this) {
            @Override
            public void run(@NotNull Message message, @NotNull List<String> args) {
                view(message, args);
            }
        });
    }

    @Override
    public @NotNull String @NotNull [] getNames() {
        return new String[]{"logchannel","setlogchannel","slc"};
    }

    @Override
    public @NotNull String getHelp() {
        return "Manage the server log channel.";
    }

    @Override
    public @NotNull CommandPermission minimumPermission() {
        return CommandPermission.SERVER_ADMIN;
    }

    private void set(@NotNull Message message, List<String> args) {
        ServerCache sc = c.getServerCache(message.getGuild());
        ServerSettings s = new ServerSettings(message.getGuild(), ServerParameter.SERVER_LOG_CHANNEL, message.getChannel().getIdLong());
        s.write_state=PENDING_WRITE;
        sc.getSettings().getData().add(s);
        message.replyFormat("Set log channel to current channel (%s)",
                message.getTextChannel().getAsMention()).mentionRepliedUser(false).queue();
    }

    private void view(@NotNull Message message, List<String> args) {
        TextChannel chan = c.getServerCache(message.getGuild()).getLogChannel(message.getJDA());
        message.reply(chan != null ? "Log channel is currently set to "+chan.getAsMention() :
                "Log channel is not set.").mentionRepliedUser(false).queue();
    }
}
