package com.immutables;

/*
 * Copyright 2017 Justin Hughey
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.time.LocalDate;

/**
 * This class represents a declaration of an immutable field instance.
 * @param <T> The type of the value that this field represents.
 */
public interface ImmutableField<T> {

   /**
     * Get the type that this field's values must be.
     * @return The type.
     */
    Class<T> getType ();

    /**
     * Set the field value in a new state instance which is a value clone of the original {@link State} instance
     * provided.  If the original state instance had a value declared for this field the value will be overwritten
     * in the new state.
     * @param state The original state.
     * @param val The value to set.
     * @return The new {@link State} instance containing the value provided mapped to this field.
     */
    State set (State state, T val);

    /**
     * Get the field value from the {@link State} instance provided.
     * @param state The {@link State} to retrieve the value from.
     * @return The value for this field in the provided {@link State} instance.
     */
    T get (State state);

    /**
     * The unique object that identifies the field.
     * @return The unique object.
     */
    Object getFieldIdentifier();

    /**
     * The name given to this field.
     * @return The field's name.
     */
    String getName();

    /**
     * Construct a new {@link ImmutableField} instance of the type provided.
     * @param type The type of the field.
     * @return The new instance.
     */
    static <S> ImmutableField<S> of (Class<S> type) {
        return (ImmutableField<S>) new SimpleImmutableField<S>(type);
    }

    /**
     * Construct a new {@link ImmutableField} instance of the type provided.
     * @param type The type of the field.
     * @param name a custom name to give the field.
     * @return The new instance.
     */
    static <S> ImmutableField<S> of (Class<S> type, String name) {
        return (ImmutableField<S>) new SimpleImmutableField<S>(type, name);
    }

    /**
     * String type.
     * @return String type field.
     */
    static ImmutableField<String> string() {
        return of(String.class);
    }

    /**
     * Integer type.
     * @return Integer type field.
     */
    static ImmutableField<Integer> integer() {
        return of(Integer.class);
    }

    /**
     * Bool type.
     * @return Bool type field.
     */
    static ImmutableField<Boolean> bool() {
        return of(Boolean.class);
    }

    /**
     * Date type.
     * @return Date type field.
     */
    static ImmutableField<LocalDate> date() {
        return of(LocalDate.class);
    }
}
