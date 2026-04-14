package com.github.rshindo.jfunc;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class OptionTest {

    @Test
    void map_onSome_transformsValue() {
        Option<Integer> some = Option.some(2);
        Option<Integer> mapped = some.map(x -> x + 3);

        assertEquals(Option.some(5), mapped);
    }

    @Test
    void map_onSome_nullResult_becomesNone() {
        Option<String> some = Option.some("x");
        Option<String> mapped = some.map(v -> null);
        assertEquals(Option.none(), mapped);
    }

    @Test
    void map_onNone_keepsNone() {
        Option<Integer> none = Option.none();
        Option<Integer> mapped = none.map(x -> x + 1);

        assertEquals(Option.none(), mapped);
    }

    @Test
    void flatMap_onSome_flattens() {
        Option<Integer> some = Option.some(10);
        Option<String> result = some.flatMap(x -> Option.some("v=" + x));

        assertEquals(Option.some("v=10"), result);
    }

    @Test
    void flatMap_onNone_keepsNone() {
        Option<Integer> none = Option.none();
        Option<String> result = none.flatMap(x -> Option.some("v=" + x));

        assertEquals(Option.none(), result);
    }

    @Test
    void ifPresent_runsOnlyForSome() {
        AtomicReference<Integer> ref = new AtomicReference<>();

        Option.some(7).ifPresent(ref::set);
        assertEquals(7, ref.get());

        ref.set(null);
        Option.<Integer>none().ifPresent(ref::set);
        assertNull(ref.get());
    }

    @Test
    void filter_onSome_true_keepsSome() {
        Option<Integer> some = Option.some(4);
        Option<Integer> filtered = some.filter(x -> x % 2 == 0);
        assertEquals(Option.some(4), filtered);
    }

    @Test
    void filter_onSome_false_becomesNone() {
        Option<Integer> some = Option.some(3);
        Option<Integer> filtered = some.filter(x -> x % 2 == 0);
        assertEquals(Option.none(), filtered);
    }

    @Test
    void filter_onNone_staysNone() {
        Option<Integer> none = Option.none();
        Option<Integer> filtered = none.filter(x -> true);
        assertEquals(Option.none(), filtered);
    }

    @Test
    void toOptional_convertsSomeAndNone() {
        assertEquals(Optional.of(42), Option.some(42).toOptional());
        assertEquals(Optional.empty(), Option.<Integer>none().toOptional());
    }

    @Test
    void fromOptional_convertsToOption() {
        assertEquals(Option.some("x"), Option.fromOptional(Optional.of("x")));
        assertEquals(Option.none(), Option.fromOptional(Optional.empty()));
    }

    @Test
    void ofNullable_mapsNullToNone_andNonNullToSome() {
        assertEquals(Option.none(), Option.ofNullable(null));
        assertEquals(Option.some("ok"), Option.ofNullable("ok"));
    }

    @Test
    void some_withNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Option.some(null));
    }

    @Test
    void stream_onSome_yieldsSingleElement() {
        assertEquals(List.of(99), Option.some(99).stream().toList());
    }

    @Test
    void stream_onNone_isEmpty() {
        assertEquals(List.of(), Option.<Integer>none().stream().toList());
    }

    @Test
    void sequence_onEmptyIterable_returnsSomeOfEmptyList() {
        Option<List<Integer>> sequenced = Option.sequence(List.of());

        assertEquals(Option.some(List.of()), sequenced);
    }

    @Test
    void sequence_onAllSome_collectsValues() {
        Option<List<Integer>> sequenced = Option.sequence(List.of(
                Option.some(1),
                Option.some(2),
                Option.some(3)
        ));

        assertEquals(Option.some(List.of(1, 2, 3)), sequenced);
    }

    @Test
    void sequence_onNone_returnsNone() {
        Option<List<Integer>> sequenced = Option.sequence(List.of(
                Option.some(1),
                Option.none(),
                Option.some(3)
        ));

        assertEquals(Option.none(), sequenced);
    }

    @Test
    void sequence_shortCircuitsAfterFirstNone() {
        AtomicReference<String> trace = new AtomicReference<>("start");
        Iterable<Option<Integer>> options = () -> List.<Option<Integer>>of(
                Option.some(1),
                Option.none(),
                Option.some(3)
        ).stream().map(option -> {
            if (option.equals(Option.some(3))) {
                trace.set("visited-third");
            }
            return option;
        }).iterator();

        Option<List<Integer>> sequenced = Option.sequence(options);

        assertEquals(Option.none(), sequenced);
        assertEquals("start", trace.get());
    }

    @Test
    void sequence_withNullIterable_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Option.<Integer>sequence(null));
    }

    @Test
    void sequence_withNullElement_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Option.sequence(java.util.Arrays.asList(
                Option.some(1),
                null
        )));
    }

    @Test
    void traverse_onEmptyIterable_returnsSomeOfEmptyList() {
        Option<List<Integer>> traversed = Option.traverse(List.<String>of(), value -> Option.some(value.length()));

        assertEquals(Option.some(List.of()), traversed);
    }

    @Test
    void traverse_onAllSome_collectsMappedValues() {
        Option<List<Integer>> traversed = Option.traverse(
                List.of("a", "bb", "ccc"),
                value -> Option.some(value.length())
        );

        assertEquals(Option.some(List.of(1, 2, 3)), traversed);
    }

    @Test
    void traverse_onNone_returnsNone() {
        Option<List<Integer>> traversed = Option.traverse(
                List.of("10", "oops", "30"),
                value -> value.matches("\\d+") ? Option.some(Integer.parseInt(value)) : Option.none()
        );

        assertEquals(Option.none(), traversed);
    }

    @Test
    void traverse_shortCircuitsAfterFirstNone() {
        AtomicReference<String> trace = new AtomicReference<>("start");

        Option<List<Integer>> traversed = Option.traverse(List.of("10", "oops", "30"), value -> {
            if (value.equals("oops")) {
                return Option.none();
            }
            if (value.equals("30")) {
                trace.set("visited-30");
            }
            return Option.some(Integer.parseInt(value));
        });

        assertEquals(Option.none(), traversed);
        assertEquals("start", trace.get());
    }

    @Test
    void traverse_withNullIterable_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Option.<String, Integer>traverse(null, value -> Option.some(value.length())));
    }

    @Test
    void traverse_withNullElement_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Option.traverse(
                java.util.Arrays.asList("ok", null),
                value -> Option.some(value.length())
        ));
    }

    @Test
    void traverse_withNullMapper_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Option.traverse(List.of("ok"), null));
    }

    @Test
    void traverse_whenMapperReturnsNull_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Option.traverse(List.of("ok"), value -> null));
    }
}
