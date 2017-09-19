package com.immutables;

import java.util.Collection;

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
 * 
 * API that declares an immutable type declaration.
 */
public interface ImmutableType {

    /**
     * Adding a field to an ImmutableType changes the type and therefore this method must return a new instance of
     * ImmutableType that contains the original fields as well as the newly added field.
     * @param field The field to add.
     * @return The new ImmutableType containing the original fields and new field.
     */
    public ImmutableType addField (ImmutableField<?> field);

    /**
     * Adding a validation to an ImmutableType changes the type and therefore this method must return a new instance of
     * ImmutableType that contains the original validations as well as the newly added validation.
     * @param validation The validation to add.
     * @return The new ImmutableType containing the original validations and the new validation.
     */
    public ImmutableType addValidation (FieldValidation<?> validation);

    /**
     * Check if the ImmutableType knows the {@link ImmutableField} provided. Multiple types are allowed to know a
     * single field instance.
     * @param field The ImmField.
     * @return {@code true} if this type owns the field, {@code false} otherwise.
     */
    public boolean knows (ImmutableField<?> field);

    /**
     * Retrieve an immutable collection of the {@link ImmutableField} instances that this type owns.
     * @return The collection of {@link ImmutableField} instances.
     */
    public Collection<ImmutableField<?>> getFields();

    /**
     * Get the {@link com.immutables.FieldValidation} for all fields.
     * @return The collection of field validations for all fields.
     */
    public Collection<FieldValidation<?>> getValidations();

    /**
     * Return a field validator for the field specified.
     * @param field The field to retrieve a validator for.
     * @return The field value validator.
     */
    public <T> FieldValidator<T> validator (ImmutableField<T> field);

    /**
     * The unique object that identifies the type.
     * @return The unique object.
     */
    Object getTypeIdentifier();

    /**
     * Return a new state instance for this type.
     */
    public State state();

}
