package com.immutables;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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

public class State {

    private static final Object UNASSIGNED = new Object();
    private final Object unique = new Object();

    public final ImmutableType type;
    private final Map<ImmutableField<?>, Object> values;

    /**
     * Construct a new instance.
     * @param iType The type the state holds values for.
     */
    public State (ImmutableType iType) {
        this(iType, unassignedValuesMap(iType, new HashMap<>()));
    }

    /**
     * Construct an instance of the provided type with the provided values.
     * @param type The type.
     * @param values The values.
     */
    private State (ImmutableType type, Map<ImmutableField<?>, Object> values) {
        this.type = type;
        this.values = Collections.unmodifiableMap(values);
    }

    /**
     * Create a new populator instance based on the immutable type.
     * @return The populator.
     */
    public Populator populator() {
        return new Populator(type);
    }

    /**
     * Return a "setter" function for the provided field.
     * @param field The field to get a setter function for.
     * @return The setter function.
     */
    public <T> Function<T, State> set(ImmutableField<T> field) {
        checkOwnership(field);
        return t -> setFieldValue(field, t);
    }

    /**
     * Return the {@link GetSet} for the provided field.
     * @param field The field to retrieve the {@link GetSet} for.
     * @return The {@link GetSet} for the field.
     */
    public <T> GetSet<T> field (ImmutableField<T> field) {
        return new GetSet<T> () {

            @Override
            public T get () {
                return fieldGet(field);
            }

            @Override
            public State set (T value) {
                return setFieldValue(field, value);
            }

            @Override
            public boolean isNull () {
                return get() == null;
            }

            @Override
            public boolean isAssigned () {
                return UNASSIGNED != unassignedFieldGet(field);
            }

            @Override
            public boolean isEmpty () {
                return !isAssigned() || isNull();
            }

            @Override
            public void consume (Consumer<T> c) {
                if (isAssigned()) {
                    c.accept(get());
                }
            }
        };
    }

    /**
     * Check if the state (via the type) knows about the provided field.
     * @param field The field to check.
     * @return {@code true} if the field is known, {@code false} otherwise
     */
    public boolean knows(ImmutableField<?> field) {
        return type().knows(field);
    }

    /**
     * The type this state represents.
     * @return The {@link ImmutableType}.
     */
    public ImmutableType type() {
        return type;
    }

    @Override
    public boolean equals (Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof State)) {
            return false;
        }
        // Two state instances are only equal if they share the "unique" object.
        return unique.equals(((State)obj).unique);
    }

    @Override
    public int hashCode () {
        return unique.hashCode();
    }

    private <T> T fieldGet (ImmutableField<T> field) {
        T value = unassignedFieldGet(field);

        if (UNASSIGNED == value) {
            throw new IllegalStateException("The field requested has not been assigned a value.");
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    private <T> T unassignedFieldGet (ImmutableField<T> field) {
        checkOwnership(field);
        Optional<Entry<ImmutableField<?>, Object>> es =
            values
            .entrySet()
            .stream()
            .filter(e -> e.getKey().equals(field))
            .findFirst();
        return (T)es.get().getValue();
    }

    private <I, T> State setFieldValue(ImmutableField<T> fld, T value) {
        checkOwnership(fld);
        type.validator(fld).validate(value);
        Map<ImmutableField<?>, Object> tmp = new HashMap<>(values);
        tmp.put(fld, value);
        return new State(type, Collections.unmodifiableMap(tmp));
    }

    private <T> void checkOwnership (ImmutableField<T> f) {
        if (!type.knows(f)) {
            throw new IllegalArgumentException("Field provided not owned by type.");
        }
    }

    private static Map<ImmutableField<?>, Object> unassignedValuesMap (
        ImmutableType type, Map<ImmutableField<?>, Object> m)
    {
        type.getFields().forEach(f -> {
            if(!m.containsKey(f)) {
                m.put(f, UNASSIGNED);
            }
        });
        return m;
    }


    /**
     * A {@link GetSet} is the method of accessing the value of a field within a state.
     * @param <T> The type of the value the field references.
     */
    public interface GetSet<T> extends Supplier<T> {
        /**
         * Set the value.
         * @param value The value to set.
         * @return The new state with the provided value.
         */
        public State set(T value);
        /**
         * Is the field's value {@code null}? 
         * @return {@code true} if null, {@code false} if otherwise.
         */
        public boolean isNull();

        /**
         * Is the field's value not yet assigend or was it explicitly set to 'null'?
         * @return {@code true} if not assigned or null, {@code false} if populated with a non-null value.
         */
        public boolean isEmpty();

        /**
         * Is the field's value was assigned.
         * @return {@code true} if assigned a value (includes {@code null}), {@code false} if never assigned a value.
         */
        public boolean isAssigned();

        /**
         * Provide the field's value to the provided consumer.  If a value was not assigned then method does nothing.
         * @param consumer The consumer to apply to the value.
         */
        public void consume (Consumer<T> consumer);
    }

    /**
     * A populator that allows for the setting of a multiple fields on a State instance at once.
     */
    public static class Populator {
        private final ImmutableType popType;
        private final Map<ImmutableField<?>, Object> popValues = new HashMap<>();

        /**
         * New instance for the provided type.
         */
        public Populator(ImmutableType type) {
            type.hashCode();
            this.popType = type;
        }

        /**
         * Set the provided field to the provided value.
         * @param field The {@link com.immutables.ImmutableField} to set.
         * @param val The value to set it to.
         * @param <T> The type of the value.
         * @return This, the populator.
         */
        public <T> Populator set(ImmutableField<T> field, T val) {
            if (popType.knows(field)) {
                popType.validator(field).validate(val);
                popValues.put(field, val);
            } else {
                throw new IllegalArgumentException("Unkown field, " + field.getName());
            }
            return this;
        }

        /**
         * Construct a new state instance from the fields and values provided.
         * @return The new state instance.
         */
        public State done() {
            return new State(popType, unassignedValuesMap(popType, popValues));
        }
    }
}
