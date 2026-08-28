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
| 2 | Явная конфигурация через `@Bean` | ✅ done |
| 3 | Первая цепочка процессоров | ✅ done |
| 4 | Component scan | ✅ done |
| 5 | Кастомный `BeanPostProcessor` | ✅ done |
| 6 | Bean lifecycle: `@PostConstruct` / `@PreDestroy` | ✅ done |
| 7 | Bean scope: prototype vs singleton | ✅ done |
| 8 | AOP-прокси через `BeanPostProcessor` (ProxyFactory) | ✅ done |
| 9 | SpEL и `@Value`: `${}`, `#{}`, дефолты | ✅ done |
| 10 | Property-файлы и `@PropertySource`, приоритеты | ✅ done |
| 11 | События (`@EventListener`) + `@ConfigurationProperties` | ✅ done |
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

## ✅ Шаг 3 — Первая цепочка процессоров (подробно)

### Идея шага на пальцах

Шаг 1 — у нас был один канал (`emailChannel`). Шаг 3 — мы добавляем **второй** (`smsChannel`), и убеждаемся, что в контейнере **живут оба сразу**. Это и есть «цепочка»: вместо гигантского `if/else` («если email — шли туда, если sms — сюда») мы кладём все каналы в **коллекцию** и перебираем её.

### Что нового появилось в коде

**1. `SmsChannel` — близнец `EmailChannel`**
- Тот же интерфейс `NotificationChannel`, та же структура: `final` поле, конструктор, `send(...)`, `getType()`.
- Разница только в **доменных полях**: у email — `smtpHost`, у sms — `gatewayUrl`. Это важно: «тип канала» и «параметры подключения» — разные вещи, хотя оба инжектятся через конструктор.
- `getType()` возвращает `ChannelType.SMS` — **enum** гарантирует, что мы не опечатаемся в строке.

**2. В `AppConfig` — два `@Bean` метода**
- `emailChannel()` и `smsChannel()` — каждый создаёт свой объект и кладёт в контекст.
- Имя бина в контексте = имя метода (`emailChannel`, `smsChannel`). Поэтому `ctx.getBean("emailChannel", ...)` работает.
- **Singleton по умолчанию** — на каждый `@Bean` создаётся **ровно один** объект. Доказательство: в консоли две строки `[ctor] ...` (по одной на канал), и при повторном `getBean` отдаётся тот же экземпляр (`email == email2` → `true`).

**3. Два способа достать все бины одного типа**

| Метод | Что возвращает | Когда использовать |
|---|---|---|
| `getBeanNamesForType(T.class)` | `String[]` — массив **имён** | Когда нужно только перечислить или достать позже по имени |
| `getBeansOfType(T.class)` | `Map<String, T>` — имя → бин | Когда нужно сразу **работать** с бинами (вызывать методы, фильтровать, перебирать) |

На собесе могут спросить: «почему два метода?» — потому что иногда нужны только имена (быстрее, не создаёт ссылок), а иногда нужны сами бины.

**4. `@Value` с default — теперь два примера**

```java
@Value("${app.channels.email.smtp:smtp.local}")             // default = "smtp.local"
@Value("${app.channels.sms.gatewayUrl:http://localhost:8080/sms}")  // default = "http://..."
```

- Символ `:` отделяет **ключ** от **дефолта**. Если ключа нет в properties — берётся default, программа не падает.
- **Без `:`** — если ключа нет, контекст **не поднимется**, будет `IllegalArgumentException` на старте.
- Дефолты — это «костыль для разработки». В проде ключ всегда должен быть в `application.properties` или в переменных окружения.

### Что я понял про DI на практике

```
EmailChannel(String smtpHost)  ← constructor injection
  ↑
new EmailChannel(emailSmtp)     ← AppConfig создаёт
  ↑
@Value("${...}") String emailSmtp  ← Spring подставляет
```

Spring **сам** подставляет значение из properties в поле `emailSmtp`, потом **сам** вызывает конструктор с этим значением. Мы не пишем ни `Properties.load(...)`, ни `new EmailChannel(props.get("..."))`. Это и есть инверсия контроля — **мы говорим, ЧТО нам нужно, Spring решает, КАК это дать**.

### Маленький урок про косметику

