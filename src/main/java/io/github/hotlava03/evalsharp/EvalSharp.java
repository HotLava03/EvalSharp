package io.github.hotlava03.evalsharp;

import org.bukkit.plugin.java.JavaPlugin;

public final class EvalSharp extends JavaPlugin {
    private EvalTabCompleter tabCompleter;

    @Override
    public void onEnable() {
        tabCompleter = new EvalTabCompleter();
        this.getCommand("eval").setTabCompleter(tabCompleter);
        this.getCommand("eval").setExecutor(new EvalCommand(this));
    }

    public EvalTabCompleter getTabCompleter() {
        return tabCompleter;
    }
}
