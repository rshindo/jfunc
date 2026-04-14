package com.github.rshindo.jfunc;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void map_onOk_transformsValue() {
        Result<Integer, String> r = Result.success(2);
        Result<String, String> mapped = r.map(x -> "v=" + (x + 1));
        assertEquals(new Result.Success<String, String>("v=3"), mapped);
    }

    @Test
    void map_onErr_keepsErr() {
        Result<Integer, String> r = Result.failure("bad");
        Result<String, String> mapped = r.map(x -> "v=" + x);
        assertEquals(new Result.Failure<String, String>("bad"), mapped);
    }

    @Test
    void mapFailure_onFailure_transformsError() {
        Result<Integer, String> r = Result.failure("e");
        Result<Integer, Integer> mapped = r.mapFailure(String::length);
        assertEquals(new Result.Failure<Integer, Integer>(1), mapped);
    }

    @Test
    void mapFailure_onSuccess_keepsSuccess() {
        Result<Integer, String> r = Result.success(5);
        Result<Integer, Integer> mapped = r.mapFailure(String::length);
        assertEquals(new Result.Success<Integer, Integer>(5), mapped);
    }

    @Test
    void flatMap_onOk_flattens() {
        Result<Integer, String> r = Result.success(10);
        Result<String, String> mapped = r.flatMap(x -> Result.success("x=" + x));
        assertEquals(new Result.Success<String, String>("x=10"), mapped);
    }

    @Test
    void flatMap_onErr_keepsErr() {
        Result<Integer, String> r = Result.failure("bad");
        Result<String, String> mapped = r.flatMap(x -> Result.success("x=" + x));
        assertEquals(new Result.Failure<String, String>("bad"), mapped);
    }

    @Test
    void onSuccess_and_onFailure_executeOnCorrectSide() {
        AtomicReference<String> ref = new AtomicReference<>();

        Result.<Integer, String>success(7).onSuccess(v -> ref.set("OK" + v));
        assertEquals("OK7", ref.get());

        ref.set(null);
        Result.<Integer, String>failure("E").onSuccess(v -> ref.set("OK" + v));
        assertNull(ref.get());

        Result.<Integer, String>failure("E").onFailure(ref::set);
        assertEquals("E", ref.get());
    }

    @Test
    void tee_onSuccess_runsSideEffect_and_returnsSameResult() {
        AtomicReference<String> ref = new AtomicReference<>();
        Result<Integer, String> result = Result.success(7);

        Result<Integer, String> returned = result.tee(v -> ref.set("OK" + v));

        assertEquals("OK7", ref.get());
        assertSame(result, returned);
    }

    @Test
    void tee_onFailure_skipsSideEffect_and_returnsSameResult() {
        AtomicReference<String> ref = new AtomicReference<>();
        Result<Integer, String> result = Result.failure("E");

        Result<Integer, String> returned = result.tee(v -> ref.set("OK" + v));

        assertNull(ref.get());
        assertSame(result, returned);
    }

    @Test
    void tee_allows_chaining_after_sideEffect() {
        Result<String, String> result = Result.<Integer, String>success(3)
                .tee(v -> { })
                .map(v -> "v=" + (v + 1));

        assertEquals(new Result.Success<String, String>("v=4"), result);
    }

    @Test
    void tee_disallowsNullConsumer_onBothSides() {
        assertThrows(NullPointerException.class, () -> Result.<Integer, String>success(1).tee(null));
        assertThrows(NullPointerException.class, () -> Result.<Integer, String>failure("E").tee(null));
    }

    @Test
    void toOption_convertsEachSide() {
        assertEquals(Option.some(1), Result.<Integer, String>success(1).toOptionSuccess());
        assertEquals(Option.none(), Result.<Integer, String>success(1).toOptionFailure());
        assertEquals(Option.some("x"), Result.<Integer, String>failure("x").toOptionFailure());
        assertEquals(Option.none(), Result.<Integer, String>failure("x").toOptionSuccess());
    }

    @Test
    void toEither_convertsEachSide() {
        assertEquals(Either.right(1), Result.<Integer, String>success(1).toEither());
        assertEquals(Either.left("x"), Result.<Integer, String>failure("x").toEither());
    }

    @Test
    void constructors_disallowNull() {
        assertThrows(IllegalArgumentException.class, () -> Result.success(null));
        assertThrows(IllegalArgumentException.class, () -> Result.failure(null));
    }

    @Test
    void rop_style_pipeline_with_flatMap() {
        // f: String -> Result<Integer, String>
        var f = (java.util.function.Function<String, Result<Integer, String>>) s ->
                (s == null || s.isBlank()) ? Result.failure("empty") : Result.success(s.length());

        // g: Integer -> Result<String, String>
        var g = (java.util.function.Function<Integer, Result<String, String>>) n ->
                (n % 2 == 0) ? Result.success("even:" + n) : Result.failure("odd:" + n);

        assertEquals(new Result.Success<String, String>("even:4"), f.apply("abcd").flatMap(g));
        assertEquals(new Result.Failure<String, String>("empty"), f.apply(" ").flatMap(g));
        assertEquals(new Result.Failure<String, String>("odd:3"), f.apply("abc").flatMap(g));
}

    @Test
    void pattern_matching_switch_expression() {
        Result<Integer, String> a = Result.success(3);
        String sa = switch (a) {
            case Result.Success(var v) -> "OK:" + v;
            case Result.Failure(var e) -> "ERR:" + e;
        };
        assertEquals("OK:3", sa);

        Result<Integer, String> b = Result.failure("nope");
        String sb = switch (b) {
            case Result.Success(var v) -> "OK:" + v;
            case Result.Failure(var e) -> "ERR:" + e;
        };
        assertEquals("ERR:nope", sb);
    }

    @Test
    void sequence_onEmptyIterable_returnsSuccessOfEmptyList() {
        Result<List<Integer>, String> sequenced = Result.sequence(List.of());

        assertEquals(Result.success(List.of()), sequenced);
    }

    @Test
    void sequence_onAllSuccess_collectsValues() {
        Result<List<Integer>, String> sequenced = Result.sequence(List.of(
                Result.success(1),
                Result.success(2),
                Result.success(3)
        ));

        assertEquals(Result.success(List.of(1, 2, 3)), sequenced);
    }

    @Test
    void sequence_onFirstFailure_returnsThatFailure() {
        Result<List<Integer>, String> sequenced = Result.sequence(List.of(
                Result.success(1),
                Result.failure("bad"),
                Result.failure("later")
        ));

        assertEquals(Result.failure("bad"), sequenced);
    }

    @Test
    void sequence_shortCircuitsAfterFirstFailure() {
        AtomicReference<String> trace = new AtomicReference<>("start");
        Iterable<Result<Integer, String>> results = () -> List.<Result<Integer, String>>of(
                Result.success(1),
                Result.failure("bad"),
                Result.success(3)
        ).stream().map(result -> {
            if (result.equals(Result.success(3))) {
                trace.set("visited-third");
            }
            return result;
        }).iterator();

        Result<List<Integer>, String> sequenced = Result.sequence(results);

        assertEquals(Result.failure("bad"), sequenced);
        assertEquals("start", trace.get());
    }

    @Test
    void sequence_withNullIterable_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Result.<Integer, String>sequence(null));
    }

    @Test
    void sequence_withNullElement_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Result.sequence(java.util.Arrays.asList(
                Result.success(1),
                null
        )));
    }

    @Test
    void traverse_onEmptyIterable_returnsSuccessOfEmptyList() {
        Result<List<Integer>, String> traversed = Result.traverse(List.<String>of(), value -> Result.success(value.length()));

        assertEquals(Result.success(List.of()), traversed);
    }

    @Test
    void traverse_onAllSuccess_collectsMappedValues() {
        Result<List<Integer>, String> traversed = Result.traverse(
                List.of("a", "bb", "ccc"),
                value -> Result.success(value.length())
        );

        assertEquals(Result.success(List.of(1, 2, 3)), traversed);
    }

    @Test
    void traverse_onFirstFailure_returnsThatFailure() {
        Result<List<Integer>, String> traversed = Result.traverse(
                List.of("10", "oops", "30"),
                value -> value.matches("\\d+") ? Result.success(Integer.parseInt(value)) : Result.failure("not a number: " + value)
        );

        assertEquals(Result.failure("not a number: oops"), traversed);
    }

    @Test
    void traverse_shortCircuitsAfterFirstFailure() {
        AtomicReference<String> trace = new AtomicReference<>("start");

        Result<List<Integer>, String> traversed = Result.traverse(List.of("10", "oops", "30"), value -> {
            if (value.equals("oops")) {
                return Result.failure("not a number: " + value);
            }
            if (value.equals("30")) {
                trace.set("visited-30");
            }
            return Result.success(Integer.parseInt(value));
        });

        assertEquals(Result.failure("not a number: oops"), traversed);
        assertEquals("start", trace.get());
    }

    @Test
    void traverse_withNullIterable_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Result.<String, Integer, String>traverse(null, value -> Result.success(value.length())));
    }

    @Test
    void traverse_withNullElement_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Result.traverse(
                java.util.Arrays.asList("ok", null),
                value -> Result.success(value.length())
        ));
    }

    @Test
    void traverse_withNullMapper_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Result.traverse(List.of("ok"), null));
    }

    @Test
    void traverse_whenMapperReturnsNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Result.traverse(List.of("ok"), value -> null));
    }
}
