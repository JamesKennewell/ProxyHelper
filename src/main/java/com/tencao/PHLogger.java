package com.tencao;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PHLogger {

    static Logger logger = LogManager.getLogger(PHCore.MODID);

    static void log(Level level, String msg)
    {
        logger.log(level, msg);
    }

    static void logInfo(String msg)
    {
        logger.info(msg);
    }

    static void logWarn(String msg)
    {
        logger.warn(msg);
    }

    static void logFatal(String msg)
    {
        logger.fatal(msg);
    }

    static void logDebug(String msg)
    {
        logger.debug(msg); // fml log
    }
}
