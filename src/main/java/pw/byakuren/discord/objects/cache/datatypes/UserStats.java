package pw.byakuren.discord.objects.cache.datatypes;

import org.jetbrains.annotations.NotNull;
import pw.byakuren.discord.DatabaseManager;
import pw.byakuren.discord.objects.Statistic;

import static pw.byakuren.discord.objects.cache.WriteState.PENDING_WRITE;

public class UserStats extends CacheEntry {

    private final long server;
    private final long user;
    private int reactions_sent = 0;
    private int reactions_received = 0;
    private int messages_sent = 0;
    private int messages_deleted = 0;
    private int attachments_sent = 0;

    public UserStats(long server, long user, int reactions_sent, int reactions_received, int messages_sent, int messages_deleted, int attachments_sent) {
        this.server = server;
        this.user = user;
        this.reactions_sent = reactions_sent;
        this.reactions_received = reactions_received;
        this.messages_sent = messages_sent;
        this.messages_deleted = messages_deleted;
        this.attachments_sent = attachments_sent;
    }

    public UserStats(long server, long user) {
        this.server = server;
        this.user = user;
    }

    public long getServer() {
        return server;
    }

    public long getUser() {
        return user;
    }

    public int getStatistic(@NotNull Statistic e) {
        switch (e) {
            case REACTIONS_SENT:
                return reactions_sent;
            case REACTIONS_RECEIVED:
                return reactions_received;
            case MESSAGES_SENT:
                return messages_sent;
            case MESSAGES_DELETED:
                return messages_deleted;
            case ATTACHMENTS_SENT:
                return attachments_sent;
            default:
                return -1;
        }
    }

    public boolean incrementStatistic(@NotNull Statistic e) {
        switch (e) {
            case REACTIONS_SENT:
                reactions_sent++;
                break;
            case REACTIONS_RECEIVED:
                reactions_received++;
                break;
            case MESSAGES_SENT:
                messages_sent++;
                break;
            case MESSAGES_DELETED:
                messages_deleted++;
                break;
            case ATTACHMENTS_SENT:
                attachments_sent++;
                break;
            default:
                return false;
        }
        write_state = PENDING_WRITE;
        return true;
    }

    public boolean decrementStatistic(@NotNull Statistic e) {
        switch (e) {
            case REACTIONS_SENT:
                reactions_sent--;
                break;
            case REACTIONS_RECEIVED:
                reactions_received--;
                break;
            case MESSAGES_SENT:
                messages_sent--;
                break;
            case MESSAGES_DELETED:
                messages_deleted--;
                break;
            case ATTACHMENTS_SENT:
                attachments_sent--;
                break;
            default:
                return false;
        }
        write_state=PENDING_WRITE;
        return true;
    }

    public boolean setStatistic(@NotNull Statistic e, int v) {
        switch (e) {
            case REACTIONS_SENT:
                reactions_sent=v;
                break;
            case REACTIONS_RECEIVED:
                reactions_received=v;
                break;
            case MESSAGES_SENT:
                messages_sent=v;
                break;
            case MESSAGES_DELETED:
                messages_deleted=v;
                break;
            case ATTACHMENTS_SENT:
                attachments_sent=v;
                break;
            default:
                return false;
        }
        write_state=PENDING_WRITE;
        return true;
    }

    @Override
    protected void write(@NotNull DatabaseManager dbmg) {
        dbmg.editUserChatData(server, user, Statistic.REACTIONS_SENT.datapoint_name, reactions_sent);
        dbmg.editUserChatData(server, user, Statistic.REACTIONS_RECEIVED.datapoint_name, reactions_received);
        dbmg.editUserChatData(server, user, Statistic.MESSAGES_SENT.datapoint_name, messages_sent);
        dbmg.editUserChatData(server, user, Statistic.MESSAGES_DELETED.datapoint_name, messages_deleted);
        dbmg.editUserChatData(server, user, Statistic.ATTACHMENTS_SENT.datapoint_name, attachments_sent);
    }

    @Override
    protected void delete(@NotNull DatabaseManager dbmg) {
        //todo delete chat data option
    }
}
