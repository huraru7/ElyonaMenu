package world.elyona.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;
import world.elyona.core.ElyonaCorePlugin;
import world.elyona.core.mimic.MimicMessenger;
import world.elyona.core.rank.RankTier;
import world.elyona.items.ElyonaItemsPlugin;
import world.elyona.rank.ElyonaRankPlugin;
import world.elyona.rank.rank.RankGui;
import world.elyona.rank.title.TitleGui;

public class MenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuGui)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ElyonaCorePlugin core = ElyonaCorePlugin.getInstance();
        ElyonaRankPlugin rank = ElyonaRankPlugin.getInstance();
        MimicMessenger mimic = core.getMimicMessenger();

        switch (event.getRawSlot()) {
            case MenuGui.SLOT_ECONOMY -> {
                player.closeInventory();
                mimic.sendTo(player, "/pay <プレイヤー名> <金額> で送金できます。");
            }
            case MenuGui.SLOT_DUNGEON_RANK -> {
                if (!rank.getSeasonManager().hasActiveSeason()) {
                    mimic.sendTo(player, "現在アクティブなシーズンはありません。");
                    return;
                }
                RankTier tier = rank.getRankManager().getRank(player);
                long exp = rank.getRankManager().getExp(player);
                player.closeInventory();
                new RankGui(player, tier, exp).open(player);
            }
            case MenuGui.SLOT_TITLE -> {
                player.closeInventory();
                rank.getTitleManager().getOwnedTitles(player).thenAccept(ownedIds -> {
                    String activeId = rank.getTitleManager().getActive(player);
                    Bukkit.getScheduler().runTask(rank, () ->
                            new TitleGui(player, ownedIds, rank.getTitleLoader(), activeId).open());
                });
            }
            case MenuGui.SLOT_MIMIC_SELL -> {
                player.closeInventory();
                Plugin itemsPlugin = Bukkit.getPluginManager().getPlugin("ElyonaItems");
                if (itemsPlugin instanceof ElyonaItemsPlugin items) {
                    items.getMimicSellGui().open(player);
                } else {
                    mimic.sendTo(player, "ElyonaItemsが見つかりません。");
                }
            }
            case MenuGui.SLOT_RESIDENT_RANK, MenuGui.SLOT_SERATH_RANK, MenuGui.SLOT_LAND_INFO ->
                    mimic.sendTo(player, "記録にありません。");
            case MenuGui.SLOT_CLOSE -> player.closeInventory();
            default -> { /* 装飾・未使用スロットは無視 */ }
        }
    }
}
