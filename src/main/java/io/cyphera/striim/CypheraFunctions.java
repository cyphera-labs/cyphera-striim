package io.cyphera.striim;

import io.cyphera.Cyphera;

/**
 * Cyphera custom functions for Striim CQ pipelines.
 *
 * Usage in TQL:
 *   IMPORT STATIC io.cyphera.striim.CypheraFunctions.*;
 *
 *   CREATE CQ ProtectFields
 *     INSERT INTO ProtectedStream
 *     SELECT
 *       data[0],
 *       cyphera_protect('ssn', data[1]),
 *       cyphera_protect('credit_card', data[2]),
 *       data[3]
 *     FROM IncomingStream;
 *
 * Deploy: copy cyphera-striim-0.1.0.jar to $STRIIM_HOME/lib/
 * Load:   LOAD "lib/cyphera-striim-0.1.0.jar";
 */
public abstract class CypheraFunctions {

    private static final Cyphera CLIENT = CypheraLoader.getInstance();

    /**
     * Protect a value using a named configuration.
     * Output is header-prefixed — cyphera_access needs no configuration name.
     */
    public static String cyphera_protect(String configurationName, String value) {
        if (value == null) return null;
        try {
            return CLIENT.protect(value, configurationName);
        } catch (Exception e) {
            return "[error: " + e.getMessage() + "]";
        }
    }

    /**
     * Access (decrypt) a protected value using the embedded header.
     * No configuration name needed — the header identifies which configuration to use.
     */
    public static String cyphera_access(String protectedValue) {
        if (protectedValue == null) return null;
        try {
            return CLIENT.access(protectedValue);
        } catch (Exception e) {
            return "[error: " + e.getMessage() + "]";
        }
    }

    /**
     * Access (decrypt) a protected value with an explicit configuration name.
     * Escape hatch for headerless configurations.
     */
    public static String cyphera_access(String configurationName, String protectedValue) {
        if (protectedValue == null) return null;
        try {
            return CLIENT.access(protectedValue, configurationName);
        } catch (Exception e) {
            return "[error: " + e.getMessage() + "]";
        }
    }
}
