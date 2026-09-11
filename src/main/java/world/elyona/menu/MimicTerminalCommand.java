package world.elyona.menu;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MimicTerminalCommand implements CommandExecutor {

    private final Plugin plugin;

    public MimicTerminalCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ使用できます。");
            return true;
        }
        player.getInventory().addItem(MimicTerminalItem.create(plugin));
        player.sendMessage("MIMIC端末を入手しました。");
        return true;
    }
}
