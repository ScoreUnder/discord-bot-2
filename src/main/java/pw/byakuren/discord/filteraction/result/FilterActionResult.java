package pw.byakuren.discord.filteraction.result;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import pw.byakuren.discord.objects.cache.datatypes.MessageFilterAction;
import pw.byakuren.discord.util.BotEmbed;
import pw.byakuren.discord.util.ScalaReplacements;

import java.time.OffsetDateTime;
import java.util.List;

public class FilterActionResult {

    public final @NotNull MessageFilterAction mfa;
    public final @NotNull OffsetDateTime time = OffsetDateTime.now();
    public final @NotNull Message message;
    public final boolean activated;
    public final @NotNull List<FilterResult> filterResults;
    public final @NotNull List<ActionResult> actionResults;

    public FilterActionResult(@NotNull MessageFilterAction messageFilterAction, @NotNull Message message, boolean activated,
                              @NotNull List<FilterResult> filterResults, @NotNull List<ActionResult> actionResults) {
        this.mfa = messageFilterAction;
        this.message = message;
        this.activated = activated;
        this.filterResults = filterResults;
        this.actionResults = actionResults;
    }

    public int countFiltersPassed() {
        int i = 0;
        for (FilterResult fa: filterResults) {
            if (fa.functionName()) i++;
        }
        return i;
    }

    public int countActionsTaken () {
        int c = 0;
        for (ActionResult ar: actionResults) {
            if (ar.success) c++;
        }
        return c;
    }

    public @NotNull MessageEmbed embedReport() {
        EmbedBuilder eb = BotEmbed.information(mfa.getName());
        if (filterResults.size()==0) {
            eb.addField("NOTICE",
                    "**As a safety mechanism, actions will naturally not occur unless the FA has at least one filter. " +
                            "To bypass this, you can add the true() filter which always returns true.**", false);
        }
        String filterTitle = String.format("Filter Results (%d/%d)", countFiltersPassed(), filterResults.size());
        String actionTitle = String.format("Actions Taken (%d/%d)", countActionsTaken(), mfa.getActions().size());
        eb.addField("Message Contents", message.getContentRaw(), false);
        eb.addField(filterTitle, ScalaReplacements.mkString(filterResults,"\n"), false);
        String actionStr =
                actionResults.isEmpty() ? "No actions taken, as not all criteria were met" : ScalaReplacements.mkString(actionResults, "\n");
        eb.addField(actionTitle, actionStr, false);
        return eb.build();
    }
}
