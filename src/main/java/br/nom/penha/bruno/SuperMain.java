package br.nom.penha.bruno;

import br.nom.penha.bruno.kubeson.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SuperMain {

    private static final Logger logger = LogManager.getLogger(SuperMain.class);

    public static void main(String[] args) {
        logger.error("Running app with Java Version " + System.getProperty("java.version") + " Arch " + System.getProperty("sun.arch.data.model"));

        logger.debug("My Debug Log");
        logger.info("My Info Log");
        logger.warn("My Warn Log");
        logger.error("My error log");
        logger.fatal("My fatal log");

        Main.main(args);
    }


}
