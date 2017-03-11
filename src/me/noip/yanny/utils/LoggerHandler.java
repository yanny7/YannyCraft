package me.noip.yanny.utils;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public class LoggerHandler {

    private Logger logger;
    private StringBuilder strBuilder = new StringBuilder();

    public LoggerHandler(Plugin plugin) {
        this.logger = plugin.getLogger();
    }

    public void logInfo(Class clazz, String msg) {
        strBuilder.setLength(0);
        strBuilder.append('[').append(clazz.getSimpleName()).append("] ").append(msg);
        logger.info(strBuilder.toString());
    }

    public void logWarn(Class clazz, String msg) {
        strBuilder.setLength(0);
        strBuilder.append('[').append(clazz.getSimpleName()).append("] ").append(msg);
        logger.warning(strBuilder.toString());
    }
}
