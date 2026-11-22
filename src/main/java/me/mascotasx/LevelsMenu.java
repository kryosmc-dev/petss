package me.mascotasx;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class LevelsMenu extends Menu {
    private static final int PER_PAGE = 45;
    private final String petId;
    private final int page;

    public LevelsMenu(MascotasX plugin, Player p, String petId, int page) {
        super(plugin, p, plugin.getConfig().getString("titles.levels", "Niveles"));
        this.petId = petId;
        this.page = page;
    }

    @Override
    protected void build() {
        fill();
        PetData d = data(petId);
        PetType t = plugin.manager().type(petId);
        if (d == null || t == null) { player.closeInventory(); return; }
        int max = plugin.manager().maxLevel();
        int start = (page - 1) * PER_PAGE + 1;

        for (int i = 0; i < PER_PAGE && start + i <= max; i++) {
            int lvl = start + i;
            boolean reached = d.level >= lvl;
            boolean milestone = plugin.getConfig().isConfigurationSection("levels.rewards." + lvl);
            Material m = reached ? Material.LIME_STAINED_GLASS_PANE
                    : lvl == d.level + 1 ? Material.YELLOW_STAINED_GLASS_PANE
                    : milestone ? Material.RED_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;

            List<String> lore = new ArrayList<>();
            lore.add(" ");
            lore.add("<white>Recompensas:");
            lore.add("<gray>» Otorga <green>+" + Text.num(lvl * plugin.manager().bonus()) + "%<gray> de bonificación de daño");
            ConfigurationSection r = plugin.getConfig().getConfigurationSection("levels.rewards." + lvl);
            if (r != null) for (String s : r.getStringList("description")) lore.add("<gray>» " + s);

            ConfigurationSection es = plugin.getConfig().getConfigurationSection("effects");
            if (es != null) for (String k : es.getKeys(false))
                if (es.getInt(k + ".level", 1) == lvl) lore.add("<gray>» Desbloquea el efecto <aqua>" + es.getString(k + ".name", k));
            if (lvl == plugin.getConfig().getInt("cosmetics.hat-level", 5)) lore.add("<gray>» Desbloquea <aqua>usarla de sombrero");
            if (lvl == plugin.getConfig().getInt("cosmetics.mount-level", 10)) lore.add("<gray>» Desbloquea <aqua>montarla");

            lore.add(" ");
            lore.add(reached ? "<green><bold>DESBLOQUEADO" : "<red><bold>BLOQUEADO");
            inv.setItem(i, Items.of(m, lvl, "<gold>" + d.displayName(t) + " <white>Lvl. " + lvl, lore));
        }
        if (page > 1) inv.setItem(48, Items.of(Material.ARROW, "<green>« Anterior"));
        if (start + PER_PAGE <= max) inv.setItem(50, Items.of(Material.ARROW, "<green>Siguiente »"));
        inv.setItem(49, Items.of(Material.BARRIER, "<red>Volver"));
    }

    @Override
    public void click(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if (slot == 49) new DataMenu(plugin, player, petId).open();
        else if (slot == 48 && page > 1) new LevelsMenu(plugin, player, petId, page - 1).open();
        else if (slot == 50 && inv.getItem(50) != null && inv.getItem(50).getType() == Material.ARROW)
            new LevelsMenu(plugin, player, petId, page + 1).open();
    }
}
