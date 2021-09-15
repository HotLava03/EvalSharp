package io.github.hotlava03.evalsharp;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class EvalTabCompleter implements TabCompleter {
    private static final Map<String, Map<String, Class<?>>> POSSIBLE_VARIABLES = new HashMap<>();
    private final Map<String, Map<String, Class<?>>> lastCallsPerUser = new HashMap<>();

    static {
        POSSIBLE_VARIABLES.put("sender",
                getAllFieldsAndMethods("sender", CommandSender.class)
        );
        POSSIBLE_VARIABLES.put("command",
                getAllFieldsAndMethods("command", Command.class)
        );
        POSSIBLE_VARIABLES.put("label",
                getAllFieldsAndMethods("label", String.class)
        );
        POSSIBLE_VARIABLES.put("args",
                getAllFieldsAndMethods("args", String[].class)
        );
        POSSIBLE_VARIABLES.put("player",
                getAllFieldsAndMethods("player", Player.class)
        );
        POSSIBLE_VARIABLES.put("location",
                getAllFieldsAndMethods("location", Location.class)
        );
        POSSIBLE_VARIABLES.put("world",
                getAllFieldsAndMethods("world", World.class)
        );
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String playerName = "@console";
        if (sender instanceof Player p) playerName = p.getName();

        if (args.length == 0) resetAutocompleteForPlayer(playerName);

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
                System.out.println("IT'S A VARIABLE WE KNOW");

                Map<String, Class<?>> el = POSSIBLE_VARIABLES.getOrDefault(variable, Collections.emptyMap());

                List<Map.Entry<String, Class<?>>> toReturn = el.entrySet().stream()
                        .filter(entry -> {
                            String[] currElements = entry.getKey().split("\\.");
                            return currElements[currElements.length - 1]
                                    .startsWith(elements[elements.length - 1]);
                        }).collect(Collectors.toList());
                if (toReturn.size() == 1) {
                    Map.Entry<String, Class<?>> entry = toReturn.get(0);
                    lastCallsPerUser.put(playerName, getAllFieldsAndMethods(entry.getKey(), entry.getValue()));
                }

                return toReturn.stream().map(Map.Entry::getKey).toList();
            } else if (lastCallsPerUser.containsKey(playerName)) {
                System.out.println("IT'S SOMETHING RANDOM");
                Map<String, Class<?>> el = lastCallsPerUser.get(playerName);

                List<Map.Entry<String, Class<?>>> toReturn = el.entrySet().stream()
                        .filter(entry -> {
                            String[] currElements = entry.getKey().split("\\.");
                            return currElements[currElements.length - 1]
                                    .startsWith(elements[elements.length - 1]);
                        }).collect(Collectors.toList());

                if (toReturn.size() == 1) {
                    Map.Entry<String, Class<?>> entry = toReturn.get(0);
                    lastCallsPerUser.put(playerName, getAllFieldsAndMethods(entry.getKey(), entry.getValue()));
                }

                return toReturn.stream().map(Map.Entry::getKey).toList();
            }
        } else if (lastChar == '.') {
            // Split calls.
            String lastElement = lastArg.substring(0, lastArg.length() - 1);

            Map<String, Class<?>> autocomplete = POSSIBLE_VARIABLES.get(lastElement);
            if (autocomplete == null || lastCallsPerUser.containsKey(playerName)) {
                return lastCallsPerUser.getOrDefault(playerName, Collections.emptyMap()).keySet().stream().toList();
            } else {
                // lastCallsPerUser.put(playerName, autocomplete);
                return autocomplete.keySet().stream().toList();
            }
        }
        resetAutocompleteForPlayer(playerName);
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

    private static Map<String, Class<?>> getAllFieldsAndMethods(String varName, Class<?> clazz) {
        Map<String, Class<?>> fieldsAndMethods = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers()))
                fieldsAndMethods.put(varName + "." + field.getName(), field.getType());
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers()))
                fieldsAndMethods.put(varName + "." + method.getName() + "()", method.getReturnType());
        }
        return fieldsAndMethods;
    }

    public void resetAutocompleteForPlayer(String name) {
        lastCallsPerUser.remove(name);
    }
}
