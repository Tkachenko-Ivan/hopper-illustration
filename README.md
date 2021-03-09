# Пример создания собсвенного веб-сервиса для прокладки маршрута на основе библиотек GraphHopper

Приложение основано на библиотеке [GraphHopper](https://github.com/graphhopper/graphhopper/tree/0.10) версии 0.10. **Все ссылки даны на эту версию!**

Позволяет прокладывать маршрут (пешеходный), при этом дополняет [FootFlagEncoder](https://github.com/graphhopper/graphhopper/blob/0.10/core/src/main/java/com/graphhopper/routing/util/FootFlagEncoder.java) выполняя дополнительную проверку. По сути, - пример кастомизации существующих правил.

Сначала назвал проект illustration, потом понял что ни каких иллюстраций здесь и близко нет. Назван он так, ибо иллюстрирует создание и применение собственных правил построения маршрутов с использованием GraphHopper.

## Использование проекта

Для создания собсвенных правил построения маршрутов, необходимо реализовать класс Encoder, унаследовав его от [AbstractFlagEncoder](https://github.com/graphhopper/graphhopper/blob/0.10/core/src/main/java/com/graphhopper/routing/util/AbstractFlagEncoder.java), см. `AvailabilityFlagEncoder`. 

Для демонстрации, реализованы два эффективных класса, расположены в пакете `encoders`.

### Предварительная подготовка данных `ApplicationCreateGraph`

Веб-сервис работает уже с предварительно расчитанными файлами путей. 

Для их создания необходимо запустить `ApplicationCreateGraph`. Результат работы будет размещён в папке, прописанной в переменной `hopperFolder`.

*В дальнейшем этот шаг можно пропустить, нет смысла пересоздавать эти файлы каждый раз.*

Метод `deleteAllFilesFolder` очищает папку от ранее созданных файлов.

Для работы примера используются файлы ресурсов. В частности метод `fileFromResourceToFolder`, достаёт из ресурсов файл `REGION.osm.pbf`, если у вас уже есть собственный готовый .pbf файл, просто укажите приложению путь к нему. 

***Внимание! Файл `REGION.osm.pbf` весит почти 100 Мбайт и раздувает репозиторий.***

Для создания дополнительных ограничений внесена проверка в метод `handleWayTags` см. `AvailabilityFlagEncoder` (в нём всё сделано по аналогии с `FootFlagEncoder`)

```Java
@Override
public long acceptWay(ReaderWay way) {
    // Проверяем допуспность объекта
    if (restricted.contains(way.getId())) {
        return 0;
    }
    ...
```

Проверяет есть ли идентификатор объекта в списке недоступных для прохода объектов, и если есть - возвращает 0.

Для демонстрации, список заранее предопределён и приведён в ресурсах в папке `restricted`. Метод `getRestricted`, считывает эти файлы. Имена файлов со списками, соответствуют именам Encoder-классов.

***Внимание! Файлы из `restricted` подобраны под `REGION.osm.pbf` и не подойдут для других выгрузок OSM.***

### Использование данных в веб-сервисе `ApplicationRestService`

Для работы веб-сервиса запускайте `ApplicationRestService`.

Он создаст Bean для фабрики `EncoderFactory`, которая предоставит доступ для всех созданных и расчитанных графов, и загрузит готовые файлы. 

Конечная точка, для API предоставляется `FootController`, в нём реализован пример взаимодействия с графами, для построения маршрута.

Пример вызова метода:

```cURL
curl --location --request GET 'http://localhost:8070/foot/getRoute?fromLon=61.006532&fromLat=69.021435&toLon=61.005949&toLat=69.031219&encoderPrinciple=WHEELCHAIR'
```

## Создание новых правил

Для создания новых правил построения маршрута, необходимо:

* Реализовать Encoder
* Добавить его в перечисление `EncoderEnum`

### Применение паттерна "декоратор"

Поскольку в примере демонстрируется применение ограничивающих правил, их можно комбинировать, для этого можно использовать паттерн "декоратор". Для этого, в `AvailabilityFlagEncoder` имеются атрибуты `decoreFlagEncoder` и `isDecored`. 

Единсвенный декорированный метод `acceptWay` (`toString` ещё).

Пример применения декорации Encoder

```Java
new BlindFlagEncoder(restrictedBlind, new WheelchairFlagEncoder(restrictedWheelchair))
```

Декорированный `Encoder`, так же необходимо давить в перечисление `EncoderEnum`

## См. также

[Routing via Java API](https://github.com/graphhopper/graphhopper/blob/0.10/docs/core/routing.md)

[Low level API](https://github.com/graphhopper/graphhopper/blob/0.10/docs/core/low-level-api.md)
