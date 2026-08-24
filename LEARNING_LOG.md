# 📓 Дневник прогресса: Spring Core

Этот файл — мой «конспект лекций». Если закрыл IDEA / перечитал переписку — скинь его мне, и я сразу пойму, на каком мы шаге, что сделано и что нужно вспомнить.

---

## 🎯 Цель проекта: **Notification Hub**

Плугин-ориентированный конвейер обработки и доставки уведомлений.
Spring Core **без Spring Boot** — 12 шагов.

| # | Тема | Статус |
|---|------|--------|
| 0 | Git + скелет проекта | ✅ done |
| 1 | `ApplicationContext` поднимается | ✅ done |
| 2 | Явная конфигурация через `@Bean` | 🟡 in progress |
| 3 | Первая цепочка процессоров | ⏳ |
| 4 | Component scan | ⏳ |
| 5 | Кастомный `BeanPostProcessor` | ⏳ |
| 6 | `Aware`-интерфейсы | ⏳ |
| 7 | События | ⏳ |
| 8 | Профили + `application.properties` | ⏳ |
| 9 | `@ConditionalOnProperty` | ⏳ |
| 10 | SpEL + `Environment` | ⏳ |
| 11 | `BeanFactoryPostProcessor` | ⏳ |
| 12 | Финальный раннер | ⏳ |

---

## ✅ Шаг 0 — Git + скелет

- Репо: `https://github.com/VasiliiBasov/SpringCoreProject`
- Артефакт Maven: `com.vasilii:notification-hub:0.0.1-SNAPSHOT`
- Java 21, Spring 6.1.14 (только `spring-context`)
- Главные классы: `App`, `AppConfig`
- Remote через HTTPS + credential manager

---

## ✅ Шаг 1 — ApplicationContext поднимается (подробно)

### Идея шага на пальцах

Представь, что **Spring — это огромный склад**, куда ты складываешь **готовые объекты (бины)**. Любой код в приложении может прийти на склад и сказать: «дай мне бин с именем `emailChannel`» — и получит его.

Это и есть **IoC-контейнер** (Inversion of Control). Раньше (без Spring) ты сам писал `new EmailChannel(...)` где нужно. Со Spring'ом ты говоришь: «я хочу объект типа `EmailChannel`», а контейнер сам решает, **когда** его создать и **как** настроить.

### Главные штуки шага 1

**1. `ApplicationContext` — это и есть тот самый «склад»**
- Это **интерфейс**. Конкретная реализация, которую мы используем, называется `AnnotationConfigApplicationContext`.
- «AnnotationConfig» = «я буду читать твои аннотации (`@Configuration`, `@Bean`, `@Component`)».
- Есть и другие реализации (`ClassPathXmlApplicationContext` для XML, `GenericWebApplicationContext` для веба) — но в нашем проекте везде `AnnotationConfigApplicationContext`.

**2. `ConfigurableApplicationContext` + try-with-resources**
- `ConfigurableApplicationContext` — расширение `ApplicationContext`, которое можно **закрывать**.
- `try (ctx = new ...)` — после блока Spring сам вызовет `ctx.close()`. Это важно: на close он вызовет все `DisposableBean`-методы и `@PreDestroy`. Если забудешь закрыть — в маленькой программе ничего не случится, в большом приложении будут течь ресурсы.

**3. Что делает `AppConfig.java`**

```java
@Configuration              // "Я — источник определений бинов"
@ComponentScan(...)         // "Просканируй этот пакет и подними всё с @Component"
@PropertySource("...")      // "Подключи этот файл со свойствами"
public class AppConfig { }
```

- `@Configuration` — маркер. Сам по себе **не создаёт ни одного бина**, только говорит Spring: «тут могут быть `@Bean`-методы».
- `@ComponentScan` — без него Spring просканирует **только** `AppConfig`. С ним — пойдёт рекурсивно по пакету `com.vasilii.notificationhub` и поднимет все классы с `@Component`, `@Service`, `@Repository`, `@Controller`.
- `@PropertySource` — подключает properties-файл в classpath. На этом шаге файл пустой, и мы его не используем, но Spring его уже видит.

