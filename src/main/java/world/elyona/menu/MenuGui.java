package world.elyona.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import world.elyona.core.rank.RankTier;
import world.elyona.economy.ElyonaEconomyPlugin;
import world.elyona.rank.ElyonaRankPlugin;
import world.elyona.items.ElyonaItemsPlugin;
import world.elyona.items.combat.PlayerStatManager;
import world.elyona.items.combat.WeaponDisplay;

import java.util.List;

/**
 * /menu または MIMIC端末アイテムで開く統合メニュー（ラージチェスト・54スロット）。
 *
 * スロット配置:
 *  [0-8]   上段: 装飾
 *  [9-17]  プレイヤー情報（スロット13）・ステータス（スロット14）
 *  [18-26] 余白行（装飾のみ）
 *  [27-35] 経済 / ダンジョンランク / 住民ランク / 称号
 *  [36-44] Serathランク / MIMIC売却 / 土地情報 / 予備
 *  [45-53] サーバー情報（未使用） / 閉じる
 */
public class MenuGui implements InventoryHolder {

    public static final int SLOT_PLAYER_INFO = 13;
    public static final int SLOT_STATUS = 14;
    public static final int SLOT_ECONOMY = 28;
    public static final int SLOT_DUNGEON_RANK = 30;
    public static final int SLOT_RESIDENT_RANK = 32;
    public static final int SLOT_TITLE = 34;
    public static final int SLOT_SERATH_RANK = 37;
    public static final int SLOT_MIMIC_SELL = 39;
    public static final int SLOT_LAND_INFO = 41;
    public static final int SLOT_CLOSE = 53;

    private final Inventory inventory;

    public MenuGui(Player player) {
        inventory = Bukkit.createInventory(this, 54, Component.text("[MIMIC] 端末", NamedTextColor.LIGHT_PURPLE));
        build(player);
    }

    private void build(Player player) {
        ItemStack filler = makeItem(Material.PURPLE_STAINED_GLASS_PANE, Component.text(" "), null);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
        // 余白行はグレーで一段暗くし、プロフィールと機能一覧を視覚的に分ける
        ItemStack spacer = makeItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "), null);
        for (int i = 18; i <= 26; i++) {
            inventory.setItem(i, spacer);
        }

        inventory.setItem(SLOT_PLAYER_INFO, buildPlayerInfo(player));
        inventory.setItem(SLOT_STATUS, buildStatus(player));
        inventory.setItem(SLOT_ECONOMY, buildEconomy(player));
        inventory.setItem(SLOT_DUNGEON_RANK, buildDungeonRank(player));
        inventory.setItem(SLOT_RESIDENT_RANK, buildLocked("住民ランク"));
        inventory.setItem(SLOT_TITLE, buildTitle());
        inventory.setItem(SLOT_SERATH_RANK, buildLocked("Serathランク"));
        inventory.setItem(SLOT_MIMIC_SELL, buildMimicSell());
        inventory.setItem(SLOT_LAND_INFO, buildLocked("土地・コロニー情報"));
        // サーバー情報スロット(45)は内容未定のため装飾のまま空けておく
        inventory.setItem(SLOT_CLOSE, buildClose());
    }

    private ItemStack buildPlayerInfo(Player player) {
        ElyonaRankPlugin rank = ElyonaRankPlugin.getInstance();
        long balance = economyCache(player).getBalance(player.getUniqueId());
        boolean seasonActive = rank.getSeasonManager().hasActiveSeason();
        RankTier tier = seasonActive ? rank.getRankManager().getRank(player) : null;
        String activeTitle = rank.getTitleManager().getActive(player);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.displayName(Component.text(player.getName(), NamedTextColor.GOLD));
        meta.lore(List.of(
                Component.text("残高: " + balance + " Cr", NamedTextColor.YELLOW),
                tier != null
                        ? Component.text(tier.displayName)
                        : Component.text("ダンジョンランク: 未参加", NamedTextColor.GRAY),
                activeTitle != null
                        ? Component.text("装備中の称号: " + activeTitle, NamedTextColor.GRAY)
                        : Component.text("装備中の称号: なし", NamedTextColor.GRAY)
        ));
        head.setItemMeta(meta);
        return head;
    }

