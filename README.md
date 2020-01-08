# iFunny

https://funcodechallenge.com/

## Преамбула

// TODO

## Сборка приложения

Для того, чтобы собрать приложение, необходимо иметь предустановленную JDK ≥ 8 и выполнить следующую команду:

```
$ ./gradlew assemble
```

### Сборка с тестами

Дело в том, что тесты, требующие БД производятся против реального сервера БД. 
На мой личный взгляд это один (или самый) из самых надежных способов тестирования. 

Сложность заключается лишь в развёртывании необходимого окружения. 
В случае с CI/CD, GitLab Pipeline настроен таким образом, что перед сборкой поднимаются нужные сервисы и линкуются с тестируемым образом.

Для того, чтобы локально добиться того же результата, был создан `docker-compose.yml` файл.  

Итак, для сборки приложения с запуском всех тестов локально необходимо выполнить две команды:

```
$ docker-compose up -d
$ ./gradlew build
```

## Сборка Docker образа

Прежде, чем приступить к сборке Docker образа необходимо собрать само приложение. 
Необходимые шаги подробно описаны в соответствующем разделе.

Как только приложение создано, сборка Docker образа становится тривиальной задачей:

```
$ docker build -t ifunny .
```

## Запуск

### Переменные окружения

#### MongoDB

* `MONGODB_HOST` – Хост сервера MongoDB (По умолчанию `localhost`).
* `MONGODB_PORT` – Порт сервера MongoDB (По умолчанию `27017`)
* `MONGODB_DATABASE` - Имя базы данных MongoDB (По умолчанию `ifunny`).
* `MONGODB_USERNAME` – Имя пользователя MongoDB (По умолчанию `ifunny`).
* `MONGODB_PASSWORD` – Пароль пользователя MongoDB (По умолчанию `ifunny`).
* `MONGODB_AUTH_DB` – База данных аутентификации MongoDB (По умолчанию `admin`).

#### AWS S3

* `AWS_S3_ACCESS_KEY` – [Ключ доступа](https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/signup-create-iam-user.html) к S3. 
* `AWS_S3_SECRET_KEY` – [Ключ доступа](https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/signup-create-iam-user.html) к S3. 
* `AWS_S3_REGION` – S3 Регион.
* `AWS_S3_BUCKET` – S3 Bucket.

Если S3 Bucket не существует на этапе запуска приложения, он будет создан автоматически. Но не уверен, что это хорошая практика,
поэтому получите warning: лучше контролировать создание корзин и/или делать это ручками.

#### Redis

* `REDIS_HOST` - Хост сервера Redis (По умолчанию `localhost`).
* `REDIS_PORT` – Порт сервера Redis (По умолчанию `6379`).
* `REDIS_DB` – Индекс БД в Redis (По умолчанию `0`).
* `SPRING_REDIS_PASSWORD` – Если для доступа к Redis требуется пароль, укажите эту переменную.

### Локальное окружение

Для удобства развертывания локального окружения (в целях разработки) в корневом каталоге расположен `docker-compose.yml`.

#### Запуск через терминал

```
$ docker-compose up -d
```

#### Запуск через IDEA'ю

Если Вы используете Intellij IDEA, в репозиторий добавлена конфигурация запуска Docker-compose под именем `Local Environment`.

### Swagger

Swagger Open API v3 Specification: `/v3/api-docs`.

Swagger UI: `/swagger-ui.html`.

## Реализация

### Версионирование

// TODO

### Источники

Источник – веб-сайт с мемами, который является предметом для парсинга и обработки приложением. 

Архитектура основана на предположении, что каждый источник возвращает ленту, разделенную на страницы (Pagination).

В объектной модели источник представляет класс `Channel` (канал). 
Для добавления нового источника разработчику необходимо объявить новый бин класса `Channel`,
реализовав абстрактные методы: `pagePath`, `parsePage`, `parseMeme`.
Изначально была идея реализовать Kotlin DSL, позволяющий как-то простенько конфигурировать новые источники и даже иметь Hot-Reload,
но на деле оказалось, что на короткой дистанции выгода от этого подхода неоднозначна.