**4. Что делает `App.java`**
- Создаёт контекст, передавая ему `AppConfig.class` — Spring читает этот класс, находит аннотации.
- Дальше мы вызвали `ctx.getApplicationName()` (пока пустое — зададим позже) и `ctx.getStartupDate()` (timestamp старта JVM-процесса).

### Зачем всё это?

Это фундамент. Дальше мы будем:
- объявлять бины (`@Bean` или `@Component`) — Spring будет создавать их сам
- запрашивать бины у контекста — не писать `new`
- внедрять зависимости (`@Autowired`, конструктор) — Spring будет сам подставлять нужные объекты

### Шпаргалка (распечатай в голове)

| Термин | Что это | Аналогия |
|---|---|---|
| `ApplicationContext` | Склад, где лежат бины | Склад |
| Бин | Объект, управляемый Spring'ом | Коробка на складе |
| `@Configuration` | «Тут лежат правила, что класть на склад» | Инструкция для грузчика |
| `@ComponentScan` | «Пройдись по пакету, всё с `@Component` — на склад» | Маршрут грузчика |
| `@PropertySource` | «Прочитай этот файл настроек» | Папка с доп. инструкциями |

---

## 🟡 Шаг 2 — Явная конфигурация через `@Bean` (ТЕКУЩИЙ, подробно)

### Идея шага на пальцах

В шаге 1 наш склад был **пустой** — мы только его построили. Теперь мы кладём туда первую **коробку** — `EmailChannel`. Причём кладём её **вручную** через `@Bean`-метод, а не через `@Component`. Зачем? Чтобы научиться **контролировать процесс создания**: что передать в конструктор, как назвать, какой scope поставить.

### Главные штуки шага 2

**1. `@Bean` vs `@Component` — два способа положить бин на склад**

| Способ | Когда используют | Пример |
|---|---|---|
| `@Component` на классе | Класс **наш**, мы можем его менять | `@Component class EmailChannel { ... }` |
| `@Bean` в `@Configuration`-классе | Класс чужой / библиотечный, или нужна сложная логика создания | `@Bean EmailChannel emailChannel() { return new EmailChannel("smtp.gmail.com"); }` |

Сейчас мы используем `@Bean`, потому что:
- хотим **внедрить значение из properties** (`smtpHost`) в конструктор
- хотим, чтобы студент понял механику ручного создания

**2. Имя бина = имя метода**

```java
@Bean
public NotificationChannel emailChannel() { ... }  // имя бина: "emailChannel"
```

В Spring у каждого бина есть **id** (имя) и **type** (класс). По id мы получаем: `ctx.getBean("emailChannel")`. По типу — `ctx.getBean(NotificationChannel.class)`.

Если хочешь дать другое имя: `@Bean("myEmail")` — тогда id будет `"myEmail"`.

**3. Singleton — бин создаётся один раз**

Когда ты **первый раз** вызываешь `ctx.getBean("emailChannel", ...)`, Spring:
1. Видит, что бина ещё нет в кэше
2. Вызывает метод `emailChannel()` — конструктор `EmailChannel` срабатывает (отсюда `[ctor]`)
3. Кладёт результат в кэш
4. Отдаёт тебе

Когда ты **второй раз** вызываешь `getBean` — Spring **не вызывает конструктор**, а отдаёт уже готовый объект из кэша. Поэтому:
- `email == email2` → `true` (тот же объект по ссылке)
- `[ctor]` в консоли **ровно один раз**

Это называется **singleton scope** — и он в Spring **по умолчанию**.

**4. `@Value("${key:default}")` — DI из properties**

```java
@Value("${app.channels.email.smtp:smtp.local}")
private String emailSmtp;
```

Spring читает `application.properties`, ищет ключ `app.channels.email.smtp`:
- нашёл → подставляет значение
- не нашёл → подставляет `smtp.local` (то, что после `:`)
- нет и `:` → упадёт с ошибкой

Это **Dependency Injection** через поле (field injection) — самый простой, но и самый «не любимый» способ. Дальше будем использовать конструктор.

