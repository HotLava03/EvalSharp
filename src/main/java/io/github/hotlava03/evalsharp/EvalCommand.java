package io.github.hotlava03.evalsharp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;

import javax.script.ScriptEngine;
import java.io.PrintWriter;
import java.io.StringWriter;

public class EvalCommand implements CommandExecutor {
    private final EvalSharp plugin;
    // Default imports for the eval.
    private static final String[] DEFAULT_IMPORTS = {
            "org.bukkit",
            "org.bukkit.entity",
            "org.bukkit.configuration",
            "org.bukkit.plugin",
            "org.bukkit.plugin.java",
            "org.bukkit.command",
            "java.lang",
            "java.io",
            "java.math",
            "java.eval",
            "java.eval.concurrent",
            "java.time",
            "java.awt"
    };

    public EvalCommand(EvalSharp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the user has permissions for this.
        if (!sender.hasPermission("evalsharp.eval")) {
            sender.sendMessage("\u00a7cEval# \u00a78\u00a78l\u00BB \u00a77Nope, you can't do this. Learn java first silly.");
            return true;
        }

        // Require code in order to eval.
        if (args.length == 0) {
            sender.sendMessage("\u00a79Eval# \u00a78\u00a7l\u00BB \u00a77Usage: /eval \u00a7f<code>\u00a77.");
            return true;
        }

        // Get help.
        if (args[0].equalsIgnoreCase("-help")) {
            sender.sendMessage("\u00a79Eval# \u00a78\u00a7l\u00BB \u00a77Options: help, js/javascript, sync\n" +
                    "\u00a76Eval# \u00a78\u00a7l\u00BB \u00a77Notice: Sync is to be used" +
                    " \u00a7conly \u00a77when something doesn't work properly on \u00a7aasync (default)\u00a77! This may freeze your server.");
            return true;
        }

        // Instantiate the Groovy Script Engine.
        ScriptEngine engine = new GroovyScriptEngineImpl();

        // Add variables with external values to the eval runtime.
        if (sender instanceof Player) {
            // Additional shortcuts if the one running this is a player.
            Player player = (Player) sender;
            engine.put("player", player);
            engine.put("location", player.getLocation());
            engine.put("world", player.getWorld());
        }
        // Console or not, these can be set anyway.
        engine.put("sender", sender);
        engine.put("command", command);
        engine.put("label", label);
        engine.put("args", args);
        engine.put("plugin", plugin);

        // Set output/error writers.
        StringWriter out = new StringWriter();
        PrintWriter outWriter = new PrintWriter(out);
        engine.getContext().setWriter(outWriter);
        StringWriter error = new StringWriter();
        PrintWriter errorWriter = new PrintWriter(error);
        engine.getContext().setErrorWriter(errorWriter);

        // Format default imports.
        StringBuilder sb = new StringBuilder();
        for (String packageImport : DEFAULT_IMPORTS) {
            sb.append("import ").append(packageImport).append(".*;");
        }

        // Final code to eval.
        String toEval = sb + String.join(" ", args);

        // Eval.
        Object res = null;
        try {
            res = engine.eval(toEval);
        } catch (Throwable e) {
            errorWriter.println(e.toString());
        }

        // Message to send to the player.
        String message = "";

        // Distinguish result from output from errors.
        if (res != null) message += "\u00a7aResult \u00a78\u00BB \u00a77" + res.toString() + "\n";
        if (!out.toString().isEmpty()) message += "\u00a79Output \u00a78\u00BB \u00a77" + out.toString() + "\n";
        if (!error.toString().isEmpty()) message += "\u00a7cError \u00a78\u00BB \u00a77" + error.toString() + "\n";

        // Send the result as message.
        sender.sendMessage(message);
        return true;
    }
}
