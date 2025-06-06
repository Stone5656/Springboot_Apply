function createApiConfigUI(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    container.innerHTML = ""; // 既存の中身を消去

    // プロトコル選択
    const protocolLabel = createLabel("プロトコル");
    const protocolSelect = createSelecter("conn_protocol", ["http", "https"]);

    // ポート入力
    const portLabel = createLabel("ポート");
    const portInput = createInput("port", "number");

    // API URL 入力
    const urlLabel = createLabel("API URL");
    const urlInput = createInput("api_url", "url");

    // メソッド選択
    const methodLabel = createLabel("メソッド")
    const methodSelect = createSelecter("method", ["GET", "POST", "PUT", "DELETE"]);

    // 初期値入力
    portInput.value = "9000";
    urlInput.value = "/api/hello";

    // 要素を配置（ラベル＋入力）
    appendContainerElement(container, protocolLabel, protocolSelect);
    appendContainerElement(container, portLabel, portInput);
    appendContainerElement(container, urlLabel, urlInput);
    appendContainerElement(container, methodLabel, methodSelect);
}

function createLabel(labelText) {
    const label = document.createElement("label");
    label.textContent = labelText + ": ";

    return label
}

function createSelecter(selecterId, optionList) {
    const selecter = document.createElement("select");
    selecter.id = selecterId;
    optionList.forEach(p => {
        const option = document.createElement("option");
        option.value = p;
        option.textContent = p;
        selecter.appendChild(option);
    });

    return selecter
}

function createInput(inputId, inputType) {
    const input = document.createElement("input");
    input.id = inputId;
    input.type = inputType;

    return input
}

function appendContainerElement(container, labelElement, labelTarget) {
    container.appendChild(labelElement);
    container.appendChild(labelTarget);
    container.appendChild(document.createElement("br"));
}