    /**
     * ステータス表示は別画面を開くのではなく、本アイテムのlore（ホバー時のツールチップ）に
     * HP・攻撃力・防御力を直接記載する。
     */
    private ItemStack buildStatus(Player player) {
        double health = Math.max(0, player.getHealth());
        double maxHealth = 20.0;
        var healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttr != null) maxHealth = healthAttr.getValue();

        PlayerStatManager statManager = itemsStatManager();
        Material held = player.getInventory().getItemInMainHand().getType();
        double baseDamage = WeaponDisplay.getBaseAttackDamage(held);
        double attackBonus = statManager != null ? statManager.getTotalAttackBonus(player.getUniqueId()) : 0.0;
        double totalAttack = baseDamage + attackBonus;

        double armor = 0.0;
        var armorAttr = player.getAttribute(Attribute.GENERIC_ARMOR);
        if (armorAttr != null) armor = armorAttr.getValue();
        double defenseBonus = statManager != null ? statManager.getTotalDefenseBonus(player.getUniqueId()) : 0.0;
        double totalDefense = armor + defenseBonus;

        return makeItem(Material.BOOK, Component.text("ステータス", NamedTextColor.WHITE), List.of(
                Component.text("HP: " + Math.round(health) + " / " + Math.round(maxHealth), NamedTextColor.RED),
                Component.text(String.format("攻撃力: %.1f", totalAttack), NamedTextColor.GOLD),
                Component.text(String.format("防御力: %.1f", totalDefense), NamedTextColor.AQUA)
        ));
    }

    private PlayerStatManager itemsStatManager() {
        Plugin itemsPlugin = Bukkit.getPluginManager().getPlugin("ElyonaItems");
        if (itemsPlugin instanceof ElyonaItemsPlugin items) {
            return items.getPlayerStatManager();
        }
        return null;
    }

    private ItemStack buildEconomy(Player player) {
        long balance = economyCache(player).getBalance(player.getUniqueId());
        return makeItem(Material.EMERALD, Component.text("経済", NamedTextColor.GREEN), List.of(
                Component.text("残高: " + balance + " Cr", NamedTextColor.YELLOW),
                Component.text("クリックで送金方法を確認", NamedTextColor.GRAY)
        ));
    }

    private ItemStack buildDungeonRank(Player player) {
        ElyonaRankPlugin rank = ElyonaRankPlugin.getInstance();
        if (!rank.getSeasonManager().hasActiveSeason()) {
            return makeItem(Material.IRON_INGOT, Component.text("ダンジョンランク", NamedTextColor.WHITE), List.of(
                    Component.text("現在アクティブなシーズンはありません", NamedTextColor.GRAY)
            ));
        }
        RankTier tier = rank.getRankManager().getRank(player);
        return makeItem(tier.iconMaterial, Component.text("ダンジョンランク", NamedTextColor.WHITE), List.of(
                Component.text(tier.displayName),
                Component.text("クリックで詳細を開く", NamedTextColor.GRAY)
        ));
    }

    private ItemStack buildTitle() {
        return makeItem(Material.ENCHANTED_BOOK, Component.text("称号", NamedTextColor.LIGHT_PURPLE), List.of(
                Component.text("クリックで称号一覧を開く", NamedTextColor.GRAY)
        ));
    }

    private ItemStack buildMimicSell() {
        return makeItem(Material.HOPPER, Component.text("MIMIC売却", NamedTextColor.AQUA), List.of(
                Component.text("超技術アイテムをMIMICに売却", NamedTextColor.GRAY),
                Component.text("クリックで開く", NamedTextColor.GRAY)
        ));
    }

    private ItemStack buildLocked(String name) {
        return makeItem(Material.BARRIER, Component.text(name, NamedTextColor.DARK_GRAY), List.of(
                Component.text("「記録にありません」", NamedTextColor.DARK_GRAY),
                Component.text("MIMIC — 未実装のシステム", NamedTextColor.DARK_GRAY)
        ));
    }

    private ItemStack buildClose() {
        return makeItem(Material.BARRIER, Component.text("閉じる", NamedTextColor.RED), null);
    }

    private world.elyona.economy.EconomyCache economyCache(Player player) {
        Plugin economyPlugin = player.getServer().getPluginManager().getPlugin("ElyonaEconomy");
        return ((ElyonaEconomyPlugin) economyPlugin).getEconomyCache();
    }

    private ItemStack makeItem(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        if (lore != null) meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
