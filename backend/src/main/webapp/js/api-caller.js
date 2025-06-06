
function buildUrl(protocol, port, apiUrl, pathParams, queryParams) {
    let url = protocol + "://localhost";
    if (port) url += ":" + port;
    if (apiUrl) url += apiUrl.startsWith("/") ? apiUrl : "/" + apiUrl;
    if (pathParams.length > 0) url += "/" + pathParams.join("/");
    if (queryParams) url += "?" + queryParams;
    return url;
}
function validateInputs(method, url) {
    if (!method || !url.startsWith("http")) {
        throw new Error("不正なメソッドまたはURLです");
    }
}
async function fetchApi(url, method, headers, body) {
    try {
        const response = await fetch(url, {
            method: method,
            headers: headers,
            body: method === "GET" || method === "DELETE" ? null : body
        });

        const text = await response.text();
        document.getElementById("response_status").innerText = "Status Code: " + response.status;
        document.getElementById("response_body").innerText = text;
    } catch (e) {
        document.getElementById("response_status").innerText = "Error";
        document.getElementById("response_body").innerText = e.toString();
    }
}

async function callApi() {
    const method = document.getElementById("method").value.toUpperCase();
    const protocol = document.getElementById("conn_protocol").value;
    const port = document.getElementById("port").value;
    const apiUrl = document.getElementById("api_url").value;
    const body = document.getElementById("body").value;

    const pathParams = Array.from(document.getElementsByClassName("path_param"))
        .map(el => encodeURIComponent(el.value.trim()))
        .filter(v => v);

    const queryParams = Array.from(document.getElementsByClassName("query_param"))
        .map(el => el.value.trim())
        .filter(v => v)
        .join("&");

    const headers = {
        "Content-Type": "application/json"
    };
    Array.from(document.getElementsByClassName("header_param")).forEach(el => {
        const [key, value] = el.value.split("=");
        if (key && value) {
            headers[key.trim()] = value.trim();
        }
    });

    const url = buildUrl(protocol, port, apiUrl, pathParams, queryParams);
    console.log("最終URL:", url);

    try {
        validateInputs(method, url);
        await fetchApi(url, method, headers, body);
    } catch (e) {
        document.getElementById("response_status").innerText = "Validation Error";
        document.getElementById("response_body").innerText = e.toString();
    }
}
