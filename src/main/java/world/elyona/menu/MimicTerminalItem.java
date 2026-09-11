package world.elyona.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * MIMIC端末アイテム（コンパスベース）。
 * PersistentDataContainerにタグを付け、右クリックで統合メニューを開く目印にする。
 */
public final class MimicTerminalItem {

    private static final String KEY = "mimic_terminal";

    private MimicTerminalItem() {}

    public static ItemStack create(Plugin plugin) {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("MIMIC端末", NamedTextColor.LIGHT_PURPLE));
        meta.lore(List.of(
                Component.text("右クリックでMIMIC端末を開く", NamedTextColor.GRAY)
        ));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, KEY), PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isTerminal(Plugin plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(
                new NamespacedKey(plugin, KEY), PersistentDataType.BYTE);
    }
}
