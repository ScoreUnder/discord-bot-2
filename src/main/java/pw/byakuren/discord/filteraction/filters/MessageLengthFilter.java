package pw.byakuren.discord.filteraction.filters;

import net.dv8tion.jda.api.entities.Message;
import pw.byakuren.discord.filteraction.MessageFilter;
import pw.byakuren.discord.filteraction.arguments.Argument;
import pw.byakuren.discord.filteraction.arguments.ArgumentType;
import pw.byakuren.discord.filteraction.result.FilterResult;

public class MessageLengthFilter extends MessageFilter {

    private final int length;

    public MessageLengthFilter(int length) {
        this.length = length;
    }

    @Override
    public String getRepresentation() {
        return "msgLength";
    }

    @Override
    public String[] getArguments() {
        return new String[]{length+""};
    }

    @Override
    public String getArgumentsDisplay() {
        return length+"";
    }

    @Override
    public Argument[] getExpectedArguments() {
        return new Argument[]{new Argument("length",  ArgumentType.NUMBER, "minimum length of message needed to trigger")};
    }

    @Override
    public FilterResult apply(Message obj) {
        boolean trigger = obj.getContentRaw().length() >= length;
        String reason = trigger ? null : "the message is not at least "+length+" characters long";
        return new FilterResult(trigger, getDisplay(), reason);
    }
}
