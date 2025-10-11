#!/bin/bash

COMMON_CONFIG="orchestrator.conf.json"
NODE_CONFIGS=("orchestrator1.conf.json" "orchestrator2.conf.json" "orchestrator3.conf.json")

mkdir -p conf

for config in "${NODE_CONFIGS[@]}"; do
    echo "Создаем $config"
    # Объединяем общий конфиг с конфигами нод
    jq -s '.[0] * .[1]' "$COMMON_CONFIG" "node/$config" > "conf/$config"
done