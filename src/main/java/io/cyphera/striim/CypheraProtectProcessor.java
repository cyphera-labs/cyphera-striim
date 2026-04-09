package io.cyphera.striim;

import com.webaction.anno.AdapterType;
import com.webaction.anno.PropertyTemplate;
import com.webaction.anno.PropertyTemplateProperty;
import com.webaction.runtime.components.openprocessor.StriimOpenProcessor;

import io.cyphera.Cyphera;

import java.util.Map;

/**
 * Striim Open Processor: Cyphera Protect
 *
 * Drag-and-drop component for the Flow Designer.
 * Protects (encrypts) specified fields using Cyphera format-preserving encryption.
 *
 * Properties:
 *   policyName - the Cyphera policy to use (e.g. "ssn")
 *   fieldIndex - which field in the event array to protect (0-based)
 *
 * Requires: StriimOpenProcessor-SDK.jar from your Striim installation.
 * See https://www.striim.com/docs/en/using-striim-open-processors.html
 */
@PropertyTemplate(
    name = "CypheraProtect",
    type = AdapterType.process,
    properties = {
        @PropertyTemplateProperty(
            name = "policyName",
            type = String.class,
            required = true,
            defaultValue = "ssn"
        ),
        @PropertyTemplateProperty(
            name = "fieldIndex",
            type = Integer.class,
            required = true,
            defaultValue = "2"
        )
    }
)
public class CypheraProtectProcessor extends StriimOpenProcessor {

    private volatile Cyphera client;
    private volatile String policyName;
    private volatile int fieldIndex = -1;

    private void ensureInitialized() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    Map<String, Object> props = getProperties();
                    this.policyName = props.getOrDefault("policyName", "ssn").toString();
                    this.fieldIndex = Integer.parseInt(props.getOrDefault("fieldIndex", "2").toString());
                    this.client = CypheraLoader.getInstance();
                }
            }
        }
    }

    @Override
    public void run() {
        ensureInitialized();
        for (Object eventObj : getAdded()) {
            try {
                Object[] data = (Object[]) eventObj;
                if (data.length > fieldIndex && data[fieldIndex] != null) {
                    String value = data[fieldIndex].toString();
                    data[fieldIndex] = client.protect(value, policyName);
                }
                send(data);
            } catch (Exception e) {
                send(eventObj);
            }
        }
    }

    @Override
    public void close() {}

    @Override
    public Map getAggVec() { return null; }

    @Override
    public void setAggVec(Map map) {}
}
