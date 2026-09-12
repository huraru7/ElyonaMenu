package world.elyona.menu;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class MimicTerminalCommand implements CommandExecutor, TabCompleter {

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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of(); // 引数を取らないコマンドのため常に空
    }
}
