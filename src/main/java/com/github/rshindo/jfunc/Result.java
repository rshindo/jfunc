package com.github.rshindo.jfunc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.Objects;

/**
 * Result type for Railway Oriented Programming (ROP).
 * Represents either a successful value ({@link Success}) or a failure ({@link Failure}).
 * <p>
 * Design principles:
 * - Minimal API: prefer Java pattern matching over helpers; no fold-like methods are provided.
 * - Variant-oriented naming: {@link #onSuccess(Consumer)} / {@link #onFailure(Consumer)} / {@link #tee(Consumer)}.
 * - Nulls are not allowed for values or mapper results; factory methods reject null inputs.
 * </p>
 * <p>
 * Semantics:
 * - Right-biased operations: {@link #map(Function)} and {@link #flatMap(Function)} operate on {@code Success}
 * and propagate {@code Failure} unchanged.
 * - Transform failures with {@link #mapFailure(Function)}.
 * - Preserve information with {@link #toEither()}.
 * - Convert to {@link Option} via {@link #toOptionSuccess()} / {@link #toOptionFailure()}.
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
	 * Combines multiple {@link Result} values into a single {@link Result}.
	 *
	 * @param results the input {@link Result} values
	 * @param <T>     the success value type
	 * @param <E>     the failure value type
	 * @return {@code Success(List<T>)} when all elements are {@link Success}; otherwise the first {@link Failure}
	 * @throws IllegalArgumentException if {@code results} is {@code null} or contains {@code null} elements
	 */
	static <T, E> Result<List<T>, E> sequence(Iterable<Result<T, E>> results) {
		if (results == null) {
			throw new IllegalArgumentException("results must not be null");
		}

		List<T> values = new ArrayList<>();
		for (Result<T, E> result : results) {
			if (result == null) {
				throw new IllegalArgumentException("results must not contain null");
			}

			switch (result) {
				case Success(var value) -> values.add(value);
				case Failure(var error) -> {
					return Result.failure(error);
				}
			}
		}
		return Result.success(List.copyOf(values));
	}

	/**
	 * Maps each element to a {@link Result} and combines the results into a single {@link Result}.
	 *
	 * @param values the input values
	 * @param mapper the function that maps each value to a {@link Result}
	 * @param <T>    the input type
	 * @param <U>    the success value type
	 * @param <E>    the failure value type
	 * @return {@code Success(List<U>)} when all mapped elements are {@link Success}; otherwise the first {@link Failure}
	 * @throws IllegalArgumentException if {@code values} is {@code null}, contains {@code null}, if
	 *                                  {@code mapper} is {@code null}, or if the mapper returns {@code null}
	 */
	static <T, U, E> Result<List<U>, E> traverse(Iterable<T> values, Function<? super T, Result<U, E>> mapper) {
		if (values == null) {
			throw new IllegalArgumentException("values must not be null");
		}
		if (mapper == null) {
			throw new IllegalArgumentException("mapper must not be null");
		}

		List<U> mappedValues = new ArrayList<>();
		for (T value : values) {
			if (value == null) {
				throw new IllegalArgumentException("values must not contain null");
			}

			Result<U, E> mapped = mapper.apply(value);
			if (mapped == null) {
				throw new IllegalArgumentException("mapper must not return null");
			}

			switch (mapped) {
				case Success(var successValue) -> mappedValues.add(successValue);
				case Failure(var error) -> {
					return Result.failure(error);
				}
			}
		}
		return Result.success(List.copyOf(mappedValues));
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

	/**
	 * Recovers from a {@link Failure} by mapping the error to a success value.
	 *
	 * @param mapper mapping function for the failure value (must not return null)
	 * @return this {@link Result} when this is {@link Success}; otherwise a recovered {@link Result}
	 * @throws IllegalArgumentException if this is {@link Failure} and the mapper returns {@code null}
	 * @throws NullPointerException     if {@code mapper} is {@code null}
	 */
	Result<T, E> recover(Function<? super E, ? extends T> mapper);

	/**
	 * Recovers from a {@link Failure} by mapping the error to another {@link Result}.
	 *
	 * @param mapper mapping function for the failure value (must not return null)
	 * @return this {@link Result} when this is {@link Success}; otherwise the mapped {@link Result}
	 * @throws IllegalArgumentException if this is {@link Failure} and the mapper returns {@code null}
	 * @throws NullPointerException     if {@code mapper} is {@code null}
	 */
	Result<T, E> recoverWith(Function<? super E, Result<T, E>> mapper);

	/**
	 * Executes the consumer only when this is a {@link Success}.
	 *
	 * @param consumer side-effect to execute with the success value
	 * @throws NullPointerException if {@code consumer} is {@code null}
	 */
	void onSuccess(Consumer<? super T> consumer);

	/**
	 * Executes the consumer only when this is a {@link Failure}.
	 *
	 * @param consumer side-effect to execute with the failure value
	 * @throws NullPointerException if {@code consumer} is {@code null}
	 */
	void onFailure(Consumer<? super E> consumer);

	/**
	 * Executes the consumer only when this is a {@link Success}, then returns this result.
	 *
	 * @param consumer side-effect to execute with the success value
	 * @return this {@link Result}
	 * @throws NullPointerException if {@code consumer} is {@code null}
	 */
	Result<T, E> tee(Consumer<? super T> consumer);

	/**
	 * Converts the success value to {@link Option}.
	 *
	 * @return {@code Option.some(value)} for {@link Success}; otherwise {@code Option.none()}
	 */
	Option<T> toOptionSuccess();

	/**
	 * Converts the failure value to {@link Option}.
	 *
	 * @return {@code Option.some(error)} for {@link Failure}; otherwise {@code Option.none()}
	 */
	Option<E> toOptionFailure();

	/**
	 * Converts this value to an {@link Either}.
	 *
	 * @return {@code Either.right(value)} for {@link Success}; {@code Either.left(error)} for {@link Failure}
	 */
	Either<E, T> toEither();

	/**
	 * Variant representing success. Carries a non-null success value.
	 */
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
		public Result<T, E> recover(Function<? super E, ? extends T> mapper) {
			Objects.requireNonNull(mapper);
			return this;
		}

		@Override
		public Result<T, E> recoverWith(Function<? super E, Result<T, E>> mapper) {
			Objects.requireNonNull(mapper);
			return this;
		}

		@Override
		public void onSuccess(Consumer<? super T> consumer) {
			consumer.accept(value);
		}

		@Override
		public void onFailure(Consumer<? super E> consumer) { /* no-op */ }

		@Override
		public Result<T, E> tee(Consumer<? super T> consumer) {
			Objects.requireNonNull(consumer);
			consumer.accept(value);
			return this;
		}

		@Override
		public Option<T> toOptionSuccess() {
			return Option.some(value);
		}

		@Override
		public Option<E> toOptionFailure() {
			return Option.none();
		}

		@Override
		public Either<E, T> toEither() {
			return Either.right(value);
		}
	}

	/**
	 * Variant representing failure. Carries a non-null failure value.
	 */
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
		public Result<T, E> recover(Function<? super E, ? extends T> mapper) {
			Objects.requireNonNull(mapper);
			T recovered = mapper.apply(error);
			if (recovered == null) {
				throw new IllegalArgumentException("recovered success value must not be null");
			}
			return new Success<>(recovered);
		}

		@Override
		public Result<T, E> recoverWith(Function<? super E, Result<T, E>> mapper) {
			Objects.requireNonNull(mapper);
			Result<T, E> recovered = mapper.apply(error);
			if (recovered == null) {
				throw new IllegalArgumentException("recovered result must not be null");
			}
			return recovered;
		}

		@Override
		public void onSuccess(Consumer<? super T> consumer) { /* no-op */ }

		@Override
		public void onFailure(Consumer<? super E> consumer) {
			consumer.accept(error);
		}

		@Override
		public Result<T, E> tee(Consumer<? super T> consumer) {
			Objects.requireNonNull(consumer);
			return this;
		}

		@Override
		public Option<T> toOptionSuccess() {
			return Option.none();
		}

		@Override
		public Option<E> toOptionFailure() {
			return Option.some(error);
		}

		@Override
		public Either<E, T> toEither() {
			return Either.left(error);
		}
	}
}
