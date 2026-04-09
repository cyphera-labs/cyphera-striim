package io.cyphera.striim;

import com.webaction.anno.AdapterType;
import com.webaction.anno.PropertyTemplate;
import com.webaction.anno.PropertyTemplateProperty;
import com.webaction.runtime.components.openprocessor.StriimOpenProcessor;

import io.cyphera.Cyphera;

import java.util.Map;

/**
 * Striim Open Processor: Cyphera Access
 *
 * Drag-and-drop component for the Flow Designer.
 * Accesses (decrypts) specified fields using the embedded tag — no policy name needed.
 *
 * Properties:
 *   fieldIndex - which field in the event array to access (0-based)
 *
 * Requires: StriimOpenProcessor-SDK.jar from your Striim installation.
 * See https://www.striim.com/docs/en/using-striim-open-processors.html
 */
@PropertyTemplate(
    name = "CypheraAccess",
    type = AdapterType.process,
    properties = {
        @PropertyTemplateProperty(
            name = "fieldIndex",
            type = Integer.class,
            required = true,
            defaultValue = "2"
        )
    }
)
public class CypheraAccessProcessor extends StriimOpenProcessor {

    private volatile Cyphera client;
    private volatile int fieldIndex = -1;

    private void ensureInitialized() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    Map<String, Object> props = getProperties();
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
                    String protectedValue = data[fieldIndex].toString();
                    data[fieldIndex] = client.access(protectedValue);
                }
                send(data);
            } catch (Exception e) {
                send(eventObj);
            }
        }
    }

    @Override
    public void close() { /* no resources to release */ }

    @Override
    public Map getAggVec() { return null; /* stateless processor */ }

    @Override
    public void setAggVec(Map map) { /* stateless processor */ }
}
