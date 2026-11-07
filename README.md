# ifs25044-pbo2026-springboot-cashflow

Aplikasi web Catatan Keuangan (Spring Boot + Vaadin) dengan Clean Architecture.

## Menjalankan

    .\mvnw.cmd spring-boot:run

Buka http://localhost:8080 (port diatur lewat `APP_PORT` di `.env`).

## Test & coverage

    .\mvnw.cmd clean verify

Laporan Jacoco: `target/site/jacoco/index.html`.

## Struktur

    domain/      entity, exception, repository (port)
    usecase/     FinanceUseCase
    adapter/     repository (in-memory), presenter
    framework/   view (Vaadin)
    config/      AppConfig (composition root)

## Build production (untuk Lighthouse / deploy)

    .\mvnw.cmd clean package -Pproduction
    java -jar target\ifs25044-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar

Skor Lighthouse harus diuji pada mode production (bukan `spring-boot:run`).
Deploy otomatis memakai `Jenkinsfile` (`mvnw clean verify -Pproduction`).
