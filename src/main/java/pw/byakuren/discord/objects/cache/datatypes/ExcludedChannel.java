package pw.byakuren.discord.objects.cache.datatypes;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

public class ExcludedChannel extends CacheDatatype {


    private TextChannel channel;

    public ExcludedChannel(TextChannel channel) {
        this.channel = channel;
    }

    public static ExcludedChannel create(JDA jda, long channelid) {
        return new ExcludedChannel(jda.getTextChannelById(channelid));
    }

}