**5. `getBeanDefinitionCount()` — почему больше 1**

Spring в контекст кладёт не только наши бины, но и свои служебные:
- `appConfig` (сам `AppConfig` тоже бин)
- `environment` (объект, из которого читаются properties)
- несколько `BeanPostProcessor`-ов (мы их ещё не знаем, но они есть)
- и т.п.

Это нормально. На собесе иногда спрашивают: «как узнать, сколько бинов в контексте?» — вот так, через `getBeanDefinitionCount()`.

### Шпаргалка (распечатай в голове)

| Термин | Что это | Аналогия |
|---|---|---|
| `@Bean` | Метод, чей результат → бин | «Грузчик, сделай коробку и положи на склад» |
| Имя бина | По умолчанию = имя метода | Наклейка на коробке |
| Singleton | Один объект на весь контекст | Коробка одна, её все берут по очереди |
| `@Value` | Подставить значение из properties | «Прочитай настройку из файла» |
| `getBean` | Достать бин по id или типу | «Дай мне коробку с такой наклейкой» |

**Что узнаем:**
- `@Bean` — метод, чьё возвращаемое значение Spring кладёт в контекст
- Имя бина = имя метода (`emailChannel()` → `emailChannel`)
- Singleton scope: бин создаётся один раз, при повторном `getBean` отдаётся тот же объект
- `@Value("${ключ:default}")` — DI из properties с запасным значением
- `ctx.getBeanDefinitionCount()` — считает ВСЕ бины, включая внутренние спринговые

**Структура папок на этом шаге:**
```
com.vasilii.notificationhub/
├── App.java              ← точка входа, дёргает ctx.getBean
├── AppConfig.java        ← @Configuration + @Bean
├── api/
│   ├── ChannelType.java  ← enum
│   └── NotificationChannel.java  ← интерфейс
└── channel/
    └── EmailChannel.java ← первая реализация
```

**Найденная опечатка в моём коде (поправь у себя):**
- `api/NotificationChannel.java` строка 4: `ChannelType.getType();` → должно быть `ChannelType getType();`

**Что нужно сделать сейчас:**
1. Поправить опечатку в `NotificationChannel.java`
2. Запустить `App.main()`
3. Убедиться, что в консоли:
   - строка `[ctor] EmailChannel создан с smtpHost=...` появляется **ровно один раз**
   - `Тот же самый? true`
   - `Каналов в контексте: 1`
4. Опционально: добавить в `application.properties` строку `app.channels.email.smtp=smtp.gmail.com:587` и убедиться, что в выводе поменялось значение
5. Когда всё ок — коммит + пуш:
   ```
   git add .
   git commit -m "Step 2: explicit @Bean configuration with EmailChannel"
   git push
   ```

---

## 🎓 Глубокое погружение: основы (после шага 2)

### Зачем Spring — на нашем проекте

**Без Spring** в `main` пришлось бы писать:
- `Properties props = loadProps(...)` — загрузить настройки
- `EmailChannel email = new EmailChannel(smtpHost)` — создать канал
- `Map<ChannelType, NotificationChannel> channels = Map.of(EMAIL, email)` — собрать карту
- `NotificationService service = new NotificationService(channels)` — собрать сервис
- И так для **каждого** нового канала — лезть в `main`, дописывать

**Со Spring** (после шага 5):
- Добавил класс `WebhookChannel` с аннотацией `@Component` — и всё. **Spring сам** нашёл, создал, положил в контейнер. `AppConfig` не трогаем.

**Главная ценность Spring:** изменение поведения через **настройки и аннотации**, а не через правку кода.

### Жизненный цикл бина (полный порядок)

