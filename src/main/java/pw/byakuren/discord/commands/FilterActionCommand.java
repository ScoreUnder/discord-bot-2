package pw.byakuren.discord.commands;

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import pw.byakuren.discord.commands.permissions.CommandPermission;
import pw.byakuren.discord.commands.subcommands.Subcommand;
import pw.byakuren.discord.commands.subcommands.SubcommandList;
import pw.byakuren.discord.filteraction.Action;
import pw.byakuren.discord.filteraction.Filter;
import pw.byakuren.discord.filteraction.result.FilterActionResult;
import pw.byakuren.discord.objects.cache.Cache;
import pw.byakuren.discord.objects.cache.ServerCache;
import pw.byakuren.discord.objects.cache.WriteState;
import pw.byakuren.discord.objects.cache.datatypes.MessageFilterAction;
import pw.byakuren.discord.util.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class FilterActionCommand extends Command {

    private final @NotNull Cache c;

    private @NotNull Pattern syntaxPattern = Pattern.compile("!?[A-z]+((\\()|(<)).*((\\))|(>))", Pattern.DOTALL);

    public FilterActionCommand(@NotNull Cache c) {
        this.c = c;
        subcommands.add(new SubcommandList(this));
        subcommands.add(new Subcommand(new String[]{"test"}, "Test a given FA on your message, with verbose results.",
                "<faName> [more message content to test]", this) {
            @Override
            public void run(@NotNull Message message, @NotNull List<String> args) {
                cmd_test(message, args);
            }
        });
        subcommands.add(new Subcommand(new String[]{"add", "a"}, "Add a new filter or action", "<faName> <filterOrAction>", this) {
            @Override
            public void run(@NotNull Message message, @NotNull List<String> args) {
                cmd_add(message, args);
            }
        });
        subcommands.add(new Subcommand(new String[]{"multiadd", "ma"}, "Like add, but for multiple filters or actions. Incompatible with spaces, making it unsuitable for actions with string inputs.", "<faName> <filterOrAction>...", this) {
            @Override
            public void run(@NotNull Message message, @NotNull List<String> args) {
                cmd_multiadd(message, args);
            }
        });
        subcommands.add(new Subcommand(new String[]{"list", "l"}, "List all FAs on the server, or see the config of a specific one.",
                "<faName>", this) {
            @Override
            public void run(@NotNull Message message, @NotNull List<String> args) {
                cmd_list(message, args);
            }
        });
        subcommands.add(new Subcommand(new String[]{"trash", "t"}, "Delete an entire FA.", "<faName>", this) {
            @Override
            public void run(@NotNull Message message, @NotNull List<String> args) {
                cmd_trash(message, args);
            }
        });
        subcommands.add(new Subcommand(new String[]{"availableFilters", "af"}, "See available filters.", null, this) {
            @Override
            public void run(@NotNull Message message, @NotNull List<String> args) {
                cmd_af(message, args);
            }
        });
        subcommands.add(new Subcommand(new String[]{"availableActions", "aa"}, "See available actions", null, this) {
            @Override
            public void run(@NotNull Message message, @NotNull List<String> args) {
                cmd_aa(message, args);
            }
        });
        subcommands.add(new Subcommand(new String[]{"remove", "r"}, "Remove a filter or action", "<faName> <filterOrAction>", this) {
            @Override
            public void run(@NotNull Message message, @NotNull List<String> args) {
                cmd_remove(message, args);
            }
        });
        subcommands.add(new Subcommand(new String[]{"primer"}, "See a primer on how to use filter actions", null, this) {
            @Override
            public void run(@NotNull Message message, @NotNull List<String> args) {
                message.reply(
                        BotEmbed.information("Filter Action primer")
                                .setDescription("'Filter Actions' are a powerful way to customize your server moderating.\n\n" +
                                        "As the name suggests, they rely on the use of one or more filters to select which messages " +
                                        "you want to act on, and apply any number of actions to them.\n\n" +
                                        "[Learn more on the wiki](https://github.com/Brod8362/discord-bot-2/wiki/Filter-Action-Primer)").build()
                ).mentionRepliedUser(false).queue();
            }
        });
    }

    @Override
    public @NotNull String @NotNull [] getNames() {
        return new String[]{"filteraction", "fa"};
    }

    @Override
    public @NotNull CommandPermission minimumPermission() {
        return CommandPermission.MOD_ROLE;
    }

    private void cmd_trash(@NotNull Message message, @NotNull List<String> args) {
        ServerCache sc = c.getServerCache(message.getGuild());
        if (args.size() == 1) {
            if (sc.getFilterActionByName(args.get(0)) == null) {
                message.reply(
                        BotEmbed.bad("Cannot find FA with name " + args.get(0)).build()
                ).mentionRepliedUser(false).queue();
            } else {
                for (MessageFilterAction mfa : sc.getAllFilterActions()) {
                    if (mfa.getName().equals(args.get(0))) {
                        mfa.write_state = WriteState.PENDING_DELETE;
                        message.addReaction(BotEmoji.TRASH.unicode).queue();
                    }
                }
            }
        } else {
            message.reply(
                    BotEmbed.bad("Please provide the name of one FA to delete.").build()
            ).mentionRepliedUser(false).queue();
        }
    }

    private void cmd_af(@NotNull Message message, @NotNull List<String> args) {
        message.reply(String.format("```%s```", ScalaReplacements.mkString(Arrays.asList(MessageFilterParser.getExamples().clone()), "\n")))
                .mentionRepliedUser(false).queue();
    }

    private void cmd_aa(@NotNull Message message, @NotNull List<String> args) {
        message.reply(String.format("```%s```", ScalaReplacements.mkString(Arrays.asList(MessageActionParser.getExamples().clone()), "\n")))
                .mentionRepliedUser(false).queue();
    }

    private void cmd_test(@NotNull Message msg, @NotNull List<String> args) {
        if (args.isEmpty()) {
            msg.reply(
                    BotEmbed.bad("You didn't provide an FA to test.").build()
            ).mentionRepliedUser(false).queue();
            return;
        }
        ServerCache sc = c.getServerCache(msg.getGuild());
        MessageFilterAction mfa = sc.getFilterActionByName(args.get(0));
        if (mfa == null) {
            msg.reply(
                    BotEmbed.bad("No FA found by the name "+args.get(0)).build()
            ).mentionRepliedUser(false).queue();
            return;
        }
        FilterActionResult far = mfa.check(msg);
        msg.reply(far.embedReport()).mentionRepliedUser(false).queue();
    }

    private void cmd_add(@NotNull Message msg, @NotNull List<String> args) {
        if (args.isEmpty()) {
            msg.reply(
                    BotEmbed.bad("You didn't provide an FA to add to.").build()
            ).mentionRepliedUser(false).queue();
            return;
        }
        String mfaName = args.get(0);
        ServerCache sc = c.getServerCache(msg.getGuild());
        String qString = ScalaReplacements.mkString(args.subList(1, args.size()));
        Filter<Message> filter;
        Action<Message> action;
        String errorDetail = "filter";
        try {
            filter = MessageFilterParser.fromString(qString);
            errorDetail = "action";
            action = MessageActionParser.fromString(qString);
        } catch (Exception e) {
            msg.reply(
                    BotEmbed.bad("There was an exception handling your "+errorDetail)
                    .setDescription(e.getMessage()).build()
            ).mentionRepliedUser(false).queue();
            return;
        }

        if (!syntaxCheck(qString)) {
            msg.reply(
                    BotEmbed.bad("Seems like you have a syntax error. Check you spelled everything right and try again. " +
                            "Note that filters use () and actions use <>.").build()
            ).mentionRepliedUser(false).queue();
            return;
        }

        MessageFilterAction mfa = sc.getFilterActionByName(mfaName);
        if (mfa == null) {
            mfa = new MessageFilterAction(msg.getGuild().getIdLong(), mfaName);
            sc.getMessageFilterActions().getData().add(mfa);
        }
        if (filter != null) {
            mfa.addFilter(filter);
        } else if (action != null) {
            mfa.addAction(action);
        } else {
            msg.reply(
                    BotEmbed.bad("Your filter/action doesn't exist. Check your spelling and the examples list, and try again.").build()
            ).mentionRepliedUser(false).queue();
            return;
        }
        mfa.write_state = WriteState.PENDING_WRITE;
        msg.addReaction(BotEmoji.OK.toString()).queue();
    }

    private void cmd_multiadd(@NotNull Message msg, @NotNull List<String> args) {
        if (args.size()<2) {
            msg.reply("Insufficient arguments (need 2 or more)").mentionRepliedUser(false).queue();
            return;
        }
        ServerCache sc = c.getServerCache(msg.getGuild());
        MessageFilterAction mfa = sc.getFilterActionByName(args.get(0));

        if (mfa == null) {
            mfa = new MessageFilterAction(msg.getGuild().getIdLong(), args.get(0));
            mfa.write_state = WriteState.PENDING_WRITE;
            sc.getMessageFilterActions().getData().add(mfa);
        }

        ArrayList<String> invalid = new ArrayList<>();
        for (String s: args.subList(1,args.size())) {
            Filter<Message> filter = MessageFilterParser.fromString(s);
            Action<Message> action = MessageActionParser.fromString(s);

            if (filter != null) {
                mfa.addFilter(filter);
                mfa.write_state = WriteState.PENDING_WRITE;
            } else if (action != null) {
                mfa.addAction(action);
                mfa.write_state = WriteState.PENDING_WRITE;
            } else {
                invalid.add(s);
            }
        }
        if (invalid.isEmpty()) {
            msg.addReaction(BotEmoji.CHECK.unicode).queue();
        } else {
            msg.reply(String.format("Invalid inputs: `%s`. %d/%d completed correctly.",
                    ScalaReplacements.mkString(invalid, ","), invalid.size(), args.size()-1))
                    .mentionRepliedUser(false).queue();
        }
    }

    private void cmd_list(@NotNull Message msg, @NotNull List<String> args) {
        ServerCache sc = c.getServerCache(msg.getGuild());
        if (sc.getAllFilterActions().size() == 0) {
            msg.reply(
                    BotEmbed.information("You haven't configured any filteractions yet.").build()
            ).mentionRepliedUser(false).queue();
            return;
        }
        if (args.size() == 0) {
            //generic list of all
            msg.reply(String.format("```%s```", ScalaReplacements.mkString(sc.getAllFilterActions(), "\n")))
                    .allowedMentions(Collections.emptySet()).mentionRepliedUser(false).queue();
        } else if (args.size() == 1) {
            //specific instance of a filter action
            MessageFilterAction mfa = sc.getFilterActionByName(args.get(0));
            if (mfa == null) {
                msg.reply(
                        BotEmbed.bad(String.format("The FA \"%s\" doesn't exist. Check your spelling and try again.", args.get(0))).build()
                ).mentionRepliedUser(false).queue();
            } else {
                //todo make this the "info" embed
                msg.reply("`" + mfa.prettyPrint() + "`").allowedMentions(Collections.emptySet()).mentionRepliedUser(false).queue();
            }
        } else {
            //too many arguments
            msg.reply(
                    BotEmbed.bad("Too many arguments, expected 0-1 but got " + args.size()).build()
            ).mentionRepliedUser(false).queue();
        }
    }

    private void cmd_remove(@NotNull Message msg, @NotNull List<String> args) {
        if (args.isEmpty()) {
            msg.reply(
                    BotEmbed.bad("You didn't provide an FA to remove from.").build()
            ).mentionRepliedUser(false).queue();
            return;
        }
        String mfaName = args.get(0);
        ServerCache sc = c.getServerCache(msg.getGuild());
        String qString = ScalaReplacements.mkString(args.subList(1, args.size()));
        MessageFilterAction mfa = sc.getFilterActionByName(mfaName);
        String removeName = qString.split("[(<]")[0];

        if (mfa == null) {
            msg.reply(
                    BotEmbed.bad("No FA by the name " + mfaName + " exists.").build()
            ).mentionRepliedUser(false).queue();
            return;
        }

        if (mfa.removeAction(removeName) || mfa.removeFilter(removeName)) {
            msg.addReaction(BotEmoji.TRASH.unicode).queue();
        }
    }

    private boolean syntaxCheck(@NotNull String s) {
        return syntaxPattern.matcher(s).find();
    }
}
