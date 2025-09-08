package com.github.rshindo.jfunc;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class TryTest {

    @Test
    void of_success_and_failure() {
        Try<Integer> ok = Try.of(() -> 42);
        assertEquals(new Try.Success<>(42), ok);

        Try<Integer> ng = Try.of(() -> { throw new IOException("io"); });
        assertTrue(ng instanceof Try.Failure<?>);
        assertEquals("io", ((Try.Failure<?>) ng).error().getMessage());
    }

    @Test
    void map_onSuccess_transformsValue() {
        Try<Integer> t = Try.success(10);
        Try<String> mapped = t.map(x -> "v=" + x);
        assertEquals(new Try.Success<>("v=10"), mapped);
    }

    @Test
    void map_onFailure_keepsFailure() {
        Try<Integer> t = Try.failure(new IllegalStateException("x"));
        Try<String> mapped = t.map(x -> "v=" + x);
        assertTrue(mapped instanceof Try.Failure<?>);
        assertEquals("x", ((Try.Failure<?>) mapped).error().getMessage());
    }

    @Test
    void flatMap_onSuccess_and_onFailure() {
        Try<Integer> t = Try.success(5);
        Try<String> a = t.flatMap(x -> Try.success("x=" + x));
        assertEquals(new Try.Success<>("x=5"), a);

        Try<String> b = t.flatMap(x -> Try.failure(new RuntimeException("bad")));
        assertTrue(b instanceof Try.Failure<?>);
        assertEquals("bad", ((Try.Failure<?>) b).error().getMessage());
    }

    @Test
    void onSuccess_onFailure_sideEffects() {
        AtomicReference<String> ref = new AtomicReference<>();

        Try.success(7).onSuccess(v -> ref.set("OK" + v));
        assertEquals("OK7", ref.get());

        ref.set(null);
        Try.<Integer>failure(new RuntimeException("E")).onSuccess(v -> ref.set("OK" + v));
        assertNull(ref.get());

        Try.<Integer>failure(new RuntimeException("E")).onFailure(e -> ref.set(e.getMessage()));
        assertEquals("E", ref.get());
    }

    @Test
    void options_convertProperly() {
        assertEquals(Option.some(1), Try.success(1).toOptionSuccess());
        assertEquals(Option.none(), Try.success(1).toOptionFailure());

        Try<Integer> f = Try.failure(new IllegalArgumentException("x"));
        assertEquals(Option.none(), f.toOptionSuccess());
        assertEquals(Option.some(new IllegalArgumentException("x")).toString(), f.toOptionFailure().toString());
    }

    @Test
    void convert_to_either_and_result() {
        // Success case
        assertEquals(Either.right(42), Try.success(42).toEither());
        assertEquals(new Result.Success<Integer, Throwable>(42), Try.success(42).toResult());

        // Failure case
        Try<Integer> t = Try.failure(new RuntimeException("oops"));
        var e = t.toEither();
        assertTrue(e instanceof Either.Left<?, ?>);
        assertEquals(Option.some("oops"), e.toOptionLeft().map(Throwable::getMessage));

        var r = t.toResult();
        assertEquals(Option.some("oops"), r.toOptionFailure().map(Throwable::getMessage));
        assertEquals(Option.none(), r.toOptionSuccess());
    }

    @Test
    void factories_disallowNulls() {
        assertThrows(IllegalArgumentException.class, () -> Try.success(null));
        assertThrows(IllegalArgumentException.class, () -> Try.failure(null));
    }

    @Test
    void pattern_matching_switch_expression() {
        Try<Integer> a = Try.success(3);
        String sa = switch (a) {
            case Try.Success(var v) -> "OK:" + v;
            case Try.Failure(var e) -> "ERR:" + e.getMessage();
        };
        assertEquals("OK:3", sa);

        Try<Integer> b = Try.failure(new IOException("nope"));
        String sb = switch (b) {
            case Try.Success(var v) -> "OK:" + v;
            case Try.Failure(var e) -> "ERR:" + e.getMessage();
        };
        assertEquals("ERR:nope", sb);
    }

    @Test
    void run_executes_side_effect_and_yields_unit() {
        var ref = new java.util.concurrent.atomic.AtomicInteger();
        Try<Unit> t = Try.run(ref::incrementAndGet);
        // assert Success(Unit)
        String s = switch (t) {
            case Try.Success(var u) -> "OK";
            case Try.Failure(var e) -> "ERR";
        };
        assertEquals("OK", s);
        assertEquals(1, ref.get());
    }

    @Test
    void run_propagates_exceptions_as_failure() {
        Try<Unit> t = Try.run(() -> { throw new java.io.IOException("x"); });
        String s = switch (t) {
            case Try.Success(var u) -> "OK";
            case Try.Failure(var e) -> e.getClass().getSimpleName();
        };
        assertEquals("IOException", s);
    }
}
