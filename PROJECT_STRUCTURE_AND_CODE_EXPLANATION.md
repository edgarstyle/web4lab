# Структура проекта и объяснение кода

## Содержание
1. [Общая структура проекта](#общая-структура-проекта)
2. [Конфигурационные файлы](#конфигурационные-файлы)
3. [Java классы (Backend)](#java-классы-backend)
4. [Facelets шаблоны (Frontend)](#facelets-шаблоны-frontend)
5. [JavaScript и CSS](#javascript-и-css)
6. [Поток выполнения приложения](#поток-выполнения-приложения)

---

## Общая структура проекта

```
Web3/
├── build.gradle                    # Конфигурация сборки проекта
├── src/
│   ├── main/
│   │   ├── java/org/example/
│   │   │   ├── bean/              # Managed Beans (управляемые бины)
│   │   │   │   ├── ClockBean.java      # Бин для отображения времени
│   │   │   │   ├── PointBean.java      # Бин для обработки координат точки
│   │   │   │   └── SessionBean.java     # Бин для управления результатами в сессии
│   │   │   ├── entity/            # JPA Entity классы
│   │   │   │   └── Result.java         # Сущность для хранения результатов в БД
│   │   │   ├── exception/        # Кастомные исключения
│   │   │   │   ├── ApplicationException.java  # Базовое исключение
│   │   │   │   ├── DatabaseException.java     # Исключение для ошибок БД
│   │   │   │   └── ValidationException.java   # Исключение для ошибок валидации
│   │   │   ├── service/           # Сервисный слой
│   │   │   │   └── ResultService.java  # Сервис для работы с БД
│   │   │   └── util/              # Утилиты
│   │   │       ├── AreaChecker.java    # Проверка попадания точки в область
│   │   │       └── ErrorHandler.java   # Централизованная обработка ошибок
│   │   ├── resources/
│   │   │   └── META-INF/
│   │   │       └── persistence.xml     # Конфигурация JPA
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   ├── web.xml              # Конфигурация веб-приложения
│   │       │   ├── faces-config.xml     # Конфигурация JSF и навигация
│   │       │   └── beans.xml            # Конфигурация CDI
│   │       ├── resources/
│   │       │   ├── css/
│   │       │   │   └── styles.css       # Стили приложения
│   │       │   ├── images/
│   │       │   │   └── background.jpg   # Фоновое изображение
│   │       │   └── js/
│   │       │       └── canvas.js        # JavaScript для работы с графиком
│   │       ├── index.xhtml              # Стартовая страница
│   │       └── main.xhtml               # Основная страница приложения
│   └── test/                       # Тесты (не используется)
```

---

## Конфигурационные файлы

### build.gradle

**Назначение:** Конфигурация системы сборки Gradle для проекта.

```gradle
plugins {
    id 'java'    // Плагин для компиляции Java кода
    id 'war'     // Плагин для создания WAR архива (веб-приложение)
}
```
- `java` - добавляет задачи компиляции Java
- `war` - создает WAR файл для развертывания на сервере приложений

```gradle
group = 'org.example'
version = '1.0-SNAPSHOT'
```
- Идентификаторы проекта для Maven репозитория

```gradle
java {
    sourceCompatibility = '17'  // Версия исходного кода Java
    targetCompatibility = '17'  // Версия байт-кода для JVM
}
```
- Указывает, что проект использует Java 17

```gradle
dependencies {
    compileOnly 'jakarta.faces:jakarta.faces-api:3.0.0'
    // compileOnly - зависимость нужна только для компиляции, 
    // в runtime предоставляется WildFly
}
```
- `compileOnly` - зависимости, которые предоставляются сервером приложений в runtime
- `implementation` - зависимости, которые включаются в WAR файл

### web.xml

**Назначение:** Конфигурация веб-приложения (дескриптор развертывания).

```xml
<servlet>
    <servlet-name>Faces Servlet</servlet-name>
    <servlet-class>jakarta.faces.webapp.FacesServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>
```
- Регистрирует JSF сервлет для обработки XHTML страниц
- `load-on-startup="1"` - загружает сервлет при старте приложения

```xml
<servlet-mapping>
    <servlet-name>Faces Servlet</servlet-name>
    <url-pattern>*.xhtml</url-pattern>
</servlet-mapping>
```
- Все запросы к `.xhtml` файлам обрабатываются JSF сервлетом

```xml
<context-param>
    <param-name>jakarta.faces.PROJECT_STAGE</param-name>
    <param-value>Development</param-value>
</context-param>
```
- Режим разработки: показывает детальные сообщения об ошибках

```xml
<context-param>
    <param-name>jakarta.faces.FACELETS_REFRESH_PERIOD</param-name>
    <param-value>0</param-value>
</context-param>
```
- `0` - всегда перезагружать шаблоны (для разработки)

### faces-config.xml

**Назначение:** Конфигурация JSF и правила навигации.

```xml
<navigation-rule>
    <from-view-id>/index.xhtml</from-view-id>
    <navigation-case>
        <from-outcome>main</from-outcome>
        <to-view-id>/main.xhtml</to-view-id>
    </navigation-case>
</navigation-rule>
```
- Определяет: если метод возвращает `"main"`, перейти на `/main.xhtml`
- Используется в `h:link outcome="main"` и `return "main"` в бинах

### persistence.xml

**Назначение:** Конфигурация JPA (Java Persistence API) для работы с БД.

```xml
<persistence-unit name="web3PU" transaction-type="RESOURCE_LOCAL">
```
- `web3PU` - имя persistence unit (используется в коде)
- `RESOURCE_LOCAL` - управление транзакциями вручную (не через контейнер)

```xml
<provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>
```
- Указывает использовать EclipseLink как провайдер JPA

```xml
<property name="jakarta.persistence.jdbc.url" 
          value="jdbc:h2:mem:web3db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"/>
```
- `jdbc:h2:mem:web3db` - in-memory база данных H2
- `DB_CLOSE_DELAY=-1` - не закрывать БД при отключении последнего соединения
- `DB_CLOSE_ON_EXIT=FALSE` - не закрывать БД при выходе из приложения

```xml
<property name="eclipselink.ddl-generation" value="create-tables"/>
```
- Автоматически создавать таблицы при старте приложения

### beans.xml

**Назначение:** Активация CDI (Contexts and Dependency Injection).

```xml
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
       version="4.0"
       bean-discovery-mode="all">
```
- `bean-discovery-mode="all"` - обнаруживать все классы с CDI аннотациями
- Позволяет использовать `@Named`, `@Inject`, `@ApplicationScoped` и т.д.

---

## Java классы (Backend)

### Result.java (Entity)

**Назначение:** JPA Entity класс, представляет таблицу `results` в БД.

```java
@Entity
@Table(name = "results")
```
- `@Entity` - помечает класс как JPA сущность
- `@Table(name = "results")` - имя таблицы в БД

```java
@NamedQueries({
    @NamedQuery(name = "Result.findAll", 
                query = "SELECT r FROM Result r ORDER BY r.timestamp DESC")
})
```
- Предопределенный именованный запрос для получения всех результатов
- Используется в `ResultService.findAll()`

```java
@Id
@GeneratedValue(strategy = GenerationType.AUTO)
private Long id;
```
- `@Id` - первичный ключ
- `@GeneratedValue(strategy = GenerationType.AUTO)` - автоинкремент ID

```java
@Column(nullable = false, precision = 50, scale = 30)
private BigDecimal x;
```
- `nullable = false` - поле не может быть NULL
- `precision = 50, scale = 30` - для BigDecimal: 50 цифр всего, 30 после запятой
- Позволяет хранить очень длинные числа с высокой точностью

```java
@Column(nullable = false)
private LocalDateTime timestamp;
```
- `LocalDateTime` - дата и время проверки точки
- Автоматически устанавливается в конструкторе

### AreaChecker.java (Utility)

**Назначение:** Проверка попадания точки в область по варианту 18881.

```java
private static final MathContext MATH_CONTEXT = new MathContext(50, RoundingMode.HALF_UP);
```
- `MathContext` - контекст для вычислений с высокой точностью
- `50` - точность (50 значащих цифр)
- `HALF_UP` - округление: если >= 0.5, округляем вверх

```java
public static boolean checkHit(BigDecimal x, BigDecimal y, BigDecimal r)
```
- Статический метод - не требует создания объекта
- Использует `BigDecimal` для точных вычислений

```java
BigDecimal rHalf = r.divide(TWO, MATH_CONTEXT);
```
- Деление с использованием `MathContext` для сохранения точности
- `r.divide(TWO, MATH_CONTEXT)` эквивалентно `r / 2`

```java
if (x.compareTo(rHalf.negate()) >= 0 && x.compareTo(ZERO) <= 0 
        && y.compareTo(ZERO) >= 0 && y.compareTo(r) <= 0) {
    return true;
}
```
- `compareTo()` - сравнение BigDecimal (возвращает -1, 0, или 1)
- `x.compareTo(rHalf.negate()) >= 0` эквивалентно `x >= -R/2`
- `rHalf.negate()` - отрицательное значение `-R/2`

```java
BigDecimal boundary = r.subtract(x.multiply(TWO, MATH_CONTEXT), MATH_CONTEXT);
if (y.compareTo(boundary) <= 0) {
    return true;
}
```
- Вычисление границы треугольника: `y = R - 2x`
- `x.multiply(TWO, MATH_CONTEXT)` - умножение на 2
- `r.subtract(...)` - вычитание

```java
BigDecimal xSquared = x.multiply(x, MATH_CONTEXT);
BigDecimal ySquared = y.multiply(y, MATH_CONTEXT);
BigDecimal distanceSquared = xSquared.add(ySquared, MATH_CONTEXT);
BigDecimal rSquared = r.multiply(r, MATH_CONTEXT);
if (distanceSquared.compareTo(rSquared) <= 0) {
    return true;
}
```
- Проверка попадания в четверть круга: `x² + y² <= R²`
- Все операции с `MATH_CONTEXT` для сохранения точности

### PointBean.java (Managed Bean)

**Назначение:** View-scoped бин для обработки координат точки и валидации.

```java
@Named("pointBean")
@ViewScoped
public class PointBean implements Serializable
```
- `@Named("pointBean")` - регистрирует бин в CDI с именем `pointBean`
- Используется в XHTML как `#{pointBean.x}`
- `@ViewScoped` - бин живет пока открыта страница (сохраняет состояние между AJAX запросами)
- `Serializable` - требуется для ViewScoped бинов (сериализация состояния)

```java
@Inject
private SessionBean sessionBean;
```
- `@Inject` - внедрение зависимости через CDI
- WildFly автоматически создает и внедряет `SessionBean`

```java
private BigDecimal x;
private BigDecimal y;
private BigDecimal r = new BigDecimal("1.0");
```
- Свойства бина, связанные с формой через `value="#{pointBean.x}"`
- `r = new BigDecimal("1.0")` - значение по умолчанию

```java
public void setX(BigDecimal x) {
    if (x != null && (x.compareTo(MIN_X) < 0 || x.compareTo(MAX_X) > 0)) {
        ErrorHandler.handleValidationError("X должно быть в диапазоне от -5 до 3");
        return;
    }
    this.x = x;
}
```
- Сеттер с валидацией
- `ErrorHandler.handleValidationError()` - показывает ошибку пользователю через JSF

```java
public void setX(String xStr) {
    // ...
    BigDecimal xValue = new BigDecimal(xStr.trim());
}
```
- Дополнительный сеттер для String (JSF может передавать String из скрытых полей)
- `trim()` - убирает пробелы

```java
public String setXValue(Double value) {
    // ...
    return null; // Остаемся на той же странице
}
```
- Метод для `h:commandLink action="#{pointBean.setXValue(-4)}"`
- Возвращает `null` - остаемся на той же странице (ViewScoped сохраняет состояние)
- Возврат `"main"` - переход на main.xhtml (определено в faces-config.xml)

```java
public String checkPoint() {
    // Валидация
    if (x == null) {
        ErrorHandler.handleValidationError("...");
        return null;
    }
    
    // Проверка попадания
    long startTime = System.nanoTime();
    boolean hit = AreaChecker.checkHit(x, y, r);
    long executionTime = (System.nanoTime() - startTime) / 1000;
    
    // Сохранение результата
    Result result = new Result(x, y, r, hit);
    result.setExecutionTime(executionTime);
    sessionBean.addResult(result);
    
    return "main";
}
```
- `System.nanoTime()` - высокоточное время для измерения производительности
- `/ 1000` - преобразование наносекунд в микросекунды
- `return "main"` - навигация на main.xhtml (AJAX обновит только нужные части)

```java
public void validateY(FacesContext context, UIComponent component, Object value) 
        throws ValidatorException {
    // ...
    if (value instanceof BigDecimal) {
        yValue = (BigDecimal) value;
    } else if (value instanceof String) {
        yValue = new BigDecimal(strValue);
    }
    // ...
    if (yValue.compareTo(MIN_Y) < 0 || yValue.compareTo(MAX_Y) > 0) {
        throw new ValidatorException(new FacesMessage(...));
    }
}
```
- Кастомный валидатор для JSF
- `instanceof` - проверка типа (JSF может передавать разные типы)
- `ValidatorException` - исключение для валидации (JSF покажет сообщение)

### SessionBean.java (Managed Bean)

**Назначение:** Session-scoped бин для управления результатами в сессии пользователя.

```java
@Named("sessionBean")
@SessionScoped
public class SessionBean implements Serializable
```
- `@SessionScoped` - бин живет на протяжении всей сессии пользователя
- Один экземпляр на пользователя (сохраняет результаты между запросами)

```java
@Inject
private ResultService resultService;
```
- Внедрение сервиса для работы с БД

```java
@PostConstruct
public void init() {
    if (resultService != null) {
        loadResults();
    }
}
```
- `@PostConstruct` - метод вызывается после создания бина и внедрения зависимостей
- Загружает результаты из БД при создании сессии

```java
public void addResult(Result result) {
    resultService.save(result);  // Сохранить в БД
    loadResults();                // Перезагрузить список из БД
}
```
- Сохраняет результат в БД и обновляет список в памяти

```java
public void clearResults() {
    resultService.clearAll();  // Очистить БД
    loadResults();             // Обновить список (будет пустым)
}
```

### ResultService.java (Service)

**Назначение:** Сервис для работы с базой данных через JPA.

```java
@ApplicationScoped
public class ResultService {
```
- `@ApplicationScoped` - один экземпляр на все приложение
- Создается при старте приложения, уничтожается при остановке

```java
@PostConstruct
public void init() {
    emf = Persistence.createEntityManagerFactory("web3PU");
    entityManager = emf.createEntityManager();
}
```
- `Persistence.createEntityManagerFactory("web3PU")` - создает фабрику по имени из persistence.xml
- `emf.createEntityManager()` - создает EntityManager для работы с БД

```java
@PreDestroy
public void destroy() {
    entityManager.close();
    emf.close();
}
```
- `@PreDestroy` - вызывается перед уничтожением бина
- Закрывает соединения с БД

```java
public void save(Result result) {
    EntityTransaction tx = entityManager.getTransaction();
    try {
        if (!tx.isActive()) {
            tx.begin();  // Начать транзакцию
        }
        entityManager.persist(result);  // Сохранить в БД
        entityManager.flush();         // Принудительно выполнить SQL
        if (tx.isActive()) {
            tx.commit();  // Подтвердить изменения
        }
    } catch (PersistenceException e) {
        if (tx.isActive()) {
            tx.rollback();  // Откатить изменения при ошибке
        }
        throw new DatabaseException(...);
    }
}
```
- `RESOURCE_LOCAL` транзакции - управление вручную
- `persist()` - добавить объект в контекст персистентности
- `flush()` - выполнить SQL запросы немедленно
- `commit()` - подтвердить транзакцию
- `rollback()` - откатить при ошибке

```java
public List<Result> findAll() {
    TypedQuery<Result> query = entityManager.createNamedQuery("Result.findAll", Result.class);
    return query.getResultList();
}
```
- `createNamedQuery()` - использует именованный запрос из `@NamedQuery` в Result.java
- `getResultList()` - выполняет запрос и возвращает список

### ErrorHandler.java (Utility)

**Назначение:** Централизованная обработка ошибок.

```java
public static void handleError(Exception e, String defaultMessage) {
    FacesContext context = FacesContext.getCurrentInstance();
    // ...
    if (e instanceof ValidationException) {
        userMessage = e.getMessage();
        logLevel = Level.WARNING;
    } else if (e instanceof DatabaseException) {
        userMessage = e.getMessage();
        logLevel = Level.SEVERE;
    }
    // ...
    context.addMessage(null, new FacesMessage(
        FacesMessage.SEVERITY_ERROR, "Ошибка", userMessage));
}
```
- `FacesContext.getCurrentInstance()` - получить текущий JSF контекст
- `instanceof` - проверка типа исключения для выбора сообщения
- `context.addMessage(null, ...)` - добавить глобальное сообщение (показывается в `h:messages`)

---

## Facelets шаблоны (Frontend)

### index.xhtml

**Назначение:** Стартовая страница приложения.

```xml
<f:view>
```
- Обертка JSF для страницы (обязательна)

```xml
<h:head>
    <h:outputStylesheet library="css" name="styles.css"/>
</h:head>
```
- `h:outputStylesheet` - подключает CSS файл из `resources/css/styles.css`
- JSF автоматически генерирует правильный URL

```xml
<h:body class="style index-page" 
        style="background-image: url('#{resource['images/background.jpg']}'); ...">
```
- `#{resource['images/background.jpg']}` - JSF Expression Language (EL)
- Генерирует URL к ресурсу из `resources/images/background.jpg`

```xml
<h1>Павлов Эдгар P3216 12223</h1>
```
- Статический текст (ранее использовался `#{studentBean.fullName}`)

```xml
<div id="clock" class="clock">
    #{clockBean.currentDateTime}
</div>
```
- `#{clockBean.currentDateTime}` - вызывает метод `getCurrentDateTime()` в ClockBean
- JSF автоматически обновляет значение при рендеринге страницы

```xml
<h:link outcome="main" value="Перейти к приложению" styleClass="btn btn-primary"/>
```
- `h:link` - JSF компонент для ссылки (не отправляет форму)
- `outcome="main"` - использует навигационное правило из faces-config.xml
- `styleClass` - CSS класс (аналог `class` в HTML)

```javascript
function updateClock() {
    var clockElement = document.getElementById('clock');
    var now = new Date();
    // ...
    clockElement.textContent = day + '.' + month + '.' + year + ' ' + hours + ':' + minutes + ':' + seconds;
}
setInterval(updateClock, 6000);
```
- JavaScript функция для обновления времени на клиенте
- `setInterval(updateClock, 6000)` - вызывает функцию каждые 6000 мс (6 секунд)
- Обновляет DOM напрямую (не через JSF)

### main.xhtml

**Назначение:** Основная страница приложения с формой и графиком.

```xml
<canvas id="areaCanvas" width="500" height="500"></canvas>
```
- HTML5 Canvas для отрисовки графика
- JavaScript рисует на canvas через Canvas API

```xml
<h:form id="pointForm">
```
- JSF форма (отправляет данные на сервер)
- `id="pointForm"` - используется в JavaScript для доступа к элементам

```xml
<h:inputHidden id="xValue" value="#{pointBean.x}"/>
```
- Скрытое поле для X (заполняется JavaScript при клике на график)
- `value="#{pointBean.x}"` - двустороннее связывание с бином

```xml
<h:commandLink value="-4" action="#{pointBean.setXValue(-4)}" styleClass="x-link">
    <f:ajax execute="xValue" render="xSelectedValue pointFormMessages"/>
</h:commandLink>
```
- `h:commandLink` - ссылка, которая вызывает метод бина
- `action="#{pointBean.setXValue(-4)}"` - вызывает метод с параметром
- `f:ajax` - AJAX запрос (без перезагрузки страницы)
- `execute="xValue"` - отправляет только поле `xValue` на сервер
- `render="xSelectedValue pointFormMessages"` - обновляет только эти элементы после ответа

```xml
<h:inputText id="yValue" value="#{pointBean.y}" 
             validator="#{pointBean.validateY}"
             required="true"
             requiredMessage="Y обязательно для заполнения">
    <f:ajax event="blur" execute="@this" render="pointFormMessages yValue"/>
</h:inputText>
```
- `h:inputText` - текстовое поле ввода
- `validator="#{pointBean.validateY}"` - кастомный валидатор
- `required="true"` - поле обязательно для заполнения
- `f:ajax event="blur"` - AJAX при потере фокуса (когда пользователь уходит из поля)
- `execute="@this"` - отправить только это поле
- `render="pointFormMessages yValue"` - обновить сообщения об ошибках и само поле

```xml
<h:selectOneMenu id="rValue" value="#{pointBean.r}" validator="#{pointBean.validateR}">
    <f:selectItem itemValue="1" itemLabel="1"/>
    <f:ajax event="change" execute="@this" render="pointFormMessages rValue"/>
</h:selectOneMenu>
```
- `h:selectOneMenu` - выпадающий список
- `f:selectItem` - элемент списка
- `f:ajax event="change"` - AJAX при изменении выбора

```xml
<h:commandButton id="checkButton" value="Отправить" action="#{pointBean.checkPoint()}">
    <f:ajax execute="pointForm" render="resultsForm:resultsPanel pointFormMessages" 
            onevent="handleAjaxEvent"/>
</h:commandButton>
```
- `h:commandButton` - кнопка отправки формы
- `action="#{pointBean.checkPoint()}"` - вызывает метод при нажатии
- `execute="pointForm"` - отправить всю форму
- `render="resultsForm:resultsPanel pointFormMessages"` - обновить таблицу результатов и сообщения
- `onevent="handleAjaxEvent"` - JavaScript функция для обработки AJAX событий

```xml
<h:messages globalOnly="true" styleClass="error-message"/>
```
- `h:messages` - показывает все глобальные сообщения JSF
- `globalOnly="true"` - только глобальные (не привязанные к полю)

```xml
<h:dataTable value="#{sessionBean.results}" var="result" styleClass="results-table">
    <h:column>
        <f:facet name="header">X</f:facet>
        #{result.x}
    </h:column>
</h:dataTable>
```
- `h:dataTable` - таблица данных JSF
- `value="#{sessionBean.results}"` - список для отображения
- `var="result"` - переменная для текущей строки
- `f:facet name="header"` - заголовок колонки
- `#{result.x}` - значение свойства объекта

```xml
<h:commandButton value="Очистить результаты" action="#{sessionBean.clearResults()}">
    <f:ajax execute="@this" render="resultsPanel"/>
</h:commandButton>
```
- Кнопка очистки результатов
- `execute="@this"` - отправить только эту кнопку
- `render="resultsPanel"` - обновить таблицу результатов

---

## JavaScript и CSS

### canvas.js

**Назначение:** JavaScript для отрисовки графика и обработки кликов.

```javascript
var canvas;
var ctx;
var currentR = 1.0;
var pointsByRadius = {};
```
- Глобальные переменные для canvas и состояния
- `pointsByRadius` - объект для хранения точек по радиусам: `{"1.0": [{x, y, hit}, ...]}`

```javascript
function initCanvas() {
    canvas = document.getElementById('areaCanvas');
    if (!canvas) {
        return false;
    }
    ctx = canvas.getContext('2d');
    // ...
    return true;
}
```
- Инициализация canvas
- `getContext('2d')` - получение 2D контекста для рисования

```javascript
function drawArea() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    // ...
    var scale = 100 / currentR;
    // ...
    ctx.beginPath();
    ctx.moveTo(0, centerY);
    ctx.lineTo(canvas.width, centerY);
    ctx.stroke();
}
```
- `clearRect()` - очистка canvas
- `beginPath()` - начало нового пути для рисования
- `moveTo()` - переместить "перо" в точку
- `lineTo()` - нарисовать линию до точки
- `stroke()` - обвести путь (нарисовать контур)

```javascript
ctx.rect(centerX - (currentR / 2) * scale, centerY - currentR * scale, 
         (currentR / 2) * scale, currentR * scale);
ctx.fill();
ctx.stroke();
```
- `rect(x, y, width, height)` - прямоугольник
- `fill()` - залить фигуру цветом
- `stroke()` - обвести контур

```javascript
ctx.arc(centerX, centerY, currentR * scale, Math.PI / 2, Math.PI, false);
```
- `arc(x, y, radius, startAngle, endAngle, anticlockwise)` - дуга окружности
- `Math.PI / 2` - 90 градусов (начало)
- `Math.PI` - 180 градусов (конец)
- `false` - по часовой стрелке

```javascript
function handleCanvasClick(event) {
    canvas = document.getElementById('areaCanvas');
    if (!ctx) {
        ctx = canvas.getContext('2d');
    }
    
    var rect = canvas.getBoundingClientRect();
    var x = event.clientX - rect.left;
    var y = event.clientY - rect.top;
    
    var centerX = 250;
    var centerY = 250;
    var scale = 100 / currentR;
    
    var realX = (x - centerX) / scale;
    var realY = (centerY - y) / scale; // Инвертируем Y
    
    setYValue(realY);
    submitPoint(realX, realY);
}
```
- `getBoundingClientRect()` - возвращает позицию и размеры элемента относительно viewport
- `event.clientX` - координата X клика относительно viewport
- `event.clientY` - координата Y клика относительно viewport
- `rect.left` - позиция левого края canvas относительно viewport
- `x - centerX` - смещение от центра canvas
- `(x - centerX) / scale` - преобразование пикселей в реальные единицы
- `centerY - y` - инвертирование Y (в canvas Y растет вниз, в математике - вверх)
- `setYValue(realY)` - устанавливает Y в поле ввода
- `submitPoint(realX, realY)` - отправляет координаты на сервер

```javascript
function submitPoint(x, y) {
    var xStr = x.toString();
    var yStr = y.toString();
    
    var xInput = document.getElementById('pointForm:xValue');
    if (xInput) {
        xInput.value = xStr;
        
        var selectedText = document.getElementById('pointForm:xSelectedValue');
        if (selectedText) {
            var displayX = parseFloat(xStr).toFixed(10).replace(/\.?0+$/, '');
            selectedText.textContent = 'Выбрано: ' + displayX;
        }
        
        var xLinks = document.querySelectorAll('#xSelector a');
        xLinks.forEach(function(link) {
            link.classList.remove('selected');
        });
    }
    
    var yInput = document.getElementById('pointForm:yValue');
    if (yInput) {
        yInput.value = yStr;
    }
    
    setTimeout(function() {
        var checkButton = document.getElementById('pointForm:checkButton');
        if (checkButton) {
            checkButton.click();
        }
    }, 50);
}
```
- `x.toString()` - преобразование числа в строку (для сохранения точности BigDecimal)
- `document.getElementById('pointForm:xValue')` - поиск элемента по ID (JSF генерирует ID с префиксом формы)
- `xInput.value = xStr` - установка значения в скрытое поле
- `parseFloat(xStr).toFixed(10).replace(/\.?0+$/, '')` - форматирование для отображения (убирает лишние нули)
- `selectedText.textContent` - установка текста элемента
- `querySelectorAll('#xSelector a')` - поиск всех ссылок в селекторе X
- `classList.remove('selected')` - удаление класса выделения (X не из списка)
- `setTimeout(..., 50)` - задержка перед отправкой (гарантия установки значений)
- `checkButton.click()` - программный клик по кнопке (отправляет форму через JSF)

```javascript
function loadPointsFromTable() {
    var resultsTable = document.querySelector('.results-table');
    // ...
    for (var i = 1; i < resultsTable.rows.length; i++) {
        var row = resultsTable.rows[i];
        var x = parseFloat(cells[0].textContent.trim());
        // ...
        if (Math.abs(r - currentR) < 0.001) {
            pointsByRadius[rKey].push({x: x, y: y, hit: hit});
        }
    }
}
```
- Читает данные из HTML таблицы результатов
- Фильтрует точки по текущему радиусу
- Сохраняет в `pointsByRadius` для отрисовки

```javascript
function handleRadiusChange() {
    var rSelect = document.getElementById('pointForm:rValue');
    if (rSelect) {
        var newR = parseFloat(rSelect.value) || 1.0;
        currentR = newR;
        
        loadPointsFromTable();  // Загрузить точки для нового радиуса
        
        setTimeout(function() {
            if (initCanvas()) {
                drawArea();
            }
        }, 100);
    }
}
```
- Обработка смены радиуса
- `parseFloat(rSelect.value) || 1.0` - получить значение или использовать 1.0 по умолчанию
- `loadPointsFromTable()` - загружает точки из таблицы, фильтруя по новому радиусу
- `setTimeout(..., 100)` - задержка для гарантии обновления DOM после AJAX
- `initCanvas()` - переинициализация canvas
- `drawArea()` - перерисовка графика с новым масштабом

### main.xhtml (JavaScript часть)

```javascript
function showErrorInTooltip(errorText) {
    var tooltip = document.getElementById('tooltip');
    tooltip.textContent = errorText;
    tooltip.classList.add('show');
    tooltipTimer = setTimeout(function() {
        tooltip.classList.remove('show');
    }, 3000);
}
```
- Показывает ошибку в tooltip по центру экрана
- `classList.add('show')` - добавляет CSS класс для отображения
- `setTimeout()` - автоматически скрывает через 3 секунды

```javascript
function checkValidationErrors() {
    var yMessage = document.getElementById('pointForm:yValueMessage');
    var rMessage = document.getElementById('pointForm:rValueMessage');
    // ...
    if (errorText.trim() !== '') {
        showErrorInTooltip(errorText.trim());
    }
}
```
- Проверяет наличие ошибок валидации в DOM
- Показывает их в tooltip

```javascript
jsf.ajax.addOnEvent(function(data) {
    if (data.status === 'success') {
        setTimeout(function() {
            checkValidationErrors();
            redrawCanvas();
        }, 500);
    }
});
```
- Глобальный обработчик всех AJAX запросов JSF
- `data.status === 'success'` - успешный ответ
- `setTimeout()` - задержка для гарантии обновления DOM

```javascript
var lastResultsCount = 0;
setInterval(function() {
    var resultsTable = document.querySelector('.results-table');
    if (resultsTable && resultsTable.rows) {
        var currentCount = resultsTable.rows.length;
        if (currentCount !== lastResultsCount) {
            lastResultsCount = currentCount;
            if (typeof loadPointsFromTable === 'function') {
                loadPointsFromTable();
            }
            redrawCanvas();
        }
    }
}, 500);
```
- Polling (опрос) для отслеживания изменений таблицы
- `setInterval(function, 500)` - вызывает функцию каждые 500 мс
- `lastResultsCount` - хранит предыдущее количество строк
- `currentCount !== lastResultsCount` - проверка изменения
- Если изменилось - загружает точки и перерисовывает график
- Используется как fallback, если AJAX обновление не сработало

---

## Поток выполнения приложения

### 1. Загрузка страницы main.xhtml

1. JSF загружает XHTML шаблон
2. Создает/получает `PointBean` (ViewScoped) и `SessionBean` (SessionScoped)
3. `SessionBean.init()` загружает результаты из БД через `ResultService`
4. Рендерит HTML с данными из бинов
5. Браузер выполняет JavaScript:
   - `initCanvas()` - инициализирует canvas
   - `drawArea()` - рисует график
   - `loadPointsFromTable()` - загружает точки из таблицы
   - `setupCanvasClickHandler()` - устанавливает обработчик клика

### 2. Пользователь вводит координаты

**Вариант A: Клик на графике**
1. `handleCanvasClick(event)` - вычисляет координаты клика
2. Преобразует в реальные координаты (X, Y)
3. `submitPoint(x, y)` - устанавливает значения в скрытые поля
4. `checkButton.click()` - отправляет форму через AJAX

**Вариант B: Выбор X через кнопки**
1. Пользователь кликает `h:commandLink`
2. JSF вызывает `pointBean.setXValue(-4)` через AJAX
3. Бин валидирует значение
4. Обновляет `xSelectedValue` на странице (без перезагрузки)

**Вариант C: Ввод Y в текстовое поле**
1. Пользователь вводит значение и уходит из поля (blur)
2. JSF вызывает валидатор `pointBean.validateY()`
3. Если ошибка - показывает сообщение
4. JavaScript `checkValidationErrors()` показывает tooltip

### 3. Отправка формы (кнопка "Отправить")

1. `f:ajax` отправляет данные формы на сервер через AJAX
2. JSF вызывает `pointBean.checkPoint()`
3. Валидация полей (X, Y, R)
4. `AreaChecker.checkHit(x, y, r)` - проверка попадания
5. Создание `Result` объекта
6. `sessionBean.addResult(result)` - сохранение в БД
7. `ResultService.save(result)` - транзакция в БД
8. `sessionBean.loadResults()` - перезагрузка списка из БД
9. JSF возвращает ответ AJAX
10. JavaScript обновляет таблицу результатов и перерисовывает график

### 4. Смена радиуса

1. Пользователь выбирает новое значение R
2. `f:ajax event="change"` отправляет запрос
3. `pointBean.setR()` обновляет значение
4. JavaScript `handleRadiusChange()` вызывается
5. `loadPointsFromTable()` - загружает точки для нового радиуса
6. `drawArea()` - перерисовывает график с новым масштабом

---

## Ключевые концепции и методы

### JSF Expression Language (EL)

```xml
#{pointBean.x}
```
- Вызывает `pointBean.getX()` при чтении
- Вызывает `pointBean.setX(value)` при записи
- Автоматическое преобразование типов

### CDI Scopes (Области видимости)

- `@ApplicationScoped` - один экземпляр на все приложение (ClockBean, ResultService)
- `@SessionScoped` - один экземпляр на сессию пользователя (SessionBean)
- `@ViewScoped` - один экземпляр на страницу (PointBean, сохраняется между AJAX запросами)

### AJAX в JSF

```xml
<f:ajax execute="pointForm" render="resultsPanel"/>
```
- `execute` - какие компоненты отправить на сервер
- `render` - какие компоненты обновить после ответа
- Обновляет только указанные части страницы (без полной перезагрузки)

### JPA EntityManager

- `persist()` - добавить объект в контекст персистентности
- `flush()` - выполнить SQL запросы
- `createNamedQuery()` - выполнить именованный запрос
- `getResultList()` - получить результаты запроса

### BigDecimal для точности

- Используется вместо `double` для точных вычислений
- `compareTo()` вместо `==` для сравнения
- `MathContext` для операций с заданной точностью
- `precision` и `scale` в `@Column` для хранения в БД

---

### ClockBean.java (Managed Bean)

**Назначение:** Application-scoped бин для предоставления текущего времени.

```java
@Named("clockBean")
@ApplicationScoped
public class ClockBean implements Serializable
```
- `@ApplicationScoped` - один экземпляр на все приложение
- Используется на index.xhtml для отображения времени

```java
private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

public String getCurrentDateTime() {
    return LocalDateTime.now().format(FORMATTER);
}
```
- `DateTimeFormatter` - форматирование даты и времени
- `LocalDateTime.now()` - текущая дата и время
- `format()` - преобразование в строку по шаблону
- Используется в XHTML как `#{clockBean.currentDateTime}`

### ApplicationException.java, DatabaseException.java, ValidationException.java

**Назначение:** Иерархия кастомных исключений для типизированной обработки ошибок.

```java
public class ApplicationException extends RuntimeException {
    private final String userMessage;
    
    public ApplicationException(String userMessage, String technicalMessage, Throwable cause) {
        super(technicalMessage, cause);
        this.userMessage = userMessage;
    }
}
```
- Базовое исключение приложения
- Разделяет сообщения для пользователя и технические сообщения
- `userMessage` - понятное сообщение для пользователя
- `technicalMessage` - детальное сообщение для логирования

```java
public class ValidationException extends ApplicationException {
    public ValidationException(String message) {
        super(message);
    }
}
```
- Исключение для ошибок валидации
- Наследуется от `ApplicationException`

```java
public class DatabaseException extends ApplicationException {
    public DatabaseException(String message, Throwable cause) {
        super("Ошибка базы данных: " + message, cause);
    }
}
```
- Исключение для ошибок базы данных
- Автоматически добавляет префикс "Ошибка базы данных:"

### main.xhtml (JavaScript - продолжение)

```javascript
function redrawCanvas() {
    var canvasEl = document.getElementById('areaCanvas');
    if (canvasEl) {
        if (typeof loadPointsFromTable === 'function') {
            loadPointsFromTable();
        }
        if (typeof window.forceRedraw === 'function') {
            window.forceRedraw();
        }
    }
}
```
- Функция для перерисовки canvas после AJAX обновлений
- `typeof` - проверка существования функции (защита от ошибок)
- `window.forceRedraw` - функция из canvas.js (экспортирована в window)

```javascript
function autoHideErrors() {
    setTimeout(function() {
        var errorMessages = document.querySelectorAll('#pointFormMessages .error-message');
        errorMessages.forEach(function(msg) {
            var text = msg.textContent.trim();
            if (text !== '') {
                setTimeout(function() {
                    msg.style.opacity = '0';
                    setTimeout(function() {
                        msg.textContent = '';
                        msg.style.opacity = '1';
                    }, 500);
                }, 3000);
            }
        });
    }, 300);
}
```
- Автоматически скрывает сообщения об ошибках через 3 секунды
- `querySelectorAll()` - поиск всех элементов по CSS селектору
- `forEach()` - перебор элементов
- `style.opacity` - изменение прозрачности для анимации

```javascript
function handleAjaxEvent(data) {
    if (data.status === 'error') {
        handleAjaxError(data);
    } else if (data.status === 'success') {
        setTimeout(function() {
            checkValidationErrors();
        }, 500);
    }
}
```
- Обработчик AJAX событий для кнопки отправки
- `data.status` - статус AJAX запроса ('begin', 'success', 'error')
- `setTimeout()` - задержка для гарантии обновления DOM

```javascript
function setupFieldErrorHandlers() {
    var yInput = document.getElementById('pointForm:yValue');
    if (yInput) {
        yInput.addEventListener('input', function() {
            this.classList.remove('input-error');
        });
    }
}
```
- Устанавливает обработчики для удаления визуальных отметок об ошибках
- `addEventListener('input', ...)` - при вводе текста
- `classList.remove()` - удаляет CSS класс

```javascript
function setupRadiusChangeHandler() {
    var rSelect = document.getElementById('pointForm:rValue');
    if (rSelect) {
        rSelect.removeEventListener('change', handleRadiusChangeLocal);
        rSelect.addEventListener('change', handleRadiusChangeLocal);
    }
}

function handleRadiusChangeLocal() {
    if (typeof window.handleRadiusChange === 'function') {
        window.handleRadiusChange();
    } else if (typeof window.forceRedraw === 'function') {
        window.forceRedraw();
    }
}
```
- Устанавливает обработчик изменения радиуса
- `removeEventListener()` - удаляет старый обработчик (избегает дублирования при AJAX)
- `addEventListener('change', handleRadiusChangeLocal)` - обработчик изменения выбора
- `handleRadiusChangeLocal()` - локальная функция-обертка
- Вызывает `window.handleRadiusChange()` из canvas.js
- Fallback на `forceRedraw()` если функция не доступна

### canvas.js (продолжение)

```javascript
function drawTicks(centerX, centerY, scale) {
    var xTicks = [-r, -r/2, 0, r/2, r];
    xTicks.forEach(function(tick) {
        var x = centerX + tick * scale;
        ctx.beginPath();
        ctx.moveTo(x, centerY - 5);
        ctx.lineTo(x, centerY + 5);
        ctx.stroke();
        var label = tick === 0 ? '0' : (tick === r ? 'R' : ...);
        ctx.fillText(label, x - 8, centerY + 20);
    });
}
```
- Рисует засечки и подписи на осях координат
- `xTicks.forEach()` - перебор массива значений для засечек
- `ctx.fillText()` - рисует текст на canvas
- Тернарный оператор `? :` - условное выражение для выбора подписи

```javascript
function drawPoints(centerX, centerY, scale) {
    var rKey = currentR.toString();
    var points = pointsByRadius[rKey];
    
    if (points && points.length > 0) {
        points.forEach(function(point) {
            var canvasX = centerX + x * scale;
            var canvasY = centerY - y * scale;
            
            ctx.beginPath();
            ctx.arc(canvasX, canvasY, 4, 0, 2 * Math.PI);
            ctx.fillStyle = hit ? '#4caf50' : '#f44336';
            ctx.fill();
            ctx.stroke();
        });
    }
}
```
- Рисует точки из памяти для текущего радиуса
- `pointsByRadius[rKey]` - получает массив точек для радиуса
- Преобразует реальные координаты в координаты canvas
- `ctx.arc()` - рисует круг (точку)
- `fillStyle` - цвет заливки (зеленый для попадания, красный для непопадания)

```javascript
function setX(value) {
    var xInput = document.getElementById('pointForm:xValue');
    if (xInput) {
        xInput.value = value;
    }
    updateXSelection(value);
}
```
- Устанавливает значение X в скрытое поле
- `updateXSelection()` - обновляет визуальное выделение выбранной кнопки

```javascript
function updateXSelection(value) {
    var xLinks = document.querySelectorAll('#xSelector a, #xSelector .x-link');
    xLinks.forEach(function(link) {
        var linkText = link.textContent.trim();
        if (linkText == value.toString()) {
            link.classList.add('selected');
        } else {
            link.classList.remove('selected');
        }
    });
}
```
- Обновляет визуальное выделение выбранной кнопки X
- `querySelectorAll()` - находит все ссылки выбора X
- `classList.add('selected')` - добавляет CSS класс для выделения

```javascript
function forceRedraw() {
    loadPointsFromTable();
    canvas = null;
    ctx = null;
    if (initCanvas()) {
        drawArea();
        setupCanvasClickHandler();
        return true;
    }
    return false;
}
```
- Принудительная перерисовка canvas
- Сбрасывает canvas и ctx для полной переинициализации
- Используется после AJAX обновлений

```javascript
window.drawArea = drawArea;
window.handleCanvasClick = handleCanvasClick;
window.initCanvas = initCanvas;
window.setupCanvasClickHandler = setupCanvasClickHandler;
window.setX = setX;
window.updateXSelection = updateXSelection;
window.forceRedraw = forceRedraw;
window.handleRadiusChange = handleRadiusChange;
window.loadPointsFromTable = loadPointsFromTable;
```
- Экспорт функций в глобальную область видимости (window)
- Позволяет вызывать функции из других скриптов (main.xhtml)
- `window.functionName = functionName` - присваивание функции свойству window
- Используется в main.xhtml как `typeof window.forceRedraw === 'function'`

```javascript
document.addEventListener('DOMContentLoaded', function() {
    setTimeout(setupCanvasClickHandler, 100);
});

window.addEventListener('load', function() {
    setTimeout(setupCanvasClickHandler, 200);
});
```
- Установка обработчика клика при загрузке страницы
- `DOMContentLoaded` - событие когда DOM готов (раньше чем `load`)
- `load` - событие когда все ресурсы загружены
- `setTimeout()` - задержка для гарантии готовности элементов

### styles.css

**Назначение:** Стили для веб-приложения.

```css
body {
    font-family: "serif", Serif;
    background: white;
    min-height: 100vh;
}
```
- Базовые стили для body
- `min-height: 100vh` - минимальная высота = высота viewport (100% экрана)

```css
.container {
    display: flex;
    flex-direction: column;
    align-items: center;
}
```
- Flexbox для вертикального расположения элементов
- `align-items: center` - центрирование по горизонтали

```css
.input-block {
    background: cornsilk;
    border: 1px solid darkblue;
    padding: 15px;
    margin: 10px 0;
}
```
- Стили для блоков ввода (X, Y, R, кнопка)
- `cornsilk` - светло-желтый фон
- `darkblue` - темно-синяя рамка

```css
.tooltip {
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 10000;
    opacity: 0;
    visibility: hidden;
}

.tooltip.show {
    visibility: visible;
    opacity: 1;
}
```
- Стили для tooltip с ошибками
- `position: fixed` - фиксированная позиция относительно viewport
- `top: 50%; left: 50%` - позиция по центру
- `transform: translate(-50%, -50%)` - центрирование (смещение на половину размера)
- `z-index: 10000` - поверх всех элементов
- `opacity` и `visibility` - для анимации появления/исчезновения

```css
.results-table {
    width: 100%;
    border-collapse: collapse;
}

.results-table th {
    background: linear-gradient(to bottom, #4a90e2, #357abd);
    color: white;
}
```
- Стили для таблицы результатов
- `border-collapse: collapse` - объединение границ ячеек
- `linear-gradient()` - градиентный фон для заголовка

---

## Заключение

Проект использует архитектуру MVC (Model-View-Controller):
- **Model**: Entity классы (Result) и сервисы (ResultService)
- **View**: Facelets шаблоны (index.xhtml, main.xhtml) и JavaScript
- **Controller**: Managed Beans (PointBean, SessionBean)

Все взаимодействие происходит через JSF компоненты и AJAX для динамических обновлений без перезагрузки страницы.

### Ключевые технологии:

1. **JSF 3.0** - фреймворк для веб-приложений с компонентным подходом
2. **CDI** - внедрение зависимостей и управление жизненным циклом бинов
3. **JPA 3.1** - стандарт для работы с БД через ORM
4. **EclipseLink** - реализация JPA
5. **H2 Database** - in-memory база данных
6. **BigDecimal** - для точных вычислений с длинными числами
7. **HTML5 Canvas** - для отрисовки графика
8. **AJAX** - для динамических обновлений без перезагрузки страницы

### Паттерны проектирования:

- **MVC** - разделение на Model, View, Controller
- **Dependency Injection** - через `@Inject`
- **Service Layer** - отдельный слой для работы с БД
- **Exception Handling** - централизованная обработка ошибок
- **Named Queries** - предопределенные SQL запросы

