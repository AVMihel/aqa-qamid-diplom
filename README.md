# Инструкция по запуску автотестов

## Предварительные требования

Перед началом работы убедитесь, что у вас установлено:

- Docker (версия 20.10.0 или выше)
- Docker Compose (версия 1.29.0 или выше)
- Java JDK 11
- Git

## Установка и настройка

1. Клонируйте репозиторий:
  git clone https://github.com/AVMihel/aqa-qamid-diplom.git

2. Соберите проект:
  ./gradlew build
   
## Запуск тестового окружения

Запустите контейнеры с базами данных и тестовым приложением:
  docker-compose up -d

## Запуск тестов

Запустите автотесты:
  ./gradlew clean test

## Просмотр результатов

Сгенерируйте и откройте отчёт Allure:
  ./gradlew allureServe

[![Java CI with Gradle](https://github.com/AVMihel/aqa-qamid-diplom/actions/workflows/gradle.yml/badge.svg)](https://github.com/AVMihel/aqa-qamid-diplom/actions/workflows/gradle.yml)
