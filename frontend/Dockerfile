# Base image（安定したDebianベース + Node LTS）
FROM node:18-bullseye

# ユーザー定義
ARG USERNAME
ARG USER_UID
ARG USER_GID

# 基本ツールのインストール + 任意パッケージ追加
RUN apt-get update && apt-get install -y \
    sudo curl unzip vim bash \
    && rm -rf /var/lib/apt/lists/*

# ユーザー作成とsudo権限付与（UID/GIDの存在チェック＆再利用対応）
RUN mkdir -p /etc/sudoers.d && \
    # GIDが存在しない場合のみグループを追加
    if ! getent group "${USER_GID}" > /dev/null; then \
        groupadd --gid "${USER_GID}" "${USERNAME}"; \
    fi && \
    # UIDが既に存在している場合はそのユーザーを改名、なければ新規作成
    if id -u "${USER_UID}" > /dev/null 2>&1; then \
        EXISTING_USER=$(getent passwd "${USER_UID}" | cut -d: -f1) && \
        if [ "$EXISTING_USER" != "${USERNAME}" ]; then \
            usermod -l "${USERNAME}" "$EXISTING_USER" && \
            groupmod -n "${USERNAME}" "$EXISTING_USER"; \
        fi; \
    else \
        useradd -m -s /bin/bash -u "${USER_UID}" -g "${USER_GID}" "${USERNAME}"; \
    fi && \
    echo "${USERNAME} ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/${USERNAME} && \
    chmod 0440 /etc/sudoers.d/${USERNAME}

# 作業ディレクトリ設定（frontend直下）
USER ${USERNAME}
WORKDIR /workspace/frontend
