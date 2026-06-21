const output = document.getElementById("result-output");
const systemInfo = document.getElementById("system-info");
const healthStatus = document.getElementById("health-status");
const useMockData = window.location.protocol === "file:";

const mockSystemInfo = {
    name: "Orchestra API",
    stage: "Hackathon prototype",
    focus: "Transform BPMN, sequence diagrams, and OpenAPI specs into normalized testing metadata",
    capabilities: [
        "BPMN upload and parsing",
        "Sequence diagram upload and parsing",
        "OpenAPI upload and normalization",
        "PostgreSQL persistence",
        "Basic authentication endpoints",
        "Swagger UI and local demo page"
    ],
    sampleFiles: [
        "data_for_tests/openapi.json",
        "data_for_tests/openapi.yml",
        "data_for_tests/01_bonus_payment.puml"
    ]
};

async function fetchJson(url, options = {}) {
    const response = await fetch(url, options);
    const text = await response.text();
    let payload;

    try {
        payload = text ? JSON.parse(text) : null;
    } catch {
        payload = text;
    }

    if (!response.ok) {
        const message = typeof payload === "string" ? payload : JSON.stringify(payload, null, 2);
        throw new Error(message || `Request failed with status ${response.status}`);
    }

    return payload;
}

function showJson(target, value) {
    target.textContent = typeof value === "string" ? value : JSON.stringify(value, null, 2);
}

function showError(error) {
    showJson(output, {
        status: "error",
        message: error.message
    });
}

async function loadHealth() {
    if (useMockData) {
        healthStatus.textContent = "Preview mode";
        healthStatus.classList.remove("offline");
        return;
    }

    try {
        const data = await fetchJson("./health");
        healthStatus.textContent = data?.data || "Service is running";
        healthStatus.classList.remove("offline");
    } catch (error) {
        healthStatus.textContent = "Backend unavailable";
        healthStatus.classList.add("offline");
        showJson(systemInfo, {
            status: "offline",
            message: error.message,
            hint: "Start PostgreSQL and the Spring Boot application to use the live demo."
        });
    }
}

async function loadSystemInfo() {
    if (useMockData) {
        showJson(systemInfo, mockSystemInfo);
        return;
    }

    try {
        const data = await fetchJson("./system/info");
        showJson(systemInfo, data);
    } catch (error) {
        showJson(systemInfo, {
            status: "unavailable",
            message: error.message
        });
    }
}

async function submitUploadForm(event) {
    event.preventDefault();

    const form = event.currentTarget;
    const body = new FormData(form);

    if (useMockData) {
        showJson(output, {
            status: "preview",
            message: "Open the page through the running Spring Boot app to test live uploads.",
            endpoint: form.dataset.endpoint
        });
        return;
    }

    try {
        output.textContent = "Uploading and parsing...";
        const result = await fetchJson(form.dataset.endpoint, {
            method: form.dataset.method || "POST",
            body
        });
        showJson(output, result);
    } catch (error) {
        showError(error);
    }
}

document.querySelectorAll(".upload-form").forEach((form) => {
    form.addEventListener("submit", submitUploadForm);
});

document.getElementById("refresh-status").addEventListener("click", async () => {
    await loadHealth();
    await loadSystemInfo();
});

document.getElementById("clear-output").addEventListener("click", () => {
    output.textContent = "Submit any form to inspect the JSON response here.";
});

loadHealth();
loadSystemInfo();
