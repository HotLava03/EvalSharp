package io.github.hotlava03.evalsharp;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class EvalTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Get the last argument.
        String lastArg = args.length != 0 ? args[args.length - 1] : "";
        // Get the last character.
        char lastChar = lastArg.isEmpty() ? ' ' : lastArg.charAt(lastArg.length() - 1);
        // If the user is typing either a '.' or a space, show suggestions. Otherwise, return.
        if (lastChar != '.' && lastChar != ' ') return Collections.emptyList();
        if (lastChar == '.') {
            String variable = lastArg.replace(".", "");
            switch (variable.toLowerCase()) {
                case "sender":
                    return getAllFieldsAndMethods("sender", CommandSender.class);
                case "command":
                    return getAllFieldsAndMethods("command", Command.class);
                case "label":
                    return getAllFieldsAndMethods("label", String.class);
                case "args":
                    return Collections.singletonList("args.length"); // Array moment.
                case "player":
                    return getAllFieldsAndMethods("player", Player.class);
                case "location":
                    return getAllFieldsAndMethods("location", Location.class);
                case "world":
                    return getAllFieldsAndMethods("world", World.class);
            }
        }
        // Default autocomplete.
        List<String> autocomplete = new ArrayList<>(Arrays.asList(
                "sender",
                "command",
                "label",
                "args",
                "plugin",
                "this"
        ));
        // If it's a player.
        if (sender instanceof Player) {
            autocomplete.addAll(Arrays.asList(
                    "player",
                    "location",
                    "world"
            ));
        }
        return autocomplete;
    }

    private List<String> getAllFieldsAndMethods(String varName, Class<?> clazz) {
        List<String> fieldsAndMethods = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers())) fieldsAndMethods.add(varName + "." + field.getName());
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) fieldsAndMethods.add(varName + "." + method.getName() + "()");
        }
        return fieldsAndMethods;
    }
}
