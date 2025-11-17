package com.mrrezik.icrafts.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})");
    private static final boolean IS_MODERN = isModernServer();

    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);

        if (IS_MODERN) {
            Matcher matcher = HEX_PATTERN.matcher(coloredMessage);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String hexColor = matcher.group(1);
                String replacement = net.md_5.bungee.api.ChatColor.of("#" + hexColor).toString();
                matcher.appendReplacement(sb, replacement);
            }
            matcher.appendTail(sb);
            coloredMessage = sb.toString();
        }

        return coloredMessage;
    }

    private static boolean isModernServer() {
        try {
            Class.forName("net.md_5.bungee.api.ChatColor");
            String version = Bukkit.getVersion();
            return version.contains("1.16") ||
                    version.contains("1.17") ||
                    version.contains("1.18") ||
                    version.contains("1.19") ||
                    version.contains("1.20") ||
                    version.contains("1.21");
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}