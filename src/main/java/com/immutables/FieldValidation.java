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

import java.util.Optional;
import java.util.function.Function;

/**
 * An individual field validation.
 * @param <T> The type of the value to validate.
 */
public interface FieldValidation<T> {
    /**
     * Execute validation on the value provided.
     * @param value The value to validate.
     * @return A string containing an error message otherwise {@link java.util.Optional#empty()}.
     */
    public Optional<String> validate (T value);

    /**
     * The field that this validation is associated with.
     * @return The field.
     */
    public ImmutableField<T> field();

    /**
     * Construct a new instance for the provided field using the validation {@link java.util.function.Function}.
     * @param field The field to validate.
     * @param val The function that contains the validation logic.
     * @return The field validation.
     */
    public static <V>  FieldValidation<V> on (ImmutableField<V> field, Function<V, Optional<String>> val) {
        return new FieldValidation<V> () {

            @Override
            public Optional<String> validate (V value) {
                return val.apply(value);
            }

            @Override
            public ImmutableField<V> field () {
                return field;
            }
            
        };
    }
}