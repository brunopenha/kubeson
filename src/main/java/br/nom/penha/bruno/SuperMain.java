package br.nom.penha.bruno;

import br.nom.penha.bruno.kubeson.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class SuperMain {

    private static final Logger logger = LogManager.getLogger(SuperMain.class);

    public static void main(String[] args) {
        logger.debug("My Debug Log");
        logger.info("My Info Log");
        logger.warn("My Warn Log");
        logger.error("My error log");
        logger.fatal("My fatal log");

        Main.main(args);
    }


}
