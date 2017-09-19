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

import com.immutables.State.Populator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is an implementation of {@link ImmutableType} that provides the ability to lazily define it's
 * {@link ImmutableField} declarations by waiting until instantiation to define them via an explicit call, via
 * constructor, to the {@link #define(Collection)} method or auto-magically via the {@link #autoDefine()} method.
 */
public abstract class LazyImmutableType implements ImmutableType {

    /**
     * Construct a new instance.
     */
    public LazyImmutableType() {}

    volatile ImmutableType typeDecl;
    private final Object lock = new Object();

    /**
     * Define this ImmutableType with a collection that explicitly defines the available ImmField instances. 
     * @param fields The collection of ImmFields that define this type.
     */
    public void define (Collection<ImmutableField<?>> fields) {
        synchronized(lock) {
            checkNotDefined();
            typeDecl = new SimpleImmutableType(fields);
        }
    }

    /**
     * Define the Immutable Type by using reflection to scan the class hierarchy for ImmField declarations.
     */
    public void autoDefine () {
        synchronized(lock) {
            checkNotDefined();
            Class<?> c = getClass();
            typeDecl = construct(c);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    ImmutableType construct (Class<?> c) {
        Set<ImmutableField<?>> fields = new HashSet<>();
        List<FieldValidation<?>> validations = new ArrayList<>();
        try {
            for (Field f : c.getDeclaredFields()) {
                f.setAccessible(true); //only affects this instance of the field, the fields are copied on read
                if (ImmutableField.class.equals(f.getType())) {
                    ImmutableField field = (ImmutableField) f.get(this);
                        fields.add(withName(field, field.getType(), f.getName()));
                }
                if (ImmutableType.class.isAssignableFrom(f.getType()) && !c.equals(f.getType())) {
                    ImmutableType it = (ImmutableType)f.get(this);
                    fields.addAll(it.getFields());
                    validations.addAll(it.getValidations());
                }
                if (FieldValidation.class.isAssignableFrom(f.getType())) {
                    FieldValidation fv = (FieldValidation)f.get(this);
                    validations.add(fv);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to auto define immutable fields.", e);
        }
        return new SimpleImmutableType(fields, validations);
    }

    private <T> ImmutableField<T> withName (ImmutableField<T> field, Class<T> type, String name) {
        return new NamedImmutableField<>(field, name);
    }

    void checkNotDefined () {
        if (typeDecl != null) {
            throw new IllegalStateException("Instance's ImmutableType already defined.");
        }
    }

    @Override
    public ImmutableType addField (ImmutableField<?> field) {
        return getImmutableType().addField(field);
    }

    @Override
    public ImmutableType addValidation (FieldValidation<?> validation) {
        return getImmutableType().addValidation(validation);
    }

    @Override
    public boolean knows (ImmutableField<?> field) {
        return getImmutableType().knows(field);
    }

    @Override
    public Collection<ImmutableField<?>> getFields () {
        return getImmutableType().getFields();
    }

    @Override
    public Collection<FieldValidation<?>> getValidations () {
        return getImmutableType().getValidations();
    }

    @Override
    public <T> FieldValidator<T> validator (ImmutableField<T> field) {
        return getImmutableType().validator(field);
    }

    @Override
    public Object getTypeIdentifier () {
        return getImmutableType().getTypeIdentifier();
    }

    @Override
    public State state() {
        return new State(this);
    }

    /**
     * Retrieve the {@link ImmutableType} that was declared as this instance's type.
     * @return The {@link ImmutableType}.
     */
    public ImmutableType getImmutableType() {
        if (typeDecl == null) {
            throw new IllegalStateException("This type is lazy and has not yet been defined.");
        }
        return typeDecl;
    }

    @Override
    public boolean equals (Object obj) {
        return getImmutableType().equals(obj);
    }

    @Override
    public int hashCode () {
        return getImmutableType().hashCode();
    }

    /**
     * Handy helper to expose the {@link Populator} without having to go through the state instance.
     * @return A {@link Populator} for this type.
     */
    public Populator populator() {
        return state().populator();
    }

    private static class NamedImmutableField<T> extends DelegatingImmutableField<T> {

        private final String name;

        public NamedImmutableField (ImmutableField<T> field, String name) {
            super(field);
            this.name = name;
        }

        @Override
        public String getName () {
            return name;
        }
    }

}