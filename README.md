# Пример создания собсвенного веб-сервиса для прокладки маршрута на основе библиотек GraphHopper

Приложение основано на библиотеке [GraphHopper](https://github.com/graphhopper/graphhopper/tree/0.10) версии 0.10.

Позволяет прокладывать маршрут (пешеходный), при этом дополняет [FootFlagEncoder](https://github.com/graphhopper/graphhopper/blob/0.10/core/src/main/java/com/graphhopper/routing/util/FootFlagEncoder.java) выполняя дополнительную проверку. По сути, - пример кастомизации существующих правил.

Сначала назвал проект illustration, потом понял что ни каких иллюстраций здесь и близко нет. Назван он так, ибо иллюстрирует создание и применение собственных правил построения маршрутов с использованием GraphHopper.

## Структура проекта

### Предварительная подготовка данных `ApplicationCreateGraph`

### Использование данных в веб-сервисе `ApplicationRestService`

## См. также

[Routing via Java API](https://github.com/graphhopper/graphhopper/blob/0.10/docs/core/routing.md)

[Low level API](https://github.com/graphhopper/graphhopper/blob/0.10/docs/core/low-level-api.md)
