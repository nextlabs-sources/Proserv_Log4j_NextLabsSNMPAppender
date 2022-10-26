package org.apache.log4j.ext;

import org.apache.log4j.Category;
import org.apache.log4j.LogManager;
import org.apache.log4j.helpers.LogLog;

class SnmpTrapAppenderTest {

    private static final Category trace = Category.getInstance(SnmpTrapAppenderTest.class.getName());
    private static boolean log4jInitialized = false;

    private SnmpTrapAppenderTest() {
        final String sFile = "/etc/snmpTrapAppenderTestConfig.xml";
        initLogging(sFile);
        if (log4jInitialized) {
            trace.debug("Here's a debug message");
            trace.error("Whoa, had an error there");
            trace.info("Here's some info about that");
            trace.fatal("Help! I'm dying out here!");
            trace.fatal("Here's an Exception!", new Exception("Exception message"));
        } else LogLog.error(new StringBuffer().append(
                "[SnmpTrapAppenderTest],  Logging initialization error.  Config file=")
                .append(sFile)
                .append(" -- Aborting.").toString());
    }

    public static void initLogging(final String configFile) {
        if (!log4jInitialized) {
            try {
                org.apache.log4j.xml.DOMConfigurator.configure(configFile);
                log4jInitialized = true;
            } catch (Exception ex) {
                LogLog.error("[SnmpTrapAppenderTest], [initLogging], Error initializing logging!", ex);
            }
        }//if
    }

    public static void main(final String[] args) {
        new SnmpTrapAppenderTest();
        LogManager.shutdown();
    }
}
