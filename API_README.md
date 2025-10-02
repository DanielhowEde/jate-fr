# 🧪 Multi-API Test Framework Guide

This framework lets you write **Cucumber scenarios** that call **multiple APIs** in a consistent way.  
It uses:
- `apis.yaml` for API + environment configuration
- JSON **templates** with placeholders for request bodies
- Built-in support for `${var}`, `{date(...)}`, and conditional `{#if ...}{#/if}` blocks
- Evidence capture (request/response/cURL)
- JSONPath extraction for chaining responses into new requests

---

## 1️⃣ Adding a New API

1. **Register the API in `apis.yaml`:**

   ```yaml
   envs:
     dev:
       MyNewApi:
         baseUrl: https://dev.myapi.example.com
         auth:
           type: api_key           # or oauth2_client_credentials / none
           header: X-API-Key
           value: ${MYAPI_KEY}     # expands from environment variable
         defaultHeaders:
           Accept: application/json
Supported auth types:

none – no auth

api_key – header + value

oauth2_client_credentials – requires tokenUrl, clientId, clientSecret, and optional scope

Secrets are injected via environment variables (${VAR}).

Create a folder for templates:

bash
Copy code
src/test/resources/api/MyNewApi/v1/templates/
Use the API name as declared in apis.yaml (MyNewApi) and the version (v1, v2, etc.).

2️⃣ Creating a New Template
A template is just a JSON file with placeholders.

Example: create-item.json
json
Copy code
{
  "id": "${id}",                           // replaces with var "id"
  "name": "${name:defaultName}",           // uses default if not provided
  "createdAt": "{date(now),yyyy-MM-dd}",   // auto-inserts current date
  {#if betaUser}
  "features": ["core","beta-mode"],        // only included if betaUser=true
  {#/if}
  "expiresOn": "{date(expiry|in=dd/MM/yyyy|out=yyyy-MM-dd)}"
}
Supported placeholders:

${var} → replaced with value from scenario context or DataTable

${var:default} → uses default if not supplied

${{rawJson}} → inserts raw JSON value (object/array) without quotes

{date(now+7d),yyyy-MM-dd} → format current time + 7 days

{date(expiry|in=dd/MM/yyyy|out=yyyy-MM-dd)} → parse a date var with input format, output another format

{#if var}...{#/if} → conditional section, only included if var truthy

{#if eq plan "pro"}...{#else}...{#/if} → equality checks supported (eq, ne, contains)

3️⃣ Using the Template in a Scenario
gherkin
Copy code
Scenario: Create new item in MyNewApi
  Given I use the "MyNewApi" API in "dev" environment
  And I prepare "create-item" template for this API with:
    | _version  | v1         |
    | id        | 12345      |
    | name      | test-item  |
    | expiry    | 31/12/2025 |
    | betaUser  | true       |
  When I POST "/items"
  Then the response status should be 201
  And I capture response fields:
    | itemId | id |
What happens:

Template is loaded from api/MyNewApi/v1/templates/create-item.json

Placeholders are filled with DataTable values + captured vars

Request is sent to the API base URL from apis.yaml

Request/response evidence saved under your report directory

Response field id is captured into var itemId for later steps

4️⃣ Chaining APIs Together
Example: Create a user in Identity API, then create a wallet in Payments API using that user:

gherkin
Copy code
Scenario: Identity + Payments chain
  Given I use the "Identity" API in "dev" environment
  And I prepare "create-user" template for this API with:
    | _version | v2                  |
    | username | mike.auto           |
    | email    | mike.auto@example.com |
  When I POST "/users"
  Then the response status should be 201
  And I capture response fields:
    | userId | id |

  Given I use the "Payments" API in "dev" environment
  And I prepare "open-wallet" template for this API with:
    | _version | v1     |
    | ownerId  | ${userId} |
  When I POST "/wallets"
  Then the response status should be 201
5️⃣ Evidence Storage
Each API call produces evidence under:

pgsql
Copy code
/reportDir/api/<Scenario>/<timestamp>/
   request.json
   request.headers.txt
   request.curl.txt
   response.status.txt
   response.headers.txt
   response.json
6️⃣ Tips
Keep one folder per API/version.

Use _version in your DataTable to choose template version.

For common fields like createdBy, set them in your vars context in a Before hook so they’re available everywhere.

Use ${ENV_VAR} inside apis.yaml to inject secrets securely.

That’s it! 🚀
You can now add new APIs and new templates consistently without touching Java code.

yaml
Copy code

---

Do you want me to tailor this README to match your **current repo layout** (your `jatefr.test.framew