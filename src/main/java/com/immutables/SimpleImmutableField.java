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

import java.util.UUID;

/**
 * This class represents a declaration of an immutable field instance.
 * @param <T> The type of the value that this field represents.
 */
public class SimpleImmutableField<T> implements ImmutableField<T> {

    private final Class<T> type;
    private final String name;
    private final Object unique = new Object();
    

    /**
     * Declare a new instance of the type provided.
     * @param type The type of this field, must not be null.
     * @param name The name of the field, must not be null.
     */
    public SimpleImmutableField (Class<T> type, String name) {
        type.hashCode(); //null checks
        name.hashCode();
        this.type = type;
        this.name = name;
    }

    /**
     * Declare a new instance of the type provided.
     * @param type The type of this field, must not be null.
     */
    public SimpleImmutableField (Class<T> type) {
        this(type, UUID.randomUUID().toString());
    }

    /**
     * Get the type that this field's values must be.
     * @return The type.
     */
    public Class<T> getType () {
        return type;
    }

    /**
     * Set the field value in a new state instance which is a value clone of the original {@link State} instance
     * provided.  If the original state instance had a value declared for this field the value will be overwritten
     * in the new state.
     * @param state The original state.
     * @param val The value to set.
     * @return The new {@link State} instance containing the value provided mapped to this field.
     */
    public State set (State state, T val) {
        return state.field(this).set(val);
    }

    /**
     * Get the field value from the {@link State} instance provided.
     * @param state The {@link State} to retrieve the value from.
     * @return The value for this field in the provided {@link State} instance.
     */
    public T get (State state) {
        return state.field(this).get();
    }

    @Override
    public String getName () {
        return name;
    }

    @Override
    public final Object getFieldIdentifier () {
        return unique;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean equals (Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ImmutableField)) {
            return false;
        }
        ImmutableField sif = (ImmutableField)obj;
        return this.unique.equals(sif.getFieldIdentifier());
    }

    @Override
    public int hashCode () {
        return 17 * 37 + unique.hashCode();
    }

}
