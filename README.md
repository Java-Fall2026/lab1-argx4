# Лабораторна робота №1 — варіант {{VARIANT}}

Основи Java та модифікатори доступу.

![build](https://github.com/Java-Fall2026/{{REPO}}/actions/workflows/build.yml/badge.svg)

- Умова: https://github.com/Java-Univer-2025/Java-Univer-2025.github.io/blob/main/lab-01/README.md
- Домени та поля: https://github.com/Java-Univer-2025/Java-Univer-2025.github.io/blob/main/lab-01/tasks_2025_domains.md

## Структура

```
src/main/java/ua/common/BaseEntity.java   готовий клас, не змінюйте
src/main/java/ua/<домен>/model/           ваші сутності
src/main/java/ua/<домен>/util/            ValidationHelper, FormatHelper, <Домен>Utils
src/main/java/ua/<домен>/Main.java        демонстраційний клас
```

`<домен>` — коротка назва вашої предметної області: `hotel`, `library`, `parking` тощо.

## Як зібрати і запустити

```
./gradlew build
./gradlew run
```

У Windows замість `./gradlew` пишіть `gradlew.bat`.

Перед першим `run` впишіть свій клас у `build.gradle`:

```groovy
application {
    mainClass = 'ua.hotel.Main'
}
```

Після кожного пушу репозиторій збирається автоматично — стежте за бейджем угорі. Червоний бейдж = код не компілюється = робота не перевіряється.

## Чекліст здачі

- [ ] усі сутності варіанта успадковують `BaseEntity` і викликають `super()`
- [ ] поля `private`, сеттери тільки там, де в профілі стоїть ✎
- [ ] два класи створюються через `of(...)`, конструктор у них `private`
- [ ] валідація в конструкторах і сеттерах, повідомлення показує очікуване й отримане
- [ ] `ValidationHelper` і `FormatHelper` — package-private, `<Домен>Utils` — public
- [ ] два обчислювані методи з профілю
- [ ] `equals`, `hashCode`, `toString` у кожній сутності
- [ ] `Main` демонструє всі сценарії, зокрема невдалі
- [ ] `AI.md` заповнено
- [ ] бейдж зелений

## Здача

Код у цьому репозиторії + захист 5 хв. Без захисту робота не зарахована.