Я сначала оставил `smtpHost` внутри `SmsChannel` (просто скопировал EmailChannel и переименовал класс). Это **компилируется**, но семантически криво: у SMS нет SMTP. На ревью/собесе заметят. Хорошее правило: переименовывая класс — переименовывай и **внутренние поля/сообщения**, иначе код врёт о себе.

### Структура файлов после шага 3

```
src/main/java/com/vasilii/notificationhub/
├── App.java                  ← + getBeansOfType(...) блок
├── AppConfig.java            ← + @Bean smsChannel() + @Value для sms
├── api/
│   ├── ChannelType.java      ← EMAIL, SMS, PUSH, WEBHOOK
│   └── NotificationChannel.java  ← контракт канала
└── channel/
    ├── EmailChannel.java     ← шаг 2
    └── SmsChannel.java       ← шаг 3
```

---

## ✅ Шаг 4 — Component scan (подробно)

### Идея шага на пальцах

Шаги 2–3: всё руками — `@Bean`-метод на каждый канал, `@Value`-поля в `AppConfig`, явное создание объектов.

Шаг 4: Spring **сам** находит классы и сам создаёт бины. Мы только **помечаем** классы аннотацией `@Component`, а дальше component scan (он у нас в `AppConfig` уже был с самого начала!) делает всю работу.

### Главные изменения в коде

**1. `EmailChannel` и `SmsChannel` теперь `@Component`**

```java
@Component
public class EmailChannel implements NotificationChannel {
    private final String smtpHost;

    public EmailChannel(
        @Value("${app.channels.email.smtp:smtp.local}") String smtpHost
    ) {
        this.smtpHost = smtpHost;
    }
}
```

**`@Value` переехал** с поля в `AppConfig` **прямо на параметр конструктора**. Это критично: когда Spring создаёт бин через `@Component`, он вызывает конструктор. Если параметр не помечен `@Value` или не является бином — Spring не знает, что туда подставить.

**2. `AppConfig` стал «тонким»**

Было (шаг 3):
```java
@Configuration
@ComponentScan(...)
@PropertySource(...)
public class AppConfig {
    @Value("${app.channels.email.smtp:smtp.local}") String emailSmtp;
    @Value("${app.channels.sms.gatewayUrl:...}")  String smsGatewayUrl;
    @Bean public NotificationChannel emailChannel() { return new EmailChannel(emailSmtp); }
    @Bean public NotificationChannel smsChannel()   { return new SmsChannel(smsGatewayUrl); }
}
```

Стало (шаг 4):
```java
@Configuration
@ComponentScan(...)
@PropertySource(...)
public class AppConfig { }
```

`AppConfig` теперь **не знает**, какие именно бины есть. Он только говорит: «сканируй этот пакет». Это и есть **инверсия контроля** в чистом виде.

### Что я понял про `@Component` vs `@Bean`

| | `@Component` | `@Bean` |
|---|---|---|
| Где ставится | На **класс** | На **метод** в `@Configuration` |
| Кто создаёт бин | **Spring** сам (через конструктор) | **Ты** пишешь логику создания |
| Когда использовать | Свои классы | Чужие классы, сложная логика, несколько бинов одного типа |
| Имя бина | Имя класса с маленькой буквы (`EmailChannel` → `emailChannel`) | Имя метода |
| `getBeanDefinitionCount` | Не меняется при замене одного на другое | |

### Главный вывод

`@Component` и `@Bean` дают **одинаковый результат** — бин в контексте. Разница только в том, **как** он туда попал. Переход с `@Bean` на `@Component`:
- `getBeanDefinitionCount()` остался **7** (те же бины, разная регистрация)
- В консоли — те же 2 строки `[ctor] ...`
- `getBeansOfType(...)` показывает 2 канала

### Один «подводный камень» — конструктор без `@Value`

Если убрать `@Value` с параметра конструктора, контекст **упадёт на старте**:
```
UnsatisfiedDependencyException: Error creating bean 'emailChannel':
Unsatisfied dependency expressed through constructor parameter 0:
No qualifying bean of type 'java.lang.String' available
```

Spring говорит: «я создал бин `emailChannel`, но не знаю, что подставить в параметр `String smtpHost`». Решение — либо `@Value` на параметре, либо сделать параметр бином (например, передать другой бин).

### Структура файлов после шага 4

```
src/main/java/com/vasilii/notificationhub/
├── App.java                       ← без изменений
├── AppConfig.java                 ← 3 аннотации, ноль кода
├── api/
│   ├── ChannelType.java           ← enum
│   └── NotificationChannel.java   ← интерфейс
└── channel/
    ├── EmailChannel.java          ← @Component + @Value на параметре
    └── SmsChannel.java            ← @Component + @Value на параметре
```

