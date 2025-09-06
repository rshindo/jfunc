package com.github.rshindo.jfunc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TupleTest {

    @Test
    void arity_matches_definition() {
        assertEquals(1, new Tuple.Tuple1<>("a").arity());
        assertEquals(2, new Tuple.Tuple2<>(1, 2).arity());
        assertEquals(3, new Tuple.Tuple3<>(1, 2, 3).arity());
        assertEquals(4, new Tuple.Tuple4<>(1, 2, 3, 4).arity());
        assertEquals(5, new Tuple.Tuple5<>(1, 2, 3, 4, 5).arity());
        assertEquals(6, new Tuple.Tuple6<>(1, 2, 3, 4, 5, 6).arity());
        assertEquals(7, new Tuple.Tuple7<>(1, 2, 3, 4, 5, 6, 7).arity());
        assertEquals(8, new Tuple.Tuple8<>(1, 2, 3, 4, 5, 6, 7, 8).arity());
        assertEquals(9, new Tuple.Tuple9<>(1, 2, 3, 4, 5, 6, 7, 8, 9).arity());
        assertEquals(10, new Tuple.Tuple10<>(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).arity());
    }

    @Test
    void record_pattern_matching_extracts_values() {
        Tuple t = Tuple.of("x", 42, true);
        String s = switch (t) {
            case Tuple.Tuple3(var a, var b, var c) -> a + ":" + b + ":" + c;
            default -> "other";
        };
        assertEquals("x:42:true", s);
    }

    @Test
    void factories_create_expected_records() {
        assertEquals(new Tuple.Tuple1<>("a"), Tuple.of("a"));
        assertEquals(new Tuple.Tuple2<>(1, 2), Tuple.of(1, 2));
        assertEquals(new Tuple.Tuple3<>(1, 2, 3), Tuple.of(1, 2, 3));
        assertEquals(new Tuple.Tuple4<>(1, 2, 3, 4), Tuple.of(1, 2, 3, 4));
        assertEquals(new Tuple.Tuple5<>(1, 2, 3, 4, 5), Tuple.of(1, 2, 3, 4, 5));
        assertEquals(new Tuple.Tuple6<>(1, 2, 3, 4, 5, 6), Tuple.of(1, 2, 3, 4, 5, 6));
        assertEquals(new Tuple.Tuple7<>(1, 2, 3, 4, 5, 6, 7), Tuple.of(1, 2, 3, 4, 5, 6, 7));
        assertEquals(new Tuple.Tuple8<>(1, 2, 3, 4, 5, 6, 7, 8), Tuple.of(1, 2, 3, 4, 5, 6, 7, 8));
        assertEquals(new Tuple.Tuple9<>(1, 2, 3, 4, 5, 6, 7, 8, 9), Tuple.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
        assertEquals(new Tuple.Tuple10<>(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), Tuple.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    void equality_uses_record_semantics() {
        assertEquals(new Tuple.Tuple2<>(1, "a"), new Tuple.Tuple2<>(1, "a"));
        assertNotEquals(new Tuple.Tuple2<>(1, "a"), new Tuple.Tuple2<>(1, "b"));
    }
}
