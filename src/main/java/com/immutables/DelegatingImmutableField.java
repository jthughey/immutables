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

/**
 * A delegating instance of an immutable field.
 * @param <T> The type of the field's value.
 */
public abstract class DelegatingImmutableField<T> implements ImmutableField<T> {

    private final ImmutableField<T> field;

    /**
     * Construct a new instance.
     * @param field The field that will be delegated to.
     */
    public DelegatingImmutableField (ImmutableField<T> field) {
        this.field = field;
    }

    @Override
    public Class<T> getType () {
        return getDelegate().getType();
    }

    @Override
    public State set (State state, T val) {
        return getDelegate().set(state, val);
    }

    @Override
    public T get (State state) {
        return getDelegate().get(state);
    }

    @Override
    public final Object getFieldIdentifier () {
        return getDelegate().getFieldIdentifier();
    }

    @Override
    public String getName () {
        return getDelegate().getName();
    }

    @Override
    public boolean equals (Object obj) {
        return field.equals(obj);
    }

    @Override
    public int hashCode () {
        return field.hashCode();
    }

    /**
     * Retrieve the underlying delegate field.
     * @return The delegate.
     */
    public ImmutableField<T> getDelegate() {
        return field;
    }
}