---

## 🛠 Правило: не отвечай на вопросы ученика в «Идее на пальцах»

**26.08.2026, шаг 8:** я в разделе «Идея на пальцах» сам рассказал про декоратор vs прокси (как ученик должен был ответить сам), а потом задал этот вопрос как «подумай». Ученик справедливо отчитал.

**Правило:** в разделе «Идея на пальцах» — **только** мотивация и общая картина. Конкретные вопросы для проверки — **только** в блоке «Вопросы на подумать», и до этого блока я не должен раскрывать ответ. Если не уверен, что смогу удержаться — лучше вообще не упоминать тему в «Идее».

## 🛠 Правило: не давай устаревших фактов

**26.08.2026, шаг 8 (эксперимент 2):** я утверждал, что Spring выдаст `BeanPostProcessorChecker` warning при `@Scope("prototype")` на BPP. Ученик проверил — **никакого предупреждения нет**. Аннотация отрабатывает тихо.

**Причина:** я помню это поведение по Spring 4/5, а в Spring 6.x проверка стала менее агрессивной (или убрана). Не надо выдавать знания из старых версий за текущее поведение.

**Правило:** если не уверен в точном поведении Spring 6 — говори «проверь экспериментально», а не утверждай как факт. Особенно про warning-сообщения и сообщения об ошибках.

---

## 🛠 Правило: явно выделяй мини-экзамен

**26.08.2026, шаг 9:** ученик сказал «мини-экзамена не было». Я проводил 3 вопроса, но они шли вперемешку с теорией, без рамки «это мини-экзамен, в конце подведём итог».

**Правило:** мини-экзамен должен быть **явным блоком** с заголовком «Мини-экзамен по шагу N», вопросами друг за другом, и итоговой таблицей в конце. Не растворять вопросы в теории.

**Дополнение (26.08.2026):** мини-экзамен проводим **без вариантов ответа** (A/B/C/D). Ученик отвечает своими словами или пишет код. Варианты — это тест, а не понимание.

**Дополнение (28.08.2026):** в «Идее на пальцах» и пояснениях к вопросам **не давать ответ**. Ученик сказал «ты сам ответил» — я в подсказке сразу написал ответ. Правило: подсказка должна **направлять мысль**, но **не выдавать** сам ответ.

**Дополнение (28.08.2026):** если вопрос проверяет «знание фичи X», пример **должен** её задействовать. Ученик правильно заметил: в моём примере relaxed binding не нужен — имена совпадают точно. Ученик сказал «не увидел подводного камня». Правило: проверять, что пример **реально** проверяет то, что я хочу проверить.

**Дополнение (28.08.2026):** если ученик **уверенно утверждает** технически неверное — мягко поправить и **не соглашаться** под его давлением. Ученик сказал «@ComponentScan есть только в Spring Boot» — это неправда, но я поддался. На самом деле `@ComponentScan` — это Spring Core, Boot лишь использует его через `@SpringBootApplication`. Правило: **не идти на поводу у ученика** в технических вопросах, даже если он настаивает.

---

## 🛠 Правило работы с кодом в проекте

**Самый актуальный код — в файлах проекта, а не в чате.**

- Ассистент работает в той же IDE/рабочей директории, что и ученик (IntelliJ IDEA, корневая папка проекта).
- Если ученик говорит «посмотри код» / «проверь» / «так можно?» — **сначала открыть файлы** (`Get-ChildItem` + `read_files`), а не просить вставить код в чат.
- Если ученик говорит «я добавил/изменил X» — **проверить реальное состояние файлов**, прежде чем отвечать. Код мог не сохраниться, или ученик мог создать файл с содержимым старого (как было с `SmsChannel.java`, где остался текст `EmailChannel`).
- Если рассинхрон (файл создан, но внутри чужие имена / нет нужных правок) — **написать прямо, что не сходится**, а не угадывать.

