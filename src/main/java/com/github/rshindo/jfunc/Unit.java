package com.github.rshindo.jfunc;

/**
 * A minimal sentinel type representing the absence of a meaningful value.
 * Useful for APIs that conceptually return {@code void} yet need a non-null payload
 * (e.g., {@link Try#run(Try.CheckedRunnable)} which produces {@code Try<Unit>}).
 */
public enum Unit {
    /** The single instance. */
    INSTANCE;
}

