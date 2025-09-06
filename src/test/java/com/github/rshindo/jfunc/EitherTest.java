package com.github.rshindo.jfunc;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class EitherTest {

    @Test
    void map_onRight_transformsValue() {
        Either<String, Integer> e = Either.right(10);
        Either<String, String> mapped = e.map(x -> "v=" + x);
        assertEquals(Either.right("v=10"), mapped);
    }

    @Test
    void map_onLeft_keepsLeft() {
        Either<String, Integer> e = Either.left("err");
        Either<String, String> mapped = e.map(x -> "v=" + x);
        assertEquals(Either.left("err"), mapped);
    }

    @Test
    void mapLeft_onLeft_transformsValue() {
        Either<Integer, String> e = Either.left(4);
        Either<String, String> mapped = e.mapLeft(Object::toString);
        assertEquals(Either.left("4"), mapped);
    }

    @Test
    void mapLeft_onRight_keepsRight() {
        Either<Integer, String> e = Either.right("ok");
        Either<String, String> mapped = e.mapLeft(Object::toString);
        assertEquals(Either.right("ok"), mapped);
    }

    @Test
    void flatMap_onRight_flattens() {
        Either<String, Integer> e = Either.right(7);
        Either<String, String> mapped = e.flatMap(v -> Either.right("x=" + v));
        assertEquals(Either.right("x=7"), mapped);
    }

    @Test
    void flatMap_onLeft_keepsLeft() {
        Either<String, Integer> e = Either.left("bad");
        Either<String, String> mapped = e.flatMap(v -> Either.right("x=" + v));
        assertEquals(Either.left("bad"), mapped);
    }

    @Test
    void ifRight_and_ifLeft_executeOnCorrectSide() {
        AtomicReference<String> ref = new AtomicReference<>();

        Either.<String, Integer>left("L").ifRight(v -> ref.set("R" + v));
        assertNull(ref.get());

        Either.<String, Integer>left("L").ifLeft(ref::set);
        assertEquals("L", ref.get());

        ref.set(null);
        Either.<String, Integer>right(5).ifRight(v -> ref.set("R" + v));
        assertEquals("R5", ref.get());
    }

    @Test
    void swap_exchangesSides() {
        assertEquals(Either.left("x"), Either.<Integer, String>right("x").swap());
        assertEquals(Either.right(1), Either.<Integer, String>left(1).swap());
    }

    @Test
    void toOptional_convertsEachSide() {
        assertEquals(Optional.of("r"), Either.<Integer, String>right("r").toOptionalRight());
        assertEquals(Optional.empty(), Either.<Integer, String>right("r").toOptionalLeft());
        assertEquals(Optional.of(3), Either.<Integer, String>left(3).toOptionalLeft());
        assertEquals(Optional.empty(), Either.<Integer, String>left(3).toOptionalRight());
    }

    @Test
    void constructors_disallowNullValues() {
        assertThrows(IllegalArgumentException.class, () -> Either.right(null));
        assertThrows(IllegalArgumentException.class, () -> Either.left(null));
    }
}