```
new AnnotationConfigApplicationContext(AppConfig.class)
   ↓
[1] Spring читает AppConfig, создаёт BeanDefinition
   ↓
[2] Регистрирует BeanPostProcessor-ы
   ↓
[3] ★ Первый getBean("emailChannel") ★
   ↓
[4] Создаёт объект: new EmailChannel(smtpHost)  ← КОНСТРУКТОР
   ↓
[5] Внедряет зависимости: @Autowired, @Value
   ↓
[6] BeanPostProcessor.postProcessBeforeInitialization()
   ↓
[7] @PostConstruct / InitializingBean.afterPropertiesSet()
   ↓
[8] BeanPostProcessor.postProcessAfterInitialization()
   ↓
[9] Бин готов, кладётся в кэш
   ↓
[10] Второй getBean → отдаёт из кэша (конструктор НЕ вызывается)
   ↓
... работа ...
   ↓
ctx.close()
   ↓
[11] @PreDestroy / DisposableBean.destroy()
```

### Scope-ы

| Scope | Экземпляров | Создаётся | Умирает |
|---|---|---|---|
| singleton (default) | 1 | при первом getBean | при ctx.close() |
| prototype | новый каждый раз | при каждом getBean | **никогда** (@PreDestroy НЕ зовётся!) |
| request | 1 на HTTP-запрос | на старте запроса | в конце запроса |
| session | 1 на HTTP-сессию | на старте сессии | при уничтожении сессии |

### DI: 3 способа внедрения

| Способ | Плюсы | Минусы |
|---|---|---|
| Constructor (✅ best) | final поля, видно в сигнатуре, легко тестить | много параметров = громоздко |
| Setter | для опциональных зависимостей | можно забыть вызвать |
| Field (`@Autowired` на поле) | минимум кода | нельзя final, нужен контекст для создания |

### Что внутри ApplicationContext

ApplicationContext — это **один объект**, который реализует сразу несколько интерфейсов:
- `ListableBeanFactory` → `getBeansOfType()`, `getBeanNamesForType()`
- `ApplicationEventPublisher` → `ctx.publishEvent(...)` (шаг 7)
- `Environment` → `ctx.getEnvironment().getProperty(...)` (шаг 8)
- `MessageSource` → `ctx.getMessage(...)` (i18n)
- `ResourcePatternResolver` → загрузка ресурсов

---

> ⏱ Время и шкалы переехали в [`PROGRESS.md`](./PROGRESS.md)

---

## 🧠 Копилка фактов (то, что точно спросят на собесе)

| Вопрос | Ответ |
|--------|-------|
| Зачем нужен Spring? | Чтобы не создавать объекты вручную: контейнер сам создаёт, связывает, настраивает. Меняем поведение через настройки, не через код |
| Чем `BeanFactory` отличается от `ApplicationContext`? | `ApplicationContext` = `BeanFactory` + события + i18n + авто-регистрация `BeanPostProcessor` |
| Что такое бин? | Объект, управляемый контейнером Spring |
| `@Configuration` vs `@ComponentScan`? | `@Configuration` — маркер класса с `@Bean`-методами. `@ComponentScan` — указывает пакет для поиска `@Component` |
| `@Component` vs `@Bean`? | `@Component` на классе, `@Bean` на методе. Первое для своих классов, второе для чужих |
| Singleton scope? | Один экземпляр на весь контекст (по умолчанию) |
| Как задать scope prototype? | `@Scope("prototype")` на `@Bean` или `@Component` |
| Что делает `@Value("${key:default}")`? | Подставляет значение из properties, если нет — default. Без `:` упадёт при старте |
| Порядок жизненного цикла? | конструктор → @Autowired → BeanPostProcessor.before → @PostConstruct → BeanPostProcessor.after |
| Какой DI лучше? | Constructor injection — final, видно в сигнатуре, легко тестить |

---

## ❓ Мини-вопросы для самопроверки (после шага 2)

Попробуй ответить сам себе или мне, прежде чем гуглить:

1. Почему `ctx.getBean("emailChannel", NotificationChannel.class)` отдаёт тот же объект при двух вызовах?
2. Что будет, если убрать `@ComponentScan` — бин `emailChannel` всё равно появится? Почему?
3. Если в `@Value` опечататься в имени ключа, программа упадёт или подставится default? *(подсказка: зависит от того, есть ли `:` в строке)*
4. `ctx.getBeanDefinitionCount()` возвращает 7, а не 1. Откуда взялись остальные 6?
