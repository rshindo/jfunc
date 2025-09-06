package com.github.rshindo.jfunc;

import org.junit.jupiter.api.Test;

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
    void toOption_convertsEachSide() {
        assertEquals(Option.some(1), Result.<Integer, String>success(1).toOptionSuccess());
        assertEquals(Option.none(), Result.<Integer, String>success(1).toOptionFailure());
        assertEquals(Option.some("x"), Result.<Integer, String>failure("x").toOptionFailure());
        assertEquals(Option.none(), Result.<Integer, String>failure("x").toOptionSuccess());
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
}
