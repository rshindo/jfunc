# jfunc

A tiny, typed functional utilities library for Java. Currently provides a sealed `Option<T>` type (a `Maybe`-like container) with a minimal, Java-friendly API.

## Features
- `Option<T>` with `Some`/`None` states (sealed interface + records)
- Core ops: `map`, `flatMap`, `filter`, `ifPresent`
- Interop: `toOptional()`, `fromOptional(Optional)`, `stream()`
- Constructors: `some(T)`, `none()`, `ofNullable(T)`
- Clear null policy: `some(null)` throws; `map` returning `null` becomes `None`

## Requirements
- Java 21+ (project currently compiles and runs tests on 21)
- Maven (for building/testing)

## Install
Add the dependency to your build. Replace the version as appropriate.

Maven:

```xml
<dependency>
  <groupId>com.github.rshindo</groupId>
  <artifactId>jfunc</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Gradle (Kotlin DSL):

```kts
dependencies {
  implementation("com.github.rshindo:jfunc:0.0.1-SNAPSHOT")
}
```

## Quick Start
```java
import com.github.rshindo.jfunc.Option;

Option<Integer> a = Option.some(10);
Option<Integer> b = Option.none();

// Map: null result becomes None (like Optional.map)
Option<String> s = a.map(x -> x == 10 ? "ten" : null); // Some("ten")
Option<String> t = b.map(Object::toString);             // None

// FlatMap
Option<Integer> f = a.flatMap(x -> x % 2 == 0 ? Option.some(x / 2) : Option.none());

// Filter
Option<Integer> even = a.filter(x -> x % 2 == 0); // Some(10)
Option<Integer> odd  = a.filter(x -> x % 2 == 1); // None

// ifPresent
a.ifPresent(x -> System.out.println("value: " + x));

// ofNullable vs some
Option<String> n1 = Option.ofNullable(null);  // None
// Option.some(null); // throws IllegalArgumentException

// Optional interop
java.util.Optional<Integer> opt = a.toOptional();
Option<Integer> fromOpt = Option.fromOptional(opt);

// Stream interop (Java 9+)
int sum = a.stream().mapToInt(Integer::intValue).sum(); // 10 or 0
```

## Semantics & Design
- `Option` is a sealed interface with nested records `Some` and `None`.
- `some(T)` requires non-null and throws `IllegalArgumentException` for null.
- `ofNullable(T)` converts null to `None`, non-null to `Some(value)`.
- `map` returns `None` if the mapper returns `null` (aligns with `Optional.map`).
- `equals`/`hashCode` come from records; two `None` instances compare equal.
- `None` is not a public singleton; `Option.none()` creates a fresh instance (by design).

## Development
- Run tests: `mvn test`
- Test framework: JUnit Jupiter 5 via Maven Surefire
- See also: `AGENTS.md` for working guidelines
