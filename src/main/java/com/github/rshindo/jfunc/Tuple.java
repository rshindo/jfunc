package com.github.rshindo.jfunc;

/**
 * Tuple types for multiple values. Designed for use with record pattern matching.
 * <p>
 * Shape: {@code Tuple} is a plain interface (not sealed) so users may add their own arities
 * such as {@code Tuple11}. This file provides {@code Tuple1}..{@code Tuple10} as nested records.
 * </p>
 */
public interface Tuple {
    /** Returns the number of elements (arity). */
    int arity();

    // -- Factories ------------------------------------------------------------

    /**
     * Creates a 1-arity tuple.
     *
     * @param _1 the first element
     * @param <T1> type of the first element
     * @return a {@link Tuple1} holding {@code _1}
     */
    static <T1> Tuple1<T1> of(T1 _1) { return new Tuple1<>(_1); }

    /**
     * Creates a 2-arity tuple.
     *
     * @param _1 the first element
     * @param _2 the second element
     * @param <T1> type of the first element
     * @param <T2> type of the second element
     * @return a {@link Tuple2} holding {@code _1, _2}
     */
    static <T1, T2> Tuple2<T1, T2> of(T1 _1, T2 _2) { return new Tuple2<>(_1, _2); }

    /**
     * Creates a 3-arity tuple.
     *
     * @param _1 the first element
     * @param _2 the second element
     * @param _3 the third element
     * @param <T1> type of the first element
     * @param <T2> type of the second element
     * @param <T3> type of the third element
     * @return a {@link Tuple3} holding {@code _1, _2, _3}
     */
    static <T1, T2, T3> Tuple3<T1, T2, T3> of(T1 _1, T2 _2, T3 _3) { return new Tuple3<>(_1, _2, _3); }

    /**
     * Creates a 4-arity tuple.
     *
     * @param _1 the first element
     * @param _2 the second element
     * @param _3 the third element
     * @param _4 the fourth element
     * @param <T1> type of the first element
     * @param <T2> type of the second element
     * @param <T3> type of the third element
     * @param <T4> type of the fourth element
     * @return a {@link Tuple4} holding {@code _1, _2, _3, _4}
     */
    static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> of(T1 _1, T2 _2, T3 _3, T4 _4) {
        return new Tuple4<>(_1, _2, _3, _4);
    }

    /**
     * Creates a 5-arity tuple.
     *
     * @param _1 the first element
     * @param _2 the second element
     * @param _3 the third element
     * @param _4 the fourth element
     * @param _5 the fifth element
     * @param <T1> type of the first element
     * @param <T2> type of the second element
     * @param <T3> type of the third element
     * @param <T4> type of the fourth element
     * @param <T5> type of the fifth element
     * @return a {@link Tuple5} holding {@code _1, _2, _3, _4, _5}
     */
    static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> of(T1 _1, T2 _2, T3 _3, T4 _4, T5 _5) {
        return new Tuple5<>(_1, _2, _3, _4, _5);
    }

    /**
     * Creates a 6-arity tuple.
     *
     * @param _1 the first element
     * @param _2 the second element
     * @param _3 the third element
     * @param _4 the fourth element
     * @param _5 the fifth element
     * @param _6 the sixth element
     * @param <T1> type of the first element
     * @param <T2> type of the second element
     * @param <T3> type of the third element
     * @param <T4> type of the fourth element
     * @param <T5> type of the fifth element
     * @param <T6> type of the sixth element
     * @return a {@link Tuple6} holding {@code _1.._6}
     */
    static <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> of(
            T1 _1, T2 _2, T3 _3, T4 _4, T5 _5, T6 _6) {
        return new Tuple6<>(_1, _2, _3, _4, _5, _6);
    }

    /**
     * Creates a 7-arity tuple.
     *
     * @param _1 the first element
     * @param _2 the second element
     * @param _3 the third element
     * @param _4 the fourth element
     * @param _5 the fifth element
     * @param _6 the sixth element
     * @param _7 the seventh element
     * @param <T1> type of the first element
     * @param <T2> type of the second element
     * @param <T3> type of the third element
     * @param <T4> type of the fourth element
     * @param <T5> type of the fifth element
     * @param <T6> type of the sixth element
     * @param <T7> type of the seventh element
     * @return a {@link Tuple7} holding {@code _1.._7}
     */
    static <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> of(
            T1 _1, T2 _2, T3 _3, T4 _4, T5 _5, T6 _6, T7 _7) {
        return new Tuple7<>(_1, _2, _3, _4, _5, _6, _7);
    }

    /**
     * Creates a 8-arity tuple.
     *
     * @param _1 the first element
     * @param _2 the second element
     * @param _3 the third element
     * @param _4 the fourth element
     * @param _5 the fifth element
     * @param _6 the sixth element
     * @param _7 the seventh element
     * @param _8 the eighth element
     * @param <T1> type of the first element
     * @param <T2> type of the second element
     * @param <T3> type of the third element
     * @param <T4> type of the fourth element
     * @param <T5> type of the fifth element
     * @param <T6> type of the sixth element
     * @param <T7> type of the seventh element
     * @param <T8> type of the eighth element
     * @return a {@link Tuple8} holding {@code _1.._8}
     */
    static <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> of(
            T1 _1, T2 _2, T3 _3, T4 _4, T5 _5, T6 _6, T7 _7, T8 _8) {
        return new Tuple8<>(_1, _2, _3, _4, _5, _6, _7, _8);
    }