**При новом открытии проекта:** после команды «прочитай дневник» — ассистент помнит это правило и не просит вставлять код.

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
| Имя бина = имя метода? | Да, для `@Bean`. Имя бина в контексте = имя метода (`emailChannel()` → `"emailChannel"`) |
| Несколько бинов одного типа — как достать? | `getBeansOfType(T.class)` → `Map<String, T>` (имя → бин). `getBeanNamesForType` — только имена |
| `@Value` с `:` vs без? | С `:` (`${key:default}`) — default подставляется. Без `:` — `IllegalArgumentException` при старте |
| Имя бина для `@Component`? | Имя класса с **маленькой буквы** (`EmailChannel` → `emailChannel`). Можно задать явно: `@Component("customName")` |
| `@Component` vs `@Bean` — результат? | Одинаковый. `getBeanDefinitionCount()` **не меняется**, меняется только способ регистрации |
| Когда `@Bean` всё ещё нужен? | Чужой класс из библиотеки, сложная логика создания, **несколько бинов одного типа** с разными параметрами |
| Где лежат `@PostConstruct`/`@PreDestroy`? | В пакете `jakarta.annotation` (JSR-250 стандарт, не Spring). Spring 6 перешёл с `javax.*` на `jakarta.*` |
| `@PostConstruct` vs `initMethod` в `@Bean`? | Обе — init-логика после конструктора. `@PostConstruct` для своих классов, `@Bean(initMethod=...)` для **чужих** классов |
| Порядок жизненного цикла? | `@Autowired`/инжект → конструктор → `BPP.before` → `@PostConstruct` → `BPP.after` → [бин готов] → `ctx.close()` → `@PreDestroy` |
| Что если контекст не закрыть? | `@PreDestroy` не сработает → утечка ресурсов (соединения, файлы, треды). Продакшен баг |
| Как задать scope? | `@Scope("prototype")` на `@Component` или `@Bean`. Дефолт — `singleton` |
| Prototype vs singleton — ключевое? | Singleton = 1 объект, `@PreDestroy` вызывается. Prototype = новый объект на каждый `getBean()`, `@PreDestroy` **НЕ вызывается** (Spring теряет ссылку) |
| Когда prototype нужен? | Stateful объекты: корзина юзера, транзакция, builder с состоянием. Stateless бины (наши каналы) — singleton |
| Что такое AOP-прокси? | Обёртка вокруг бина, которая вставляет свою логику **до и после** каждого метода. Spring подсовывает её через `BeanPostProcessor` — ты даже не знаешь, что работаешь с прокси |
| JDK Dynamic Proxy vs CGLIB | JDK — через интерфейсы (`$Proxy15` в имени). CGLIB — через наследование (`$$EnhancerByCGLIB$$`). Spring выбирает автоматически по наличию интерфейса |
| Что ломает CGLIB? | `final`-метод и `final`-класс. CGLIB создаёт подкласс и переопределяет методы — `final` запрещает это |
| Что ломает JDK Dynamic Proxy? | Ничего, кроме отсутствия интерфейса. `final`-метод не мешает, потому что прокси работает через интерфейс |
| `ProxyFactory` vs `Enhancer` | `ProxyFactory` (Spring AOP) — оборачивает **готовый** бин, не нужно передавать аргументы конструктора. `Enhancer` (голый CGLIB) — пытается создать объект сам, нужно передавать аргументы |
| `proxy.invokeSuper(obj, args)` vs `invocation.proceed()` | CGLIB: `invokeSuper` вызывает метод родителя через прокси. Spring AOP: `proceed()` — стандартный AOP Alliance метод |
| Что такое `@Transactional` под капотом? | Это AOP-прокси с `TransactionInterceptor` — открывает транзакцию до метода, коммитит после, откатывает при исключении |
| `${prop}` vs `#{expr}` | `${}` — placeholder из properties (строковая подстановка). `#{}` — SpEL-выражение (вычисляется в рантайме) |
| Можно ли `${}` внутри `#{}`? | Да. Spring сначала подставляет свойство, потом вычисляет SpEL. Пример: `#{'${prop}'.toUpperCase()}` |
| Можно ли наоборот? | Нет. Парсер `${}` не умеет вычислять SpEL — он ищет литеральное имя свойства |
| Что ломает конвертация типов в `@Value`? | Если свойство есть, но не конвертируется (например, `app.timeout=тридцать` в `int`) — `NumberFormatException` при старте |
| Дефолт через `@Value("${prop:default}")` | Срабатывает **только если свойство отсутствует**. Пустая строка — это **не** отсутствие, дефолт не применится |
| `@Value` на сеттер | Работает, но редко используется. Альтернативы: конструктор (immutable) или поле (мутабельно) |
| Когда падает ошибка конвертации в `@Value`? | **На старте контекста** (при создании бина). Не в компиляции и не при вызове метода. Ошибка: `BeanCreationException` → `Caused by: NumberFormatException` |
| Где Spring ищет properties? | **Spring Boot** — автоматически в `classpath:application.properties`. **Spring Core** — только если есть `@PropertySource` или настроен `Environment`. Без явного источника — свойства не подхватываются |
| Приоритет property sources | Spring перебирает список **с конца**. Чем **позже** объявлен `@PropertySource`, тем **выше** приоритет. Последний перекрывает первый |
| Что если файл в `@PropertySource` не найден? | По умолчанию — `IllegalArgumentException` при старте (`Could not open resource`). Чтобы сделать файл опциональным: `@PropertySource(value = "...", ignoreResourceNotFound = true)` |
| Чем `@ConfigurationProperties` лучше `@Value`? | Группировка полей в один класс, дефолты из Java-полей (не через `:default`), поддержка вложенных объектов и коллекций, можно подключить валидацию (`@Validated`), relaxed binding |
| Где живёт `@ConfigurationProperties`? | В **Spring Boot** (`spring-boot`), не в голом Spring Core. Без Boot нужен либо `spring-boot` в зависимостях, либо ручной биндинг через `Binder` |
| Как подключить `@ConfigurationProperties`? | `@EnableConfigurationProperties(FooProperties.class)` на `@Configuration` классе. Без Boot — без `@Component` на классе свойств |
| Что такое relaxed binding? | Spring матчит имена в разных стилях: `email.smtp-host` ↔ `smtpHost` (camelCase). `-`, `_`, регистр, точка-вложенность — всё взаимозаменяемо. `@Value` этой фичи **не имеет** |
| Что такое событие в Spring 4.2+? | **Любой POJO** — `extends ApplicationEvent` больше не обязательно. Spring резолвит listener по **типу параметра метода** |
| `@EventListener` синхронный? | **Да** — выполняется в том же потоке, что и `publishEvent(...)`. Исключение пробрасывается в publisher. Для async — отдельная настройка (`@Async` + `AsyncConfigurer`) |
| Порядок listeners | По умолчанию не гарантирован. Явно — через `@Order(1)`, `@Order(2)` на методах |
| `BeanPostProcessor` — что это? | Крючок в жизненном цикле бина. **Два метода:** `before` (после конструктора, до `@PostConstruct`) и `after` (после `@PostConstruct`). Нужно вернуть бин (или обёртку) |
| Как Spring узнаёт про BPP? | Любой бин, реализующий интерфейс `BeanPostProcessor`, **автоматически** регистрируется в конвейере |
| Что делает `LoggingBeanPostProcessor`? | Печатает `[BPP.before] beanName` и `[BPP.after] beanName` для каждого бина, созданного **после** самого BPP |
| Почему не 7 бинов видно в логе? | BPP регистрируется поздно. Бины, созданные до него (`propertySourcesPlaceholderConfigurer`), не попадают в лог |
| Где живёт `@ComponentScan`? | В **Spring Core** (`org.springframework.context.annotation`). Spring Boot лишь использует его через `@SpringBootApplication`. Не путать с `@ConfigurationProperties` — это разные вещи |

---

## ❓ Мини-вопросы для самопроверки (после шага 3)

Попробуй ответить сам себе или мне, прежде чем гуглить:

1. Почему `ctx.getBean("emailChannel", NotificationChannel.class)` отдаёт тот же объект при двух вызовах?
2. Что будет, если убрать `@ComponentScan` — бин `emailChannel` всё равно появится? Почему?
3. Если в `@Value` опечататься в имени ключа, программа упадёт или подставится default? *(подсказка: зависит от того, есть ли `:` в строке)*
4. `ctx.getBeanDefinitionCount()` возвращает 7, а не 1. Откуда взялись остальные 6?
5. Чем `getBeanNamesForType(T.class)` отличается от `getBeansOfType(T.class)`? Когда что использовать?
6. Если поменять имя метода `emailChannel()` на `mailChannel()` — `ctx.getBean("emailChannel", ...)` отдаст бин или упадёт? Почему?
7. Удали из `application.properties` ключ `app.channels.sms.gatewayUrl` — программа запустится или упадёт? А если опечататься в имени ключа (`app.chanels.sms.gatewayUrl`)? Объясни разницу.
