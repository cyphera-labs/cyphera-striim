# cyphera-striim

[![CI](https://github.com/cyphera-labs/cyphera-striim/actions/workflows/ci.yml/badge.svg)](https://github.com/cyphera-labs/cyphera-striim/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](LICENSE)

Format-preserving encryption for [Striim](https://www.striim.com/) — custom Java functions for real-time data protection in CDC and streaming pipelines.

Built on [`io.cyphera:cyphera`](https://central.sonatype.com/artifact/io.cyphera/cyphera) from Maven Central.

## Build

```bash
mvn package -DskipTests
```

Produces `target/cyphera-striim-0.1.0.jar` (fat JAR with all dependencies).

## Deploy

1. Copy the JAR to `$STRIIM_HOME/lib/`
2. In the Striim console:

```sql
LOAD "lib/cyphera-striim-0.1.0.jar";
```

## Usage

```sql
IMPORT STATIC io.cyphera.striim.CypheraFunctions.*;

CREATE APPLICATION ProtectPipeline;

-- Protect sensitive fields in a CDC stream
CREATE CQ ProtectFields
  INSERT INTO ProtectedStream
  SELECT
    data[0],                                    -- id (passthrough)
    cyphera_protect('ssn', data[1]),            -- protect SSN
    cyphera_protect('credit_card', data[2]),    -- protect credit card
    data[3]                                     -- other field (passthrough)
  FROM IncomingStream;

-- Access (decrypt) — tag tells Cyphera which policy to use
CREATE CQ AccessFields
  INSERT INTO ClearStream
  SELECT
    data[0],
    cyphera_access(data[1]),
    cyphera_access(data[2]),
    data[3]
  FROM ProtectedStream;

END APPLICATION ProtectPipeline;
```

## Policy File

Mount `cyphera.json` to `/etc/cyphera/cyphera.json`:

```json
{
  "policies": {
    "ssn": { "engine": "ff1", "key_ref": "demo-key", "tag": "T01" },
    "credit_card": { "engine": "ff1", "key_ref": "demo-key", "tag": "T02" }
  },
  "keys": {
    "demo-key": { "material": "2B7E151628AED2A6ABF7158809CF4F3C" }
  }
}
```

Override the path with `CYPHERA_POLICY_FILE` env var or `-Dcyphera.policy.file` system property.

## Docker (Striim Eval)

```bash
docker run -d --name striim \
  -p 9080:9080 \
  -v $(pwd)/target/cyphera-striim-0.1.0.jar:/opt/striim/lib/cyphera-striim-0.1.0.jar \
  -v $(pwd)/config/cyphera.json:/etc/cyphera/cyphera.json \
  -e CYPHERA_POLICY_FILE=/etc/cyphera/cyphera.json \
  striim/evalversion:latest
```

Then open `http://localhost:9080` and load the JAR:
```sql
LOAD "lib/cyphera-striim-0.1.0.jar";
```

## License

Apache 2.0 — Copyright 2026 Horizon Digital Engineering LLC
