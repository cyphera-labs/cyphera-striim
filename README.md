# cyphera-striim

[![CI](https://github.com/cyphera-labs/cyphera-striim/actions/workflows/ci.yml/badge.svg)](https://github.com/cyphera-labs/cyphera-striim/actions/workflows/ci.yml)
[![Security](https://github.com/cyphera-labs/cyphera-striim/actions/workflows/codeql.yml/badge.svg)](https://github.com/cyphera-labs/cyphera-striim/actions/workflows/codeql.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](LICENSE)

Format-preserving encryption for [Striim](https://www.striim.com/) — protect sensitive data in real-time CDC and streaming pipelines.

Built on [`io.cyphera:cyphera`](https://central.sonatype.com/artifact/io.cyphera/cyphera) from Maven Central.

## Two Integration Options

### 1. Custom Functions (UDF)

Call `cyphera_protect` and `cyphera_access` directly in CQ queries. No Striim SDK needed.

```sql
IMPORT STATIC io.cyphera.striim.CypheraFunctions.*;

CREATE CQ ProtectFields
  INSERT INTO ProtectedStream
  SELECT
    data[0],
    cyphera_protect('ssn', TO_STRING(data[1])),
    data[2]
  FROM RawStream;
```

### 2. Open Processors (Flow Designer)

Visual drag-and-drop components for the Striim Flow Designer. Requires the Striim Open Processor SDK.

| Processor | Properties | Description |
|-----------|-----------|-------------|
| **CypheraProtect** | `policyName`, `fieldIndex` | Protects a field using a named policy |
| **CypheraAccess** | `fieldIndex` | Accesses a field using the embedded tag |

#### Using Open Processors in Flow Designer

1. Load the processors in the Striim console:
   ```sql
   LOAD OPEN PROCESSOR "lib/cyphera-striim-0.1.0.jar";
   ```
2. Go to **Apps** → **Create App** → choose a template or blank canvas
3. Add a source (FileReader, DatabaseReader, etc.)
4. Click **+** to add a component → select **Open Processor**
5. In the **Adapter** dropdown, select **CypheraProtect** or **CypheraAccess**
6. Configure properties:
   - **CypheraProtect**: set `policyName` (e.g. `ssn`) and `fieldIndex` (0-based index of the field to protect)
   - **CypheraAccess**: set `fieldIndex` (the field to access — tag tells Cyphera which policy to use)
7. Connect to an output stream and target
8. Deploy and start the application

> **Note**: Open Processors appear under the generic "Open Processor" icon in the Flow Designer — select the Cyphera adapter from the dropdown after placing the component.

## Build

### UDF only (no Striim SDK needed)

```bash
mvn package -DskipTests
```

### With Open Processors (requires Striim SDK)

```bash
# 1. Start Striim to extract the SDK
docker compose up -d
# Wait ~30s for Striim to start

# 2. Extract the SDK JAR from the running container
bash setup-sdk.sh

# 3. Build
mvn package -DskipTests

# OR build via Docker
docker build -t cyphera-striim .
```

> **Note**: The Open Processor SDK is specific to your Striim version. If you upgrade Striim, re-run `setup-sdk.sh` and rebuild. See [Striim Open Processor docs](https://www.striim.com/docs/en/using-striim-open-processors.html).

## Deploy

Copy the fat JAR to `$STRIIM_HOME/lib/`:

```bash
cp target/cyphera-striim-0.1.0.jar $STRIIM_HOME/lib/
```

For UDFs, load in the console:
```sql
LOAD "lib/cyphera-striim-0.1.0.jar";
```

For Open Processors:
```sql
LOAD OPEN PROCESSOR "lib/cyphera-striim-0.1.0.jar";
```

## Quick Start (Docker)

```bash
STRIIM_FIRST_NAME=YourName \
STRIIM_LAST_NAME=YourLast \
STRIIM_COMPANY_NAME=YourCompany \
STRIIM_EMAIL=you@company.com \
docker compose up -d
```

Wait ~30s, open **http://localhost:9080**, login `admin` / `admin`.

See [DEMO.md](DEMO.md) for a complete working pipeline with input/output.

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

Override with `CYPHERA_POLICY_FILE` env var or `-Dcyphera.policy.file` system property.

## Future / Nice to Have

- **Custom icon in Flow Designer** — register as a full Striim adapter with a branded icon so CypheraProtect/CypheraAccess appear as their own components, not under the generic Open Processor
- **Multi-field support** — protect/access multiple fields in a single processor (comma-separated field indices or field name mapping)
- **Policy auto-discovery** — detect field types and suggest policies automatically
- **Striim Marketplace listing** — publish as an official Striim partner integration
- **Striim App Template** — pre-built CDC-to-encrypted-target pipeline template users can import
- **Deeper Striim integration** — explore Striim's custom adapter API (`PropertyTemplate` with `AdapterType.source` / `AdapterType.target`) for tighter UI integration beyond Open Processor

Contributions and feedback welcome — see [CONTRIBUTING.md](https://github.com/cyphera-labs/.github/blob/main/CONTRIBUTING.md).

## License

Apache 2.0 — Copyright 2026 Horizon Digital Engineering LLC
