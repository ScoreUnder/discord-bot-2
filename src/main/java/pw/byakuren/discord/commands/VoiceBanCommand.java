package pw.byakuren.discord.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import pw.byakuren.discord.commands.permissions.CommandPermission;
import pw.byakuren.discord.commands.subcommands.Subcommand;
import pw.byakuren.discord.commands.subcommands.SubcommandList;
import pw.byakuren.discord.objects.cache.Cache;
import pw.byakuren.discord.objects.cache.ServerCache;
import pw.byakuren.discord.objects.cache.datatypes.VoiceBan;
import pw.byakuren.discord.util.BotEmbed;

import java.time.LocalDateTime;
import java.util.List;

import static pw.byakuren.discord.objects.cache.WriteState.PENDING_WRITE;

public class VoiceBanCommand extends Command {

    private final @NotNull Cache c;

    public VoiceBanCommand(@NotNull Cache c) {
        this.c = c;

        subcommands.add(new SubcommandList(this));
        subcommands.add(new Subcommand(new String[]{"view", "v"}, null, null, this) {
            @Override
            public void run(@NotNull Message message, @NotNull List<String> args) {
                cmd_view(message, args);
            }
        });

        subcommands.add(new Subcommand(new String[]{"add", "a", "ban"}, null, null, this) {
            @Override
            public void run(@NotNull Message message, @NotNull List<String> args) {
                cmd_add(message, args);
            }
        });

        subcommands.add(new Subcommand(new String[]{"current", "c"}, null,
                "@User [duration] {reason}", this) {
            @Override
            public void run(@NotNull Message message, @NotNull List<String> args) {
                cmd_current(message, args);
            }
        });

        subcommands.add(new Subcommand(new String[]{"all"}, null, null, this) {
            @Override
            public void run(@NotNull Message message, @NotNull List<String> args) {
                cmd_all(message, args);
            }
        });

        subcommands.add(new Subcommand(new String[]{"cancel", "c"}, null, null, this) {
            @Override
            public void run(@NotNull Message message, @NotNull List<String> args) {
                cmd_cancel(message, args);
            }
        });
    }

    @Override
    public @NotNull String @NotNull [] getNames() {
        return new String[]{"voiceban", "vb"};
    }

    @Override
    public @NotNull String getHelp() {
        return "Ban a user from voice for a specified time.";
    }

    @Override
    public @NotNull CommandPermission minimumPermission() {
        return CommandPermission.MOD_ROLE;
    }

    private void cmd_all(@NotNull Message message, @NotNull List<String> args) {
        ServerCache sc = c.getServerCache(message.getGuild());
        StringBuilder s = new StringBuilder();
        for (VoiceBan vb : sc.getPrevVoiceBans(10)) {
            s.append(vb).append("\n\n");
        }
        EmbedBuilder b = BotEmbed.neutral("Past 10 voice bans").setDescription(s.toString());
        message.reply(b.build()).mentionRepliedUser(false).queue();
    }

    private void cmd_view(@NotNull Message message, @NotNull List<String> args) {
        ServerCache sc = c.getServerCache(message.getGuild());
        if (message.getMentionedMembers().isEmpty()) {
            message.reply("You must mention a user.").mentionRepliedUser(false).queue();
            return;
        }
        VoiceBan vb = sc.getValidVoiceBan(message.getMentionedMembers().get(0));
        if (vb == null) {
            message.reply("User is not banned from voice.").mentionRepliedUser(false).queue();
            return;
        }
        sendVoiceBanInfo(message.getTextChannel(), vb);
    }