Очень упрощенно процесс обработки каждого источника следующий:

1. Получаем URL интересующей нас страницы по её номеру используя метод `pagePath`. Например, `http://debeste.de/123`.
1. Делаем запрос по полученному из предыдущего пункта адресу, получаем содержимое страницы и отправляем в метод `parsePage`. 
Дело в том, что каждый источник очень специфичен и некоторые данные доступны только на этом этапе,
а некоторые станут доступны на следующем. Например, в случае с `Funpot`, на этом этапе доступна дата публикации мема,
а на самой странице мема эта информация отсутствует. Притом URL до самого ресурса (файл картинки или видео)
станет известен лишь на персональной странице мема. Поэтому на этом этапе грабим всю доступную информацию,
на следующем этапе будем её дополнять. Тем не менее, ключевым (обязательным) к заполнению является поле `pageUrl` 
(путь до индивидуальной страницы мема).
1. Делаем запрос по полученному из предыдущего пункта `pageUrl` и грабим недостающую информацию методом `parseMeme`. 
Обязательным на этом этапе является путь до самого файла мема (картинка, гифка, видео).

Профит.

### Фильтрация дубликатов

Итак, фильтрация мемов по схожести оказалась непростой задачей в силу многих обстоятельств, которыми я хочу поделиться.

После первого мема стало очевидно, что просто брать хеш-сумму от файла не имеет никакого смысла:
каждый источник старается запихать на картинку свой водяной знак (watermark). 
А хеш-функции как раз устроены таким образом, что даже изменение в одном байте вызовет лавинное преобразование. 
Нужно было заиметь функцию, которая бы по изображению генерировала некий уникальный fingerprint с достаточной разреженностью.

Первая идея, которая пришла в голову – уменьшать изображение, crop'ить центр, скажем 100х100,
и генерировать уникальную строку (хеш-код) в зависимости от цветов пикселей на этом участке. 

