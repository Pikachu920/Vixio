package me.iblitzkriegi.vixio.expressions.guild.categories;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.iblitzkriegi.vixio.Vixio;
import me.iblitzkriegi.vixio.changers.ChangeableSimpleExpression;
import me.iblitzkriegi.vixio.util.Util;
import me.iblitzkriegi.vixio.util.skript.EasyMultiple;
import me.iblitzkriegi.vixio.util.wrapper.Bot;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.exceptions.PermissionException;
import org.bukkit.event.Event;

public class ExprCategories extends ChangeableSimpleExpression<Category> implements EasyMultiple<Guild, Category> {
    static {
        Vixio.getInstance().registerPropertyExpression(ExprCategories.class, Category.class,
                "categories", "guilds")
                .setName("Categories of guild")
                .setDesc("Get all of the categories in a guild. You may add a category to this by name which creates a category, or delete/remove a category by inputting a category.")
                .setExample(
                        "discord command $categories:",
                        "\ttrigger:",
                        "\t\tset {_categories::*} to the categories of event-guild",
                        "\t\treply with \"Here are the current categories: `%{_categories::*}%`\""
                );
    }

    private Expression<Guild> guilds;

    @Override
    protected Category[] get(Event e) {
        return convert(getReturnType(), guilds.getAll(e), g -> g.getCategories().toArray(new Category[g.getCategories().size()]));
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends Category> getReturnType() {
        return Category.class;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "categories of " + guilds.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        guilds = (Expression<Guild>) exprs[0];
        return true;
    }

    @Override
    public Class<?>[] acceptChange(Changer.ChangeMode mode, boolean vixioChanger) {
        if (mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.DELETE) {
            return new Class[]{String.class};
        }
        return null;
    }

    @Override
    public void change(Event e, Object[] delta, Bot bot, Changer.ChangeMode mode) {
        change(guilds.getAll(e), guild -> {
            guild = Util.bindGuild(bot, guild);
            if (guild == null) {
                return;
            }
            try {
                switch (mode) {
                    case ADD:
                        String name = (String) delta[0];
                        guild.getController().createCategory(name).queue();
                        break;
                    case DELETE:
                        for (Category category : guild.getCategories()) {
                            category.delete().queue();
                        }
                }
            } catch (PermissionException x) {
                Vixio.getErrorHandler().needsPerm(bot, mode.name().toLowerCase() + " category", x.getPermission().getName());
            }
        });
    }
}
