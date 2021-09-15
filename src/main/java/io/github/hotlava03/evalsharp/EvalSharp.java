package io.github.hotlava03.evalsharp;

import org.bukkit.plugin.java.JavaPlugin;

public final class EvalSharp extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getCommand("eval").setTabCompleter(new EvalTabCompleter());
        this.getCommand("eval").setExecutor(new EvalCommand(this));
    }
}
