# Cyphera Striim Demo

Protect sensitive data in real-time CDC and streaming pipelines using Cyphera format-preserving encryption inside Striim.

## Prerequisites

- Docker and Docker Compose
- Build the Cyphera JAR first: `mvn package -DskipTests`

## Start Striim

```bash
STRIIM_FIRST_NAME=YourName \
STRIIM_LAST_NAME=YourLast \
STRIIM_COMPANY_NAME=YourCompany \
STRIIM_EMAIL=you@company.com \
docker compose up -d
```

Wait ~30 seconds for Striim to start, then open **http://localhost:9080**.

Login: `admin` / `admin`

> **Note**: The eval image is limited to 24 CPUs. The docker-compose sets `cpus: 4` to stay within the limit. If you hit a license error, lower the CPU count.

## Import the Demo

In the Striim console (click the terminal icon), paste:

```sql
CREATE APPLICATION CypheraDemo;

CREATE SOURCE CsvSource USING FileReader (
  directory: '/opt/striim/data',
  wildcard: 'demo-data.csv',
  positionByEOF: false
)
PARSE USING DSVParser (
  header: true,
  columndelimiter: ','
)
OUTPUT TO RawStream;

CREATE CQ ProtectFields
  INSERT INTO ProtectedStream
  SELECT
    data[0],
    data[1],
    io.cyphera.striim.CypheraFunctions.cyphera_protect('ssn', TO_STRING(data[2])),
    data[3]
  FROM RawStream;

CREATE TARGET FileOutput USING FileWriter (
  directory: '/opt/striim/data/output',
  filename: 'protected-data.csv'
)
FORMAT USING DSVFormatter (
  separator: ','
)
INPUT FROM ProtectedStream;

END APPLICATION CypheraDemo;
DEPLOY APPLICATION CypheraDemo;
START APPLICATION CypheraDemo;
```

## Verify Output

```bash
docker exec cyphera-striim-striim-1 cat /opt/striim/data/output/protected-data.00.csv
```

### Input (demo-data.csv)

```
id,name,ssn,email
1,Alice Johnson,123-45-6789,alice@example.com
2,Bob Smith,987-65-4321,bob@example.com
3,Carol Davis,555-12-3456,carol@example.com
4,Dave Wilson,111-22-3333,dave@example.com
5,Eve Brown,444-55-6666,eve@example.com
```

### Output (protected-data.00.csv)

```
1,Alice Johnson,T01i6J-xF-07pX,alice@example.com
2,Bob Smith,T01Q1I-cH-Sdcb,bob@example.com
3,Carol Davis,T01b54-Un-4zHt,carol@example.com
4,Dave Wilson,T01uAB-U2-IIQP,dave@example.com
5,Eve Brown,T01o1g-zO-gqkU,eve@example.com
```

SSNs are protected with format-preserving encryption. Dashes preserved. Tags embedded (`T01`). Names and emails pass through untouched.

Alice's SSN `123-45-6789` → `T01i6J-xF-07pX` matches the cross-language vector across all 7 Cyphera SDKs.

## What's Happening

```
demo-data.csv → FileReader → DSVParser → RawStream
  → ProtectFields CQ (calls cyphera_protect on SSN column)
  → ProtectedStream → DSVFormatter → FileWriter → protected-data.csv
```

The `cyphera_protect('ssn', ...)` function:
1. Looks up the `ssn` policy in `/etc/cyphera/cyphera.json`
2. Encrypts the value using FF1 format-preserving encryption
3. Prepends the tag (`T01`) so `cyphera_access` can decrypt without a policy name
4. Preserves dashes in their original positions

## Policy (config/cyphera.json)

```json
{
  "policies": {
    "ssn": { "engine": "ff1", "key_ref": "demo-key", "tag": "T01" },
    "credit_card": { "engine": "ff1", "key_ref": "demo-key", "tag": "T02" },
    "name": { "engine": "ff1", "alphabet": "alpha_lower", "key_ref": "demo-key", "tag": "T03" }
  },
  "keys": {
    "demo-key": { "material": "2B7E151628AED2A6ABF7158809CF4F3C" }
  }
}
```

## Cleanup

```bash
docker compose down
```
