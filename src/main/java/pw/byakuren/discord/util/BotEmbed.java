package pw.byakuren.discord.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class BotEmbed {

    public static final Color OK_COLOR = new Color(108, 255, 70);
    public static final Color BAD_COLOR = new Color(255, 100, 78);
    public static final Color ERROR_COLOR = new Color(255,42,24);
    public static final Color INFORMATION_COLOR = new Color(32,196,196);
    public static final Color NEUTRAL_COLOR = new Color(128, 128, 196);

    public static EmbedBuilder neutral(String msg) {
        return new EmbedBuilder().setTitle(msg).setColor(NEUTRAL_COLOR);
    }

    public static EmbedBuilder ok(String msg) {
        return neutral(msg).setColor(OK_COLOR);
    }

    public static EmbedBuilder bad(String msg) {
        return neutral(msg).setColor(BAD_COLOR);
    }

    public static EmbedBuilder information(String msg) {
        return neutral(msg).setColor(INFORMATION_COLOR);
    }

    public static EmbedBuilder error(Exception e) {
        return neutral(String.format("big ouchie! - ```%s```", e.getLocalizedMessage()))
                .setColor(ERROR_COLOR)
                .setDescription(Arrays.stream(e.getStackTrace())
                        .map(StackTraceElement::toString)
                        .collect(Collectors.joining("\n")));
    }

    public static EmbedBuilder footerAuthor(String msg, User u) {
        return neutral(msg).setFooter(u.getName()+"#"+u.getDiscriminator(), u.getEffectiveAvatarUrl());
    }

    public static EmbedBuilder footerAuthor(String msg, User u, Color c) {
        return footerAuthor(msg, u).setColor(c);
    }

    public static EmbedBuilder headerAuthor(String msg, String url, User u) {
        return neutral(msg).setAuthor(String.format("%s (%d)", u.getName(), u.getIdLong()), url, u.getEffectiveAvatarUrl());
    }

    public static EmbedBuilder headerAuthor(String msg, User u) {
        return headerAuthor(msg, null, u);
    }

    public static EmbedBuilder headerAuthor(String msg, String url, User u, Color c) {
        return headerAuthor(msg,url,u).setColor(c);
    }

    public static EmbedBuilder headerAuthor(String msg, User u, Color c) {
        return headerAuthor(msg, null, u , c);
    }

}
