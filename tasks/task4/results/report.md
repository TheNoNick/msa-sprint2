# Задание 4. Helm и CI/CD в Minikube

## Что сделано

- сервис `booking-service` реализован на Go в качестве мока
- добавлены `helm чарты` с проверками
- добавлены пайплайны и результат их выполнения. Также возможность разворачивать в разных окружениях
- выполнены все проверки и добавлены в `/results`

## Проверено

- Пайплайн `make ci` прошёл полностью (`pipeline.log`).
- Staging: `/feature` → `Feature X is enabled!` (`feature-check.txt`).
- Prod: развернуто 3 реплики (`deploy-prod.log`), `/feature` → 404 (`feature-check.txt`).
- DNS изнутри кластера и `/ping` через port-forward отвечают `pong` (скриншоты).

## Файлы

- `pipeline.log` — прогон пайплайна
- `deploy-prod.log` — выкатка prod
- `feature-check.txt` — фича-флаг на staging и prod
- `kubectl.txt`, `images.txt` — поды, сервис, helm-релиз и образы (снято на staging)
- `check-dns.png`, `check-status.png`, `curl.png` — скриншоты проверок
- `.gitlab-ci.yml`, `values-staging.yaml`, `values-prod.yaml` — копии конфигов
