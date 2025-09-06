package com.github.rshindo.jfunc;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A disjoint union of two possible values, {@link Left} or {@link Right}.
 * <p>
 * Convention: operations like {@link #map(Function)} and {@link #flatMap(Function)} act on the {@code Right} value
 * (the "success" path) and propagate {@code Left} as-is (the "error" path).
 * Minimal API and pattern-matching first design; no fold-like helpers are provided.
 * </p>
 *
 * @param <L> the left value type
 * @param <R> the right value type
 */
public sealed interface Either<L, R> {

	/**
	 * Creates a {@link Right} with the given non-null value.
	 *
	 * @param value the right value (must be non-null)
	 * @param <L>   the left type
	 * @param <R>   the right type
	 * @return a {@link Right}
	 * @throws IllegalArgumentException if {@code value} is {@code null}
	 */
	static <L, R> Either<L, R> right(R value) {
		if (value == null) {
			throw new IllegalArgumentException("value must not be null");
		}
		return new Right<>(value);
	}

	/**
	 * Creates a {@link Left} with the given non-null value.
	 *
	 * @param value the left value (must be non-null)
	 * @param <L>   the left type
	 * @param <R>   the right type
	 * @return a {@link Left}
	 * @throws IllegalArgumentException if {@code value} is {@code null}
	 */
	static <L, R> Either<L, R> left(L value) {
		if (value == null) {
			throw new IllegalArgumentException("value must not be null");
		}
		return new Left<>(value);
	}

	/**
	 * Maps the {@code Right} value using the mapper; {@code Left} is propagated unchanged.
	 *
	 * @param mapper the mapping function for the right value (must not return {@code null})
	 * @param <R2>   the mapped right type
	 * @return the mapped {@link Either}
	 * @throws IllegalArgumentException if this is {@code Right} and the mapper returns {@code null}
	 * @throws NullPointerException     if {@code mapper} is {@code null}
	 */
	<R2> Either<L, R2> map(Function<? super R, ? extends R2> mapper);

	/**
	 * Maps the {@code Left} value using the mapper; {@code Right} is propagated unchanged.
	 *
	 * @param mapper the mapping function for the left value (must not return {@code null})
	 * @param <L2>   the mapped left type
	 * @return the mapped {@link Either}
	 * @throws IllegalArgumentException if this is {@code Left} and the mapper returns {@code null}
	 * @throws NullPointerException     if {@code mapper} is {@code null}
	 */
	<L2> Either<L2, R> mapLeft(Function<? super L, ? extends L2> mapper);

	/**
	 * Flat-maps the {@code Right} value; {@code Left} is propagated unchanged.
	 *
	 * @param mapper the mapping function returning an {@link Either}
	 * @param <R2>   the mapped right type
	 * @return the mapped {@link Either}
	 * @throws NullPointerException if {@code mapper} is {@code null}
	 */
	<R2> Either<L, R2> flatMap(Function<? super R, Either<L, R2>> mapper);

	/**
	 * Executes the consumer if this is {@code Right}; no-op for {@code Left}.
	 *
	 * @param consumer side-effect on the right value
	 * @throws NullPointerException if {@code consumer} is {@code null}
	 */
	void ifRight(Consumer<? super R> consumer);

	/**
	 * Executes the consumer if this is {@code Left}; no-op for {@code Right}.
	 *
	 * @param consumer side-effect on the left value
	 * @throws NullPointerException if {@code consumer} is {@code null}
	 */
	void ifLeft(Consumer<? super L> consumer);

	/**
	 * Swaps left and right sides.
	 *
	 * @return {@code Left(v)} if this is {@code Right(v)}; otherwise {@code Right(v)}
	 */
	Either<R, L> swap();

	/**
	 * Converts the right value to {@link Option}.
	 *
	 * @return {@code Option.some(value)} for {@code Right}; {@code Option.none()} for {@code Left}
	 */
	Option<R> toOptionRight();

	/**
	 * Converts the left value to {@link Option}.
	 *
	 * @return {@code Option.some(value)} for {@code Left}; {@code Option.none()} for {@code Right}
	 */
	Option<L> toOptionLeft();

	/**
	 * Left variant.
	 */
	record Left<L, R>(L value) implements Either<L, R> {
		public Left {
			if (value == null) {
				throw new IllegalArgumentException("value must not be null");
			}
		}

		@Override
		public <R2> Either<L, R2> map(Function<? super R, ? extends R2> mapper) {
			return new Left<>(value);
		}

		@Override
		public <L2> Either<L2, R> mapLeft(Function<? super L, ? extends L2> mapper) {
			L2 mapped = mapper.apply(value);
			if (mapped == null) {
				throw new IllegalArgumentException("mapped left value must not be null");
			}
			return new Left<>(mapped);
		}

		@Override
		public <R2> Either<L, R2> flatMap(Function<? super R, Either<L, R2>> mapper) {
			return new Left<>(value);
		}

		@Override
		public void ifRight(Consumer<? super R> consumer) {
			// no-op
		}

		@Override
		public void ifLeft(Consumer<? super L> consumer) {
			consumer.accept(value);
		}

		@Override
		public Either<R, L> swap() {
			return new Right<>(value);
		}

		@Override
		public Option<R> toOptionRight() {
			return Option.none();
		}

		@Override
		public Option<L> toOptionLeft() {
			return Option.some(value);
		}
	}

	/**
	 * Right variant.
	 */
	record Right<L, R>(R value) implements Either<L, R> {
		public Right {
			if (value == null) {
				throw new IllegalArgumentException("value must not be null");
			}
		}

		@Override
		public <R2> Either<L, R2> map(Function<? super R, ? extends R2> mapper) {
			R2 mapped = mapper.apply(value);
			if (mapped == null) {
				throw new IllegalArgumentException("mapped right value must not be null");
			}
			return new Right<>(mapped);
		}

		@Override
		public <L2> Either<L2, R> mapLeft(Function<? super L, ? extends L2> mapper) {
			return new Right<>(value);
		}

		@Override
		public <R2> Either<L, R2> flatMap(Function<? super R, Either<L, R2>> mapper) {
			return mapper.apply(value);
		}

		@Override
		public void ifRight(Consumer<? super R> consumer) {
			consumer.accept(value);
		}

		@Override
		public void ifLeft(Consumer<? super L> consumer) {
			// no-op
		}

		@Override
		public Either<R, L> swap() {
			return new Left<>(value);
		}

		@Override
		public Option<R> toOptionRight() {
			return Option.some(value);
		}

		@Override
		public Option<L> toOptionLeft() {
			return Option.none();
		}
	}
}
