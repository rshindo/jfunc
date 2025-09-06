# jfunc

A tiny, typed functional utilities library for Java. Sealed, Java‑friendly sum types with minimal APIs and pattern‑matching first design.

## Features
- `Option<T>`: `Some`/`None` (sealed interface + records)
  - Ops: `map`, `flatMap`, `filter`, `ifPresent`
  - Interop: `toOptional()`, `fromOptional(Optional)`, `stream()`
  - Constructors: `some(T)`, `none()`, `ofNullable(T)`
  - Null policy: `some(null)` throws; `map` returning `null` becomes `None`
- `Either<L,R>`: `Left`/`Right` disjoint union
  - Right‑biased: `map`, `flatMap` operate on `Right`; `mapLeft`/`ifLeft` for `Left`
  - Utilities: `swap()`, `toOptionalRight()`, `toOptionalLeft()`
- `Result<T,E>`: Success/Failure for Railway Oriented Programming (ROP)
  - Right‑biased: `map`, `flatMap` on `Success`; `mapFailure`/`onFailure` for failures
  - Interop: `toOptionalSuccess()`, `toOptionalFailure()`
  - Minimal API: prefer switch pattern matching over helpers

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
import com.github.rshindo.jfunc.Either;
import com.github.rshindo.jfunc.Result;

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

// Either (right-biased)
Either<String, Integer> e1 = Either.right(42);
Either<String, Integer> e2 = Either.left("oops");
Either<String, String> em = e1.map(x -> "v=" + x);      // Right("v=42")
Either<Integer, Integer> ml = e2.mapLeft(String::length); // Left(4)

// Result (ROP, success/failure)
Result<Integer, String> r1 = Result.success(7);
Result<Integer, String> r2 = Result.failure("invalid");
Result<String, String> r3 = r1.flatMap(x -> x % 2 == 1
        ? Result.failure("odd:" + x)
        : Result.success("even:" + x));

// Pattern matching (Java 21+)
String s1 = switch (e1) {
    case Either.Left(var l) -> "LEFT:" + l;
    case Either.Right(var r) -> "RIGHT:" + r;
};
String s2 = switch (r2) {
    case Result.Success(var v) -> "OK:" + v;
    case Result.Failure(var err) -> "ERR:" + err;
};
```

## Semantics & Design
- Sealed + nested records: `Option`, `Either`, and `Result` are sealed interfaces with nested record variants.
- Pattern‑matching first: prefer Java `switch`/type patterns; helper methods like `fold` are deliberately not included.
- Null policy:
  - `Option.some(null)` throws; use `ofNullable` to map `null` to `None`.
  - `Either` and `Result` disallow `null` in both variants; mappers must not return `null`.
- Bias:
  - `Either` and `Result` are right‑biased: `map`/`flatMap` act on `Right`/`Success`.
  - Use `mapLeft` (Either) or `mapFailure` (Result) for the left/failure path.
- Equality: record value equality; distinct `None` instances compare equal.
- No `None` singleton: `Option.none()` returns a new instance by design.

## Development
- Run tests: `mvn test`
- Test framework: JUnit Jupiter 5 via Maven Surefire
- See also: `AGENTS.md` for working guidelines and API design policies
