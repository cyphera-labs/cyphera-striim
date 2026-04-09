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
     * Protect a value using a named policy.
     * Output is tagged — cyphera_access needs no policy name.
     */
    public static String cyphera_protect(String policyName, String value) {
        if (value == null) return null;
        try {
            return CLIENT.protect(value, policyName);
        } catch (Exception e) {
            return "[error: " + e.getMessage() + "]";
        }
    }

    /**
     * Access (decrypt) a protected value using the embedded tag.
     * No policy name needed — the tag identifies the policy.
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
     * Access (decrypt) a protected value with an explicit policy name.
     * Use this for untagged values where tag_enabled=false.
     */
    public static String cyphera_access(String policyName, String protectedValue) {
        if (protectedValue == null) return null;
        try {
            return CLIENT.access(protectedValue, policyName);
        } catch (Exception e) {
            return "[error: " + e.getMessage() + "]";
        }
    }
}
