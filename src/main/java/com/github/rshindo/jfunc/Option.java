package com.github.rshindo.jfunc;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A functional container representing presence or absence of a value.
 * <p>
 * It corresponds to Haskell's {@code Maybe}. When a value exists, the state is {@link Some};
 * otherwise it is {@link None}. Core operations are similar to {@link Optional} and the type
 * provides conversions via {@link #toOptional()} and {@link #fromOptional(Optional)}.
 * </p>
 * <p>
 * Policy: {@link #some(Object)} does not allow {@code null} (throws {@link IllegalArgumentException}).
 * Use {@link #ofNullable(Object)} to treat {@code null} as absence.
 * </p>
 *
 * @param <T> the type of the contained value
 */
public sealed interface Option<T> {
	/**
	 * Creates a {@link Some} with the given non-null value.
	 *
	 * @param value the value (must be non-null)
	 * @param <T>   the value type
	 * @return {@code Some(value)}
	 * @throws IllegalArgumentException if {@code value} is {@code null}
	 */
	static <T> Option<T> some(T value) {
		return new Some<>(value);
	}

	/**
	 * Creates a {@link None} (an absent value).
	 *
	 * @param <T> the value type (for wildcard compatibility)
	 * @return {@code None}
	 */
	static <T> Option<T> none() {
		return new None<>();
	}

	/**
	 * Creates an {@link Option} from a possibly-null value.
	 * Returns {@code None} if the value is {@code null}; otherwise returns {@code Some(value)}.
	 *
	 * @param value the value (nullable)
	 * @param <T>   the value type
	 * @return {@code value == null ? None : Some(value)}
	 */
	static <T> Option<T> ofNullable(T value) {
		return value == null ? Option.none() : Option.some(value);
	}

	/**
	 * If a value is present, applies the mapping function and returns {@code Some(result)};
	 * if the function returns {@code null} or the value is absent, returns {@code None}.
	 *
	 * @param mapper the mapping function
	 * @param <R>    the result type
	 * @return the mapped {@link Option}
	 */
	<R> Option<R> map(Function<? super T, ? extends R> mapper);

	/**
	 * If a value is present, applies the mapping function and returns the resulting {@link Option}
	 * (i.e., flattened); otherwise returns {@code None}.
	 *
	 * @param mapper the mapping function
	 * @param <R>    the result type
	 * @return the mapped {@link Option}
	 */
	<R> Option<R> flatMap(Function<? super T, Option<R>> mapper);

	/**
	 * If a value is present, performs the given {@link Consumer} with the value.
	 * Does nothing if the value is absent.
	 *
	 * @param consumer the side-effect to execute with the value
	 */
	void ifPresent(Consumer<? super T> consumer);

	/**
	 * If a value is present and the predicate returns {@code true}, returns {@code this};
	 * otherwise returns {@code None}. If the value is absent, returns {@code None}.
	 *
	 * @param predicate the predicate to test the value
	 * @return the filtered {@link Option}
	 */
	Option<T> filter(Predicate<? super T> predicate);

	/**
	 * Converts to a {@link Optional}.
	 *
	 * @return the corresponding {@link Optional}
	 */
	Optional<T> toOptional();

	/**
	 * Converts to a {@link Stream}. When a value is present, returns a single-element stream;
	 * otherwise returns an empty stream (same as {@link Optional#stream()}).
	 *
	 * @return a single-element or empty stream
	 */
	Stream<T> stream();

	/**
	 * Converts from a {@link Optional} to an {@link Option}. {@code null} or
	 * {@link Optional#empty()} becomes {@code None}.
	 *
	 * @param optional the source {@link Optional}
	 * @param <T>      the value type
	 * @return the converted {@link Option}
	 */
	static <T> Option<T> fromOptional(Optional<? extends T> optional) {
		if (optional == null || optional.isEmpty()) {
			return Option.none();
		}
		return Option.some(optional.get());
	}

	/**
	 * State representing the presence of a value.
	 *
	 * @param value the contained non-null value
	 */
	record Some<T>(T value) implements Option<T> {
		public Some {
			if (value == null) {
				throw new IllegalArgumentException("value must not be null");
			}
		}

		@Override
		public <R> Option<R> map(Function<? super T, ? extends R> mapper) {
			R mapped = mapper.apply(value);
			return mapped == null ? Option.none() : Option.some(mapped);
		}

		@Override
		public <R> Option<R> flatMap(Function<? super T, Option<R>> mapper) {
			return mapper.apply(value);
		}

		@Override
		public void ifPresent(Consumer<? super T> consumer) {
			consumer.accept(value);
		}

		@Override
		public Option<T> filter(Predicate<? super T> predicate) {
			return predicate.test(value) ? this : Option.none();
		}

		@Override
		public Optional<T> toOptional() {
			return Optional.ofNullable(value);
		}

		@Override
		public Stream<T> stream() {
			return Stream.of(value);
		}
	}

	/**
	 * State representing the absence of a value.
	 */
	record None<T>() implements Option<T> {

		@Override
		public <R> Option<R> map(Function<? super T, ? extends R> mapper) {
			return Option.none();
		}

		@Override
		public <R> Option<R> flatMap(Function<? super T, Option<R>> mapper) {
			return Option.none();
		}

		@Override
		public void ifPresent(Consumer<? super T> consumer) {
			// no-op
		}

		@Override
		public Option<T> filter(Predicate<? super T> predicate) {
			return this;
		}

		@Override
		public Optional<T> toOptional() {
			return Optional.empty();
		}

		@Override
		public Stream<T> stream() {
			return Stream.empty();
		}
	}
}
