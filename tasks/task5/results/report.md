# Задание 5. Управление трафиком с Istio

## Что сделано

- скопирован сервис из task4 и добавлены 2 версии `v1` - основная. `v2` - c флагом через заголовок `X-Feature-Enabled: true`
- Service общий, Istio делит поды по метке `version`
- Istio включена инъекция для неймспейса `default`
- запросы снаружи идут через гетвей (`istio/gateway.yaml`)
- canary 90/10, retry и fallback — `virtual-service.yaml`
- circuit breaker реализован в `destination-rule.yaml`
- фича-флаг — `envoy-filter.yaml`: на ingress-gateway первым ставится маршрут `X-Feature-Enabled: true` (v2). В ответ добавляется заголовок `x-routed-by: envoy-filter`
- в пайплайне `deploy` ставит обе версии и применяет `istio/`

## Проверено

- Пайплайн прошёл (`pipeline.log`).
- Istio: поды `istio-system` запущены, инъекция в `default` включена (`check-istio.txt`).
- Canary: из 100 запросов 89 на v1, 11 на v2 (`check-canary.txt`).
- Фича-флаг: с заголовком запрос уходит на v2 (`check-feature-flag.txt`).
- Fallback: перед скриптом v1 погашен через `kubectl scale deploy booking-service-v1 --replicas=0`, запрос ответил v2 (`check-fallback.txt`). После возврата v1 трафик снова делится 90/10.

## Файлы

- `values-v1.yaml`, `values-v2.yaml` — конфигурации версий
- `virtual-service.yaml`, `destination-rule.yaml`, `envoy-filter.yaml` — правила Istio
- `pipeline.log` — прогон пайплайна
- `check-istio.txt`, `check-canary.txt`, `check-feature-flag.txt`, `check-fallback.txt` — вывод проверочных скриптов
