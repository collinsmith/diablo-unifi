package com.gmail.collinsmith70.serializer;

/**
 * Implementation of a {@link Serializer} which (de)serializes objects of the specified type
 * {@link T} to and from a {@link String} representation.
 *
 * @param <T> The type of object which this {@code StringSerializer} accepts
 */
public interface StringSerializer<T> extends Serializer<T, String> {}
