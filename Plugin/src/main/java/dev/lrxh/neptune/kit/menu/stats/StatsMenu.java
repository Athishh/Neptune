package dev.lrxh.neptune.kit.menu.stats;

import dev.lrxh.neptune.configs.impl.MenusLocale;
import dev.lrxh.neptune.kit.Kit;
import dev.lrxh.neptune.kit.menu.stats.button.StatButton;
import dev.lrxh.neptune.utils.menu.Button;
import dev.lrxh.neptune.utils.menu.Filter;
import dev.lrxh.neptune.utils.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class StatsMenu extends Menu {
    private final Player target;

    public StatsMenu(String name) {
        this.target = Bukkit.getPlayer(name);
    }

    @Override
    public String getTitle(Player player) {
        return MenusLocale.STAT_TITLE.getString().replace("<player>", target.equals(player) ? "Your" : target.getName());
    }

    @Override
    public int getSize() {
        return MenusLocale.STAT_SIZE.getInt();
    }

    @Override
    public Filter getFilter() {
        return Filter.valueOf((MenusLocale.STAT_FILTER.getString()));
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int i = MenusLocale.STAT_STARTING_SLOT.getInt();
        for (Kit kit : plugin.getKitManager().kits) {
            buttons.put(i++, new StatButton(kit, target));
        }
        return buttons;
    }
}
