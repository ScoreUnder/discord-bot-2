package pw.byakuren.discord.commands.richcommands;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;
import pw.byakuren.discord.commands.Command;

public abstract class RichCommand extends Command {

    protected boolean global = false;
    public abstract void onButtonClick(ButtonClickEvent event);

    public abstract void runSlash(SlashCommandEvent event);

    public boolean isGlobal() {
        return global;
    }

    public final boolean isSlash() {
        return getType() == CommandType.SLASH || getType() == CommandType.INTEGRATED;
    }

    public @NotNull String @NotNull [] getRequestedButtonIDs() {
        return new String[0];
    }
}
