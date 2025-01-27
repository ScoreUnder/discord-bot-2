package pw.byakuren.discord.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;


/**
 * This class is necessary because java sucks.
 */
public abstract class ScalaReplacements {

    public static @NotNull String mkString(@NotNull List<?> l, @NotNull String sep) {
        if (l.isEmpty()) { return ""; }
        StringBuilder f = new StringBuilder();
        for (int i = 0; i < l.size()-1; i++) {
            f.append(l.get(i).toString()).append(sep);
        }
        return f+l.get(l.size()-1).toString();
    }

    public static @NotNull String mkString(@NotNull List<?> l) {
        return mkString(l, " ");
    }
}
