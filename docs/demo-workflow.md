# Demo workflow

## Local startup

1. Start PostgreSQL:

```bash
docker compose up -d
```

2. Run the backend:

```bash
./mvnw spring-boot:run
```

3. Open:

- `http://localhost:8080/api/`
- `http://localhost:8080/api/swagger-ui/index.html`

## Suggested manual flow

1. Open the demo page and verify that `/health` and `/system/info` respond.
2. Upload `data_for_tests/openapi.json` to inspect OpenAPI normalization.
3. Upload `data_for_tests/01_bonus_payment.puml` to inspect sequence parsing.
4. Upload a BPMN file from your local test set to inspect transition mapping.

## Example curl requests

### OpenAPI upload

```bash
curl -X POST "http://localhost:8080/api/openapi/upload" \
  -F "file=@data_for_tests/openapi.json" \
  -F "name=bank-api"
```

### Sequence upload

```bash
curl -X POST "http://localhost:8080/api/sequence/upload" \
  -F "file=@data_for_tests/01_bonus_payment.puml" \
  -F "name=bonus-payment-flow" \
  -F "format=PLANTUML"
```

### Health check

```bash
curl "http://localhost:8080/api/health"
```
