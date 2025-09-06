package com.github.rshindo.jfunc;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Try represents the result of a computation that may succeed with a value ({@link Success})
 * or fail with a {@link Throwable} ({@link Failure}).
 * <p>
 * Design principles:
 * - Minimal API: prefer Java pattern matching to helper methods (no fold-like methods).
 * - Right-biased: operations act on {@link Success}; failures propagate unchanged.
 * - Null policy: values must be non-null; factory methods reject null inputs.
 * </p>
 *
 * @param <T> success value type
 */
public sealed interface Try<T> {

    /** Functional interface that allows throwing checked exceptions. */
    @FunctionalInterface
    interface CheckedSupplier<T> {
        /**
         * Supplies a value and may throw a checked exception.
         *
         * @return supplied value
         * @throws Exception if a computation error occurs
         */
        T get() throws Exception;
    }

    /**
     * Creates a {@link Success} with the given non-null value.
     *
     * @param value the success value (must be non-null)
     * @param <T>   success type
     * @return a {@link Success} containing {@code value}
     * @throws IllegalArgumentException if {@code value} is {@code null}
     */
    static <T> Try<T> success(T value) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        return new Success<>(value);
    }

    /**
     * Creates a {@link Failure} with the given non-null {@link Throwable}.
     *
     * @param error the failure cause (must be non-null)
     * @param <T>   success type
     * @return a {@link Failure} containing {@code error}
     * @throws IllegalArgumentException if {@code error} is {@code null}
     */
    static <T> Try<T> failure(Throwable error) {
        if (error == null) {
            throw new IllegalArgumentException("error must not be null");
        }
        return new Failure<>(error);
    }

    /**
     * Executes the supplier and captures thrown exceptions as {@link Failure};
     * a non-null value becomes {@link Success}.
     *
     * @param supplier computation that may throw
     * @param <T>      success type
     * @return {@link Success} when the supplier returns normally; otherwise {@link Failure}
     * @throws IllegalArgumentException if {@code supplier} is {@code null}
     */
    static <T> Try<T> of(CheckedSupplier<? extends T> supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("supplier must not be null");
        }
        try {
            T v = supplier.get();
            return Try.success(v);
        } catch (Throwable t) {
            return Try.failure(t);
        }
    }

    /**
     * Maps the success value; failures propagate unchanged.
     *
     * @param mapper mapping function for the success value; must not return {@code null}
     * @param <U>    mapped success type
     * @return mapped {@link Try}
     * @throws IllegalArgumentException if this is {@link Success} and the mapper returns {@code null}
     * @throws NullPointerException     if {@code mapper} is {@code null}
     */
    <U> Try<U> map(Function<? super T, ? extends U> mapper);

    /**
     * Flat-maps the success value; failures propagate unchanged.
     *
     * @param mapper mapping function returning a {@link Try}
     * @param <U>    mapped success type
     * @return mapped {@link Try}
     * @throws NullPointerException if {@code mapper} is {@code null}
     */
    <U> Try<U> flatMap(Function<? super T, Try<U>> mapper);

    /**
     * Executes the consumer when this is a {@link Success}.
     *
     * @param consumer side-effect to execute with the success value
     * @throws NullPointerException if {@code consumer} is {@code null}
     */
    void onSuccess(Consumer<? super T> consumer);

    /**
     * Executes the consumer when this is a {@link Failure}.
     *
     * @param consumer side-effect to execute with the failure {@link Throwable}
     * @throws NullPointerException if {@code consumer} is {@code null}
     */
    void onFailure(Consumer<? super Throwable> consumer);

    /**
     * Converts the success value to {@link Optional}.
     *
     * @return {@code Optional.of(value)} for {@link Success}; otherwise {@code Optional.empty()}
     */
    Optional<T> toOptionalSuccess();

    /**
     * Converts the failure throwable to {@link Optional}.
     *
     * @return {@code Optional.of(error)} for {@link Failure}; otherwise {@code Optional.empty()}
     */
    Optional<Throwable> toOptionalFailure();

    /**
     * Converts this value to an {@link Either}.
     *
     * @return {@code Either.right(value)} for {@link Success}; {@code Either.left(error)} for {@link Failure}
     */
    Either<Throwable, T> toEither();

    /**
     * Converts this value to a {@link Result}.
     *
     * @return {@code Result.success(value)} for {@link Success}; {@code Result.failure(error)} for {@link Failure}
     */
    Result<T, Throwable> toResult();

    /**
     * Success variant.
     *
     * @param <T> success value type
     */
    record Success<T>(T value) implements Try<T> {
        public Success {
            if (value == null) {
                throw new IllegalArgumentException("value must not be null");
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <U> Try<U> map(Function<? super T, ? extends U> mapper) {
            U mapped = mapper.apply(value);
            if (mapped == null) {
                throw new IllegalArgumentException("mapped success value must not be null");
            }
            return new Success<>(mapped);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <U> Try<U> flatMap(Function<? super T, Try<U>> mapper) {
            return mapper.apply(value);
        }

        /** {@inheritDoc} */
        @Override
        public void onSuccess(Consumer<? super T> consumer) {
            consumer.accept(value);
        }

        /** {@inheritDoc} */
        @Override
        public void onFailure(Consumer<? super Throwable> consumer) { /* no-op */ }

        /** {@inheritDoc} */
        @Override
        public Optional<T> toOptionalSuccess() { return Optional.of(value); }

        /** {@inheritDoc} */
        @Override
        public Optional<Throwable> toOptionalFailure() { return Optional.empty(); }

        /** {@inheritDoc} */
        @Override
        public Either<Throwable, T> toEither() { return Either.right(value); }

        /** {@inheritDoc} */
        @Override
        public Result<T, Throwable> toResult() { return Result.success(value); }
    }

    /**
     * Failure variant.
     *
     * @param <T> success value type
     */
    record Failure<T>(Throwable error) implements Try<T> {
        public Failure {
            if (error == null) {
                throw new IllegalArgumentException("error must not be null");
            }
        }

        /** {@inheritDoc} */
        @Override
        public <U> Try<U> map(Function<? super T, ? extends U> mapper) {
            return new Failure<>(error);
        }

        /** {@inheritDoc} */
        @Override
        public <U> Try<U> flatMap(Function<? super T, Try<U>> mapper) {
            return new Failure<>(error);
        }

        /** {@inheritDoc} */
        @Override
        public void onSuccess(Consumer<? super T> consumer) { /* no-op */ }

        /** {@inheritDoc} */
        @Override
        public void onFailure(Consumer<? super Throwable> consumer) {
            consumer.accept(error);
        }

        /** {@inheritDoc} */
        @Override
        public Optional<T> toOptionalSuccess() { return Optional.empty(); }

        /** {@inheritDoc} */
        @Override
        public Optional<Throwable> toOptionalFailure() { return Optional.of(error); }

        /** {@inheritDoc} */
        @Override
        public Either<Throwable, T> toEither() { return Either.left(error); }

        /** {@inheritDoc} */
        @Override
        public Result<T, Throwable> toResult() { return Result.failure(error); }
    }
}
