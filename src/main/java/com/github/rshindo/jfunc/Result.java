package com.github.rshindo.jfunc;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Result type for Railway Oriented Programming (ROP).
 * Represents either a successful value ({@link Success}) or a failure ({@link Failure}).
 * <p>
 * Design principles:
 * - Minimal API: prefer Java pattern matching over helpers; no fold-like methods are provided.
 * - Variant-oriented naming: {@link #ifSuccess(Consumer)} / {@link #ifFailure(Consumer)}.
 * - Nulls are not allowed for values or mapper results; factory methods reject null inputs.
 * </p>
 * <p>
 * Semantics:
 * - Right-biased operations: {@link #map(Function)} and {@link #flatMap(Function)} operate on {@code Success}
 *   and propagate {@code Failure} unchanged.
 * - Transform failures with {@link #mapFailure(Function)}.
 * - Convert to {@link Optional} via {@link #toOptionalSuccess()} / {@link #toOptionalFailure()}.
 * </p>
 *
 * @param <T> type of the success value
 * @param <E> type of the failure value
 */
public sealed interface Result<T, E> {

    /**
     * Creates a {@link Success} with the given non-null value.
     *
     * @param value the success value (must be non-null)
     * @param <T>   success type
     * @param <E>   failure type
     * @return a {@link Success}
     * @throws IllegalArgumentException if {@code value} is {@code null}
     */
    static <T, E> Result<T, E> success(T value) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        return new Success<>(value);
    }

    /**
     * Creates a {@link Failure} with the given non-null error value.
     *
     * @param error the failure value (must be non-null)
     * @param <T>   success type
     * @param <E>   failure type
     * @return a {@link Failure}
     * @throws IllegalArgumentException if {@code error} is {@code null}
     */
    static <T, E> Result<T, E> failure(E error) {
        if (error == null) {
            throw new IllegalArgumentException("error must not be null");
        }
        return new Failure<>(error);
    }

    /**
     * Maps the success value; a failure is propagated unchanged.
     *
     * @param mapper mapping function for the success value (must not return null)
     * @param <U>    mapped success type
     * @return mapped {@link Result}
     */
    <U> Result<U, E> map(Function<? super T, ? extends U> mapper);

    /**
     * Maps the failure value; a success is propagated unchanged.
     *
     * @param mapper mapping function for the failure value (must not return null)
     * @param <E2>   mapped failure type
     * @return mapped {@link Result}
     */
    <E2> Result<T, E2> mapFailure(Function<? super E, ? extends E2> mapper);

    /**
     * Flat-maps the success value; a failure is propagated unchanged.
     *
     * @param mapper mapping function returning a {@link Result}
     * @param <U>    mapped success type
     * @return mapped {@link Result}
     */
    <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> mapper);

    /** Executes the consumer only when this is a {@link Success}. */
    void ifSuccess(Consumer<? super T> consumer);

    /** Executes the consumer only when this is a {@link Failure}. */
    void ifFailure(Consumer<? super E> consumer);

    /** Converts the success value to {@link Optional}. */
    Optional<T> toOptionalSuccess();

    /** Converts the failure value to {@link Optional}. */
    Optional<E> toOptionalFailure();

    /** Variant representing success. Carries a non-null success value. */
    record Success<T, E>(T value) implements Result<T, E> {
        public Success {
            if (value == null) {
                throw new IllegalArgumentException("value must not be null");
            }
        }

        @Override
        public <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
            U mapped = mapper.apply(value);
            if (mapped == null) {
                throw new IllegalArgumentException("mapped ok value must not be null");
            }
            return new Success<>(mapped);
        }

        @Override
        public <E2> Result<T, E2> mapFailure(Function<? super E, ? extends E2> mapper) {
            return new Success<>(value);
        }

        @Override
        public <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> mapper) {
            return mapper.apply(value);
        }

        @Override
        public void ifSuccess(Consumer<? super T> consumer) {
            consumer.accept(value);
        }

        @Override
        public void ifFailure(Consumer<? super E> consumer) { /* no-op */ }

        @Override
        public Optional<T> toOptionalSuccess() { return Optional.of(value); }

        @Override
        public Optional<E> toOptionalFailure() { return Optional.empty(); }
    }

    /** Variant representing failure. Carries a non-null failure value. */
    record Failure<T, E>(E error) implements Result<T, E> {
        public Failure {
            if (error == null) {
                throw new IllegalArgumentException("error must not be null");
            }
        }

        @Override
        public <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
            return new Failure<>(error);
        }

        @Override
        public <E2> Result<T, E2> mapFailure(Function<? super E, ? extends E2> mapper) {
            E2 mapped = mapper.apply(error);
            if (mapped == null) {
                throw new IllegalArgumentException("mapped failure value must not be null");
            }
            return new Failure<>(mapped);
        }

        @Override
        public <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> mapper) {
            return new Failure<>(error);
        }

        @Override
        public void ifSuccess(Consumer<? super T> consumer) { /* no-op */ }

        @Override
        public void ifFailure(Consumer<? super E> consumer) {
            consumer.accept(error);
        }

        @Override
        public Optional<T> toOptionalSuccess() { return Optional.empty(); }

        @Override
        public Optional<E> toOptionalFailure() { return Optional.of(error); }
    }
}
