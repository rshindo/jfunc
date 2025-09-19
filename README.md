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
  - Utilities: `swap()`, `toOptionRight()`, `toOptionLeft()`
- `Result<T,E>`: Success/Failure for Railway Oriented Programming (ROP)
  - Right‑biased: `map`, `flatMap` on `Success`; `mapFailure`/`onFailure` for failures
  - Interop: `toOptionSuccess()`, `toOptionFailure()`
  - Minimal API: prefer switch pattern matching over helpers
- `Try<T>`: Success/Failure for computations that may throw
  - Construct: `Try.of(CheckedSupplier)` to capture exceptions as `Failure`
  - Right‑biased: `map`, `flatMap` on `Success`; side effects via `onSuccess`/`onFailure`
  - Interop: `toOptionSuccess()`, `toOptionFailure()`, `toEither()`, `toResult()`

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
  <version>0.0.1</version>
</dependency>
```

Gradle (Kotlin DSL):

```kts
dependencies {
  implementation("com.github.rshindo:jfunc:0.0.1-SNAPSHOT")
}
```

## Quick Start

### Tuple
```java
import com.github.rshindo.jfunc.Tuple;

// Construct via factories
Tuple.Tuple2<String, Integer> p = Tuple.of("id", 42);
Tuple t = Tuple.of("x", 1, true);

// Pattern matching (record patterns)
String desc = switch (t) {
    case Tuple.Tuple3(var a, var b, var c) -> a + ":" + b + ":" + c;
    default -> "other";
};

int arity = t.arity(); // 3
```

### Option
```java
import com.github.rshindo.jfunc.Option;

Option<Integer> a = Option.some(10);
Option<Integer> b = Option.none();

// Map / FlatMap / Filter
Option<String> ms = a.map(x -> x == 10 ? "ten" : null); // Some("ten")
Option<Integer> mf = a.flatMap(x -> x % 2 == 0 ? Option.some(x / 2) : Option.none());
Option<Integer> fl = a.filter(x -> x % 2 == 0); // Some(10)

// Pattern matching (Java 21+)
String label = switch (a) {
    case Option.Some(var v) -> "SOME:" + v;
    case Option.None()      -> "NONE";
};

// Interop
java.util.Optional<Integer> opt = a.toOptional();
Option<Integer> fromOpt = Option.fromOptional(opt);
```

### Either
```java
import com.github.rshindo.jfunc.Either;

Either<String, Integer> e = Math.random() > 0.5 ? Either.right(42) : Either.left("oops");

// Right-biased ops
Either<String, String> em = e.map(x -> "v=" + x);
Either<Integer, Integer> ml = e.mapLeft(String::length);

// Pattern matching
String lab = switch (e) {
    case Either.Right(var r) -> "RIGHT:" + r;
    case Either.Left(var l)  -> "LEFT:" + l;
};

// Option conversions
Option<Integer> rightOpt = e.toOptionRight();
Option<String>  leftOpt  = e.toOptionLeft();
```

### Result (ROP)
```java
import com.github.rshindo.jfunc.Result;
import com.github.rshindo.jfunc.Option;
import java.util.function.Function;

// Step functions (String -> Result<...>)
Function<String, Result<String, String>> notBlank = s ->
        (s == null || s.isBlank()) ? Result.failure("empty") : Result.success(s.trim());

Function<String, Result<Integer, String>> parseInt = s -> {
    try { return Result.success(Integer.parseInt(s)); }
    catch (NumberFormatException e) { return Result.failure("nan:" + s); }
};

Function<Integer, Result<Integer, String>> evenOnly = n ->
        (n % 2 == 0) ? Result.success(n) : Result.failure("odd:" + n);

// Pipeline
String input = "42";
Result<Integer, String> res = Result.success(input)
        .flatMap(notBlank)
        .flatMap(parseInt)
        .flatMap(evenOnly);

// Pattern matching
String msg = switch (res) {
    case Result.Success(var v) -> "OK:" + v;
    case Result.Failure(var e) -> "ERR:" + e;
};

// Option conversions
Option<Integer> okOpt  = res.toOptionSuccess();
Option<String>  errOpt = res.toOptionFailure();
```

### Try
```java
import com.github.rshindo.jfunc.Try;
import com.github.rshindo.jfunc.Unit;

Try<Integer> t = Try.of(() -> Integer.parseInt("123"));
Try<Integer> u = Try.of(() -> Integer.parseInt("not-a-number"));

// Map/flatMap
Try<String> tm = t.map(x -> "v=" + x);

// Pattern matching
String tl = switch (u) {
    case Try.Success(var v) -> "OK:" + v;
    case Try.Failure(var e) -> "ERR:" + e.getMessage();
};

// Conversions
Either<Throwable,Integer> te = t.toEither();
Result<Integer,Throwable> tr = u.toResult();
Option<Integer> tOk = t.toOptionSuccess();
Option<Throwable> tNg = u.toOptionFailure();

// Void-like side effects via Unit
Try<Unit> run = Try.run(() -> doSideEffect());
switch (run) {
    case Try.Success(var u2) -> System.out.println("done");
    case Try.Failure(var e2) -> System.err.println("failed: " + e2.getMessage());
}
```

Hint: Try.of + try-with-resources
```java
import com.github.rshindo.jfunc.Try;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

// Read first line from a file
Try<String> firstLine = Try.of(() -> {
    try (var br = Files.newBufferedReader(Path.of("data.txt"), StandardCharsets.UTF_8)) {
        return br.readLine(); // null -> Failure via Try.success(null) rule
    }
});

String msg = switch (firstLine) {
    case Try.Success(var v) -> "OK: " + v;
    case Try.Failure(var e) -> "ERR: " + e.getMessage();
};

// Count lines with multiple resources
Try<Long> count = Try.of(() -> {
    try (var in = Files.newInputStream(Path.of("data.txt"));
         var br = new java.io.BufferedReader(new java.io.InputStreamReader(in, StandardCharsets.UTF_8))) {
        return br.lines().count();
    }
});

count.onSuccess(c -> System.out.println("lines: " + c))
     .onFailure(e -> System.err.println("read failed: " + e.getMessage()));
```

## Semantics & Design
- Sealed + nested records: `Option`, `Either`, and `Result` are sealed interfaces with nested record variants.
- Pattern‑matching first: prefer Java `switch`/type patterns; helper methods like `fold` are deliberately not included.
- Null policy:
  - `Option.some(null)` throws; use `ofNullable` to map `null` to `None`.
  - `Either` and `Result` disallow `null` in both variants; mappers must not return `null`.
  - `Try` disallows `null` success values; mappers must not return `null`. Failures carry a non-null `Throwable`.
- Bias:
  - `Either` and `Result` are right‑biased: `map`/`flatMap` act on `Right`/`Success`.
  - Use `mapLeft` (Either) or `mapFailure` (Result) for the left/failure path.
  - `Try` is right‑biased: `map`/`flatMap` act on `Success`; use `onFailure` for side-effects.
- Equality: record value equality; distinct `None` instances compare equal.
- No `None` singleton: `Option.none()` returns a new instance by design.

## Development
- Run tests: `mvn test`
- Test framework: JUnit Jupiter 5 via Maven Surefire
- See also: `AGENTS.md` for working guidelines and API design policies
