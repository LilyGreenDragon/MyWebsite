FROM debian:bullseye

# Устанавливаем утилиты + MariaDB client
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    lsb-release \
    curl \
    ca-certificates \
    bash \
    sudo \
    unzip \
    mariadb-client \
    && rm -rf /var/lib/apt/lists/*

# Скачиваем бинарь Orchestrator
ENV ORC_VERSION=3.2.6
RUN wget -q https://github.com/openark/orchestrator/releases/download/v${ORC_VERSION}/orchestrator-${ORC_VERSION}-linux-amd64.tar.gz \
    && mkdir -p /tmp/orc \
    && tar -xzf orchestrator-${ORC_VERSION}-linux-amd64.tar.gz -C /tmp/orc \
    && mv /tmp/orc/usr/local/orchestrator/orchestrator /usr/local/bin/orchestrator \
    && rm -rf /tmp/orc orchestrator-${ORC_VERSION}-linux-amd64*

# Создаем пользователя orchestrator
RUN useradd -m orchestrator
WORKDIR /usr/local/orchestrator

# Конфиги Orchestrator
COPY mysql_replication/orchestrator/orchestrator.conf.json /etc/orchestrator.conf.json
COPY mysql_replication/orchestrator/post_failover.sh /usr/local/orchestrator/post_failover.sh
COPY mysql_replication/orchestrator/pre_failover.sh /usr/local/orchestrator/pre_failover.sh
RUN chmod +x /usr/local/orchestrator/pre_failover.sh /usr/local/orchestrator/post_failover.sh

USER orchestrator

ENTRYPOINT ["/usr/local/bin/orchestrator"]
CMD ["-config", "/etc/orchestrator.conf.json", "http"]