    /**
     * Creates a 9-arity tuple.
     *
     * @param _1 the first element
     * @param _2 the second element
     * @param _3 the third element
     * @param _4 the fourth element
     * @param _5 the fifth element
     * @param _6 the sixth element
     * @param _7 the seventh element
     * @param _8 the eighth element
     * @param _9 the ninth element
     * @param <T1> type of the first element
     * @param <T2> type of the second element
     * @param <T3> type of the third element
     * @param <T4> type of the fourth element
     * @param <T5> type of the fifth element
     * @param <T6> type of the sixth element
     * @param <T7> type of the seventh element
     * @param <T8> type of the eighth element
     * @param <T9> type of the ninth element
     * @return a {@link Tuple9} holding {@code _1.._9}
     */
    static <T1, T2, T3, T4, T5, T6, T7, T8, T9> Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9> of(
            T1 _1, T2 _2, T3 _3, T4 _4, T5 _5, T6 _6, T7 _7, T8 _8, T9 _9) {
        return new Tuple9<>(_1, _2, _3, _4, _5, _6, _7, _8, _9);
    }

    /**
     * Creates a 10-arity tuple.
     *
     * @param _1 the first element
     * @param _2 the second element
     * @param _3 the third element
     * @param _4 the fourth element
     * @param _5 the fifth element
     * @param _6 the sixth element
     * @param _7 the seventh element
     * @param _8 the eighth element
     * @param _9 the ninth element
     * @param _10 the tenth element
     * @param <T1> type of the first element
     * @param <T2> type of the second element
     * @param <T3> type of the third element
     * @param <T4> type of the fourth element
     * @param <T5> type of the fifth element
     * @param <T6> type of the sixth element
     * @param <T7> type of the seventh element
     * @param <T8> type of the eighth element
     * @param <T9> type of the ninth element
     * @param <T10> type of the tenth element
     * @return a {@link Tuple10} holding {@code _1.._10}
     */
    static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> of(
            T1 _1, T2 _2, T3 _3, T4 _4, T5 _5, T6 _6, T7 _7, T8 _8, T9 _9, T10 _10) {
        return new Tuple10<>(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10);
    }

    /** 1-arity tuple holding element {@code _1}. */
    record Tuple1<T1>(T1 _1) implements Tuple {
        @Override public int arity() { return 1; }
    }

    /** 2-arity tuple holding elements {@code _1}, {@code _2}. */
    record Tuple2<T1, T2>(T1 _1, T2 _2) implements Tuple {
        @Override public int arity() { return 2; }
    }

    /** 3-arity tuple holding elements {@code _1}, {@code _2}, {@code _3}. */
    record Tuple3<T1, T2, T3>(T1 _1, T2 _2, T3 _3) implements Tuple {
        @Override public int arity() { return 3; }
    }

    /** 4-arity tuple holding elements {@code _1}..{@code _4}. */
    record Tuple4<T1, T2, T3, T4>(T1 _1, T2 _2, T3 _3, T4 _4) implements Tuple {
        @Override public int arity() { return 4; }
    }

    /** 5-arity tuple holding elements {@code _1}..{@code _5}. */
    record Tuple5<T1, T2, T3, T4, T5>(T1 _1, T2 _2, T3 _3, T4 _4, T5 _5) implements Tuple {
        @Override public int arity() { return 5; }
    }

    /** 6-arity tuple holding elements {@code _1}..{@code _6}. */
    record Tuple6<T1, T2, T3, T4, T5, T6>(T1 _1, T2 _2, T3 _3, T4 _4, T5 _5, T6 _6) implements Tuple {
        @Override public int arity() { return 6; }
    }

    /** 7-arity tuple holding elements {@code _1}..{@code _7}. */
    record Tuple7<T1, T2, T3, T4, T5, T6, T7>(T1 _1, T2 _2, T3 _3, T4 _4, T5 _5, T6 _6, T7 _7) implements Tuple {
        @Override public int arity() { return 7; }
    }

    /** 8-arity tuple holding elements {@code _1}..{@code _8}. */
    record Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>(T1 _1, T2 _2, T3 _3, T4 _4, T5 _5, T6 _6, T7 _7, T8 _8) implements Tuple {
        @Override public int arity() { return 8; }
    }

    /** 9-arity tuple holding elements {@code _1}..{@code _9}. */
    record Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>(T1 _1, T2 _2, T3 _3, T4 _4, T5 _5, T6 _6, T7 _7, T8 _8, T9 _9) implements Tuple {
        @Override public int arity() { return 9; }
    }

    /** 10-arity tuple holding elements {@code _1}..{@code _10}. */
    record Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>(
            T1 _1, T2 _2, T3 _3, T4 _4, T5 _5, T6 _6, T7 _7, T8 _8, T9 _9, T10 _10
    ) implements Tuple {
        @Override public int arity() { return 10; }
    }
}
