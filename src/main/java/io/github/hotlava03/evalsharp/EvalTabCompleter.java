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
import java.util.stream.Collectors;

public class EvalTabCompleter implements TabCompleter {
    private static final Map<String, List<String>> POSSIBLE_VARIABLES = new HashMap<>();
    private final Map<UUID, Class<?>> lastMethodsPerUser = new HashMap<>();

    static {
        POSSIBLE_VARIABLES.put("sender", getAllFieldsAndMethods("sender", CommandSender.class));
        POSSIBLE_VARIABLES.put("command", getAllFieldsAndMethods("command", Command.class));
        POSSIBLE_VARIABLES.put("label", getAllFieldsAndMethods("label", String.class));
        POSSIBLE_VARIABLES.put("args", Collections.singletonList("args.length")); // Array moment.
        POSSIBLE_VARIABLES.put("player", getAllFieldsAndMethods("player", Player.class));
        POSSIBLE_VARIABLES.put("location", getAllFieldsAndMethods("location", Location.class));
        POSSIBLE_VARIABLES.put("world", getAllFieldsAndMethods("world", World.class));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Get the last argument.
        String lastArg = args.length != 0 ? args[args.length - 1] : "";
        // Get the last character.
        char lastChar = lastArg.isEmpty() ? ' ' : lastArg.charAt(lastArg.length() - 1);
        if (lastChar != '.' && lastChar != ' ' && lastArg.contains(".")) {
            // Split calls.
            String[] elements = lastArg.split("\\.");
            // Get the variable.
            String variable = elements[elements.length - 2];

            if (POSSIBLE_VARIABLES.containsKey(variable)) {
                System.out.println(elements[elements.length - 1]);

                return POSSIBLE_VARIABLES.getOrDefault(variable, Collections.emptyList()).stream()
                        .filter(str -> {
                            String[] currElements = str.split("\\.");
                            return currElements[currElements.length - 1]
                                    .startsWith(elements[elements.length - 1]);
                        }).collect(Collectors.toList());
            }
        } else if (lastChar == '.') {
            // Split calls.
            String lastElement = lastArg.substring(0, lastArg.length() - 1);
            return POSSIBLE_VARIABLES.get(lastElement);
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

    private static List<String> getAllFieldsAndMethods(String varName, Class<?> clazz) {
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