Вторая идея оказалась лучше – поискать что-нибудь в Интернетах. [Нашлось](https://gitlab.com/ruslanys/ifunny/issues/3)!
Оказалось, что то, что я себе там придумал уже изобретено белым человеком и называется [Perceptual hashing](https://en.wikipedia.org/wiki/Perceptual_hashing).
Короче говоря, это тип функции, позволяющий генерировать некий «отпечаток», по которому дальше можно будет определить
схожесть изображений.

Материалы по теме:

* [Looks Like It](http://www.hackerfactor.com/blog/index.php?/archives/432-Looks-Like-It.html)
* [Using Perceptual Hash Algorithms to Identify Fragmented and Transformed Video Files](https://pdfs.semanticscholar.org/8285/4824363b4088cc65d49da7f7d8bea5b8082c.pdf)
* [Поиск дубликатов изображений на примере Instagram](https://nauchkor.ru/uploads/documents/587d36515f1be77c40d58c6d.pdf)

Существует некоторое множество уже реализованных алгоритмов. Из наиболее известных, наверно – [AverageHash](http://www.hackerfactor.com/blog/index.php?/archives/432-Looks-Like-It.html)
и [pHash](https://www.phash.org/). Казалось, задача решена, но как бы не так. 

Несмотря на то, что pHash считается «лучшим в классе» его использование оказалось затруднительным. Т.к. алгоритм точный,
даже одна и та же картинка с разными логотипами источников даст разный отпечаток. Конечно, довольно легко можно определить
схожесть изображений воспользовавшись «[Расстоянием Хэмминга](https://en.wikipedia.org/wiki/Hamming_distance)».
Если очень грубо – XOR'им отпечатки двух изображений и считаем количество взведенных разрядов, чем меньше битов взведено,
тем более схожи изображения. В MySQL есть функция `BIT_COUNT`, которая, как раз, может посчитать количество единичек,
оставшихся после XOR'а. Тогда функция расчета расстояния между отпечатками для выборки из БД могла бы выглядеть так:
`1 - (bit_count(phash1 ^ phash2) / 64.0)`, где 64 – размер отпечатка (количество бит).

Увы, но в MongoDB, такой функции нет. Но если гора не идет к Магомету, то Магомет пойдет к горе.
Была идея представить массив байт, как строку и воспользоваться полнотекстовым поиском, как это делали ребята из [Бостона](https://www.slideshare.net/ZacharyTong/boston-meetupgoingorganic/).
Но результата это не дало, да и выглядит, как стрельба из пушки по воробьям.

[Битовые](https://docs.mongodb.com/manual/reference/operator/update/bit/) операции и [побитовое](https://docs.mongodb.com/manual/reference/operator/query-bitwise/)
сравнение в MongoDB выглядит похоже на то, что нужно, однако решения «на поверхности» также не нашлось.

Да и в любом случае, очевидно, что для расчета расстояния Хэмминга нужно пройтись по всей коллекции документов.
А что, если этих изображений (отпечатков) сотни тысяч? Миллионы? Миллиарды? Учитывая специфику приложения,
это вполне реально, как мне кажется.

Можно искать отпечаток с нулевым расстоянием, т.е. абсолютно эквивалентный, в БД, и если такой не находится,
то сохранять мем. А дальше заиметь некоторый фоновый процесс ("уборщик"), который будет искать дубликаты,
сравнивая расстояние Хэмминга между отпечатками различных изображений. Проверенные изображения можно помечать.
И хотя этот метод с высокой точностью асинхронно может отыскать дубликаты, у него много проблем и сложностей в реализации.
На короткой конкурсной дистанции это точно не тот путь, которым нужно следовать. 

Второй вариант, это взять подходящий (с допущениями) алгоритм хеширования,
который в общем случае будет удовлетворять условию и отпечаток похожих изображений будет идентичен.
Тогда можно просто искать в БД изображения с таким отпечатком и если не находятся, то считать, что дубликатов нет.
Например, AverageHash с длиной отпечатка в 64 бита имеет как хорошую разряженность
(в 64 бита можно уместить [18 квинтиллионов](https://www.wolframalpha.com/input/?i=2%5E64) значений), так и неплохие результаты. 
Но придется смириться с тем, что будут ложно позитивные срабатывания, а это значит — пропуск мема.
Надо сказать, что мемы-тексты очень плохо распознаются. Анализ показал, что если иметь 10 картинок с белым текстом на чёрном фоне,
то больше половины могут считаться дубликатами, хотя текст там совершенно разный. 
Можно увеличить точность, увеличив размер отпечатка, но тогда рискуем не распознать дубликат, посчитав,
что картинки с разными водяными знаками — разные картинки.
В общем, наиболее сложным в данном решении является подбор хеш-функции, и как и многое другое, вопрос выбора хеш-функции –
tradeoff.

Ну и наконец можно совмещать различные хеш-функции.
Так, например, можно иметь по два отпечатка на каждое изображение. Первый — для поиска схожих изображений в БД по ключу.
Если изображения схожи и/или являются дубликатами, то расстояние между их отпечатками должно быть равно нулю (идентичные отпечатки).
Кстати, WaveletHash в 32 бита с 5-ю циклами отлично себя показал для этих целей. Понятное дело,
что с такой хеш-функцией будет много коллизий и ложных срабатываний. Второй отпечаток должен быть более точным,
как раз для того, чтобы эти проблемы устранить. Тогда при добавлении изображения можно рассчитать два отпечатка двумя разными хеш-функциями.
Дальше по первому отпечатку ищем все схожие изображения из БД и рассчитываем расстояние Хэмминга между вторыми отпечатками (более точными). 

Каждый из описанных вариантов обладает преимуществами и недостатками.
И хотя в реальном проекте я бы ещё поковырялся в этом вопросе, в рамках конкурсного задания поверхностный анализ третьего подхода,
увы, не принес существенных улучшений относительно второго. А раз «овчинка выделки не стоит»,
было принято решение остановиться на втором варианте, т.к. третий нуждается в дополнительной проработке и реализации.
А как известно, лучший код — тот код, которого нет.


### Его Величество Евент

// TODO