    private void cmd_add(@NotNull Message message, @NotNull List<String> args) {
        if (args.size() < 2) return; //when user & time not provided
        Member banned = message.getMentionedMembers().get(0);
        long gid = message.getGuild().getIdLong();
        long uid = banned.getIdLong();
        long mid = message.getAuthor().getIdLong();
        String reason = "";
        if (args.size() > 2) {
            reason = String.join(" ", args.subList(banned.getEffectiveName().split(" ").length + 1, args.size()));
        }
        if (reason.isEmpty()) reason = null;
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = parseTime(args.get(banned.getEffectiveName().split(" ").length));
        VoiceBan vb = new VoiceBan(gid, uid, mid, start, end, reason);
        vb.write_state = PENDING_WRITE;
        ServerCache sc = c.getServerCache(message.getGuild());
        sc.getVoiceBans().getData().add(vb);
        EmbedBuilder b = BotEmbed.ok("Voice Banned " + banned.getUser().getName())
                .setDescription("Reason: `" + (reason == null ? "<None given>" : reason) + "`")
                .setAuthor(banned.getUser().getName(), null, banned.getUser().getEffectiveAvatarUrl())
                .setFooter("Banned by " + message.getAuthor().getName() + " | Expires")
                .setTimestamp(vb.getExpireTime());
        message.reply(b.build()).mentionRepliedUser(false).queue();
        if (!message.getGuild().getSelfMember().hasPermission(Permission.VOICE_MOVE_OTHERS)) {
            message.reply(
                    "Note:Bot lacks VOICE_MOVE_OTHERS permission, removing banned users from voice will NOT work.")
                    .mentionRepliedUser(false).queue();
        }
    }

    private void cmd_current(@NotNull Message message, @NotNull List<String> args) {
        ServerCache sc = c.getServerCache(message.getGuild());
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < sc.getValidVoiceBans().size() && i < 10; i++) {
            s.append(sc.getValidVoiceBans().get(i)).append("\n\n");
        }
        EmbedBuilder b = BotEmbed.neutral("Current voice bans").setDescription(s.toString());
        message.reply(b.build()).mentionRepliedUser(false).queue();
    }

    private void cmd_cancel(@NotNull Message message, @NotNull List<String> args) {
        ServerCache sc = c.getServerCache(message.getGuild());
        if (message.getMentionedMembers().isEmpty()) {
            message.reply("You must mention a user.").mentionRepliedUser(false).queue();
            return;
        }
        VoiceBan vb = sc.getValidVoiceBan(message.getMentionedMembers().get(0));
        if (vb == null) {
            message.reply("User is not banned from voice.").mentionRepliedUser(false).queue();
            return;
        }
        vb.cancel();
        message.reply("Canceled voice ban for user <@" + vb.getMemberId() + ">").mentionRepliedUser(false).queue();
    }

    private @NotNull LocalDateTime parseTime(@NotNull String t) {
        LocalDateTime n = LocalDateTime.now();
        while (!t.isEmpty()) {
            String f = null;
            for (int i = 2; i <= t.length(); i++) {
                if (t.substring(0, i).matches("\\d*[A-z]")) {
                    f = t.substring(0, i);
                    t = t.substring(i);
                    break;
                }
            }
            if (f == null) break;
            int l = Integer.parseInt(f.substring(0, f.length() - 1));
            char c = f.charAt(f.length() - 1);
            switch (c) {
                case 'd':
                    n = n.plusDays(l);
                    break;
                case 'h':
                    n = n.plusHours(l);
                    break;
                case 'm':
                    n = n.plusMinutes(l);
                    break;
                default:
                    //unrecognized unit
            }
        }
        return n;
    }

    private void sendVoiceBanInfo(@NotNull TextChannel c, @NotNull VoiceBan vb) {
        EmbedBuilder b = BotEmbed.neutral("Voice ban").setDescription(String.format(
                "Banned member: <@%d>\n" +
                        "Banned by:<@%d>\n" +
                        "Banned on:%s\n" +
                        "Expires on:%s\n" +
                        "Reason:%s",
                vb.getMemberId(), vb.getModId(), VoiceBan.formatDateTime(vb.getStartTime()),
                VoiceBan.formatDateTime(vb.getExpireTime()), vb.getReason()));
        c.sendMessage(b.build()).queue();
    }

}

