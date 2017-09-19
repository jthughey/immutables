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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is a simple representation of {@link ImmutableType}.
 */
@SuppressWarnings("rawtypes")
public class SimpleImmutableType implements ImmutableType {

    /**
     * The collection of fields this type owns.
     */
    public final Set<ImmutableField<?>> fields;
    public final Map<ImmutableField, List<FieldValidation<?>>> validationsByField;

    private final Object unique = new Object();

    /**
     * Construct new instance which contains no fields.  This is simlar to declaring a {@link java.lang.Object}.
     */
    public SimpleImmutableType () {
        this(Collections.emptySet(), Collections.emptyList());
    }

    /**
     * Construct new instance with the provided {@link ImmutableField} declarations.
     * @param fields The fields that this type will own.
     */
    public SimpleImmutableType (Collection<ImmutableField<?>> fields) {
        this(Collections.unmodifiableSet(new HashSet<>(fields)), Collections.emptyList());
    }

    /**
     * Construct new instance with the provided {@link ImmutableField} declarations and {@link FieldValidation}
     * instances.
     * @param fields The fields that this type will own.
     * @param validations The validations that will be applied to the fields.
     */
    public SimpleImmutableType (Set<ImmutableField<?>> fields, List<FieldValidation<?>> validations)
    {
        this.fields = fields;
        this.validationsByField = validations.stream().collect(Collectors.groupingBy(FieldValidation::field));
    }

    @Override
    public SimpleImmutableType addField (ImmutableField field) {
        Set<ImmutableField<?>> tmp = new HashSet<>(fields);
        tmp.add(field);
        return new SimpleImmutableType(tmp, getValidations());
    }

    @Override
    public ImmutableType addValidation (FieldValidation<?> validation) {
        List<FieldValidation<?>> tmp = new ArrayList<>(getValidations());
        tmp.add(validation);
        return new SimpleImmutableType(fields, tmp);
    }

    @Override
    public boolean knows (ImmutableField<?> field) {
        return fields.stream().anyMatch(f -> f.equals(field)); 
    }

    @Override
    public Collection<ImmutableField<?>> getFields () {
        return fields;
    }

    @Override
    public List<FieldValidation<?>> getValidations () {
        return validationsByField.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    public Object getTypeIdentifier () {
        return unique;
    }

    @Override
    public <T> FieldValidator<T> validator (ImmutableField<T> field) {
        return new FieldValidator<T>() {

            @SuppressWarnings("unchecked")
            @Override
            public void validate (T value) {
                Optional.ofNullable(validationsByField.get(field))
                    .ifPresent(col -> {
                        List<String> messages = new ArrayList<>();
                        col.forEach(val -> {
                            ((FieldValidation<T>)val).validate(value).ifPresent(msg -> messages.add((String)msg));
                        });
                        if (!messages.isEmpty()) {
                            throw new FieldValidationException(messages);
                        }
                    });
                }
        };
    }

    @Override
    public boolean equals (Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ImmutableType)) {
            return false;
        }
        ImmutableType it = (ImmutableType) obj;
        return this.unique.equals(it.getTypeIdentifier()); 
    }

    @Override
    public int hashCode () {
        return 17 * 37 + unique.hashCode();
    }

    @Override
    public State state () {
        return new State(this);
    }



}
