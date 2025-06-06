<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>

    <head>
        <title>API テスト</title>
        <script src="/js/api-caller.js"></script>
        <script src="/js/create-api-container.js"></script>
    </head>

    <body>
        <div id="api_config_container"></div>
        <script>
          window.onload = () => {
            createApiConfigUI("api_config_container");
          };
        </script>
        <textarea id="body">{}</textarea>

        <!-- path param 用 input -->
        <input class="path_param" type="text" placeholder="path param (例: userId)" />

        <!-- query param 用 input -->
        <input class="query_param" type="text" placeholder="key=value" />

        <!-- header param 用 input -->
        <input class="header_param" type="text" placeholder="Authorization=Bearer xyz" />
        <button onclick="callApi()">API 実行</button>

        <h3 id="response_status"></h3>
        <pre id="response_body"></pre>
    </body>
</html>
