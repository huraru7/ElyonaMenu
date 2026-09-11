package world.elyona.menu;

import org.bukkit.plugin.java.JavaPlugin;

public class ElyonaMenuPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new TerminalListener(this), this);

        getCommand("menu").setExecutor(new MenuCommand());
        getCommand("mimicterminal").setExecutor(new MimicTerminalCommand(this));

        getLogger().info("ElyonaMenu が有効化されました。");
    }
}
