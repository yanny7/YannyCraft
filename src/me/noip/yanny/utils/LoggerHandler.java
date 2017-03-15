package me.noip.yanny.utils;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public class LoggerHandler {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[1;31m";
    private static final String ANSI_GREEN = "\u001B[1;32m";

    private Logger logger;
    private StringBuilder strBuilder = new StringBuilder();

    public LoggerHandler(Plugin plugin) {
        this.logger = plugin.getLogger();
    }

    public void logInfo(Class clazz, String msg) {
        strBuilder.setLength(0);
        strBuilder.append(ANSI_GREEN).append('[').append(clazz.getSimpleName()).append("] ").append(ANSI_RESET).append(msg);
        logger.info(strBuilder.toString());
    }

    public void logWarn(Class clazz, String msg) {
        strBuilder.setLength(0);
        strBuilder.append(ANSI_RED).append('[').append(clazz.getSimpleName()).append("] ").append(ANSI_RESET).append(msg);
        logger.warning(strBuilder.toString());
    }
}
