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
 * This class exists to bundle type information with state.
 * General usage is declaring the type for easier API consumption. </p>
 * {@code
 *   public void consume (Immutable<Contact> contact);
 * }
 * </p>
 * @param <I> The ImmutableType.
 */
public class Immutable<I extends ImmutableType> {
    public final I type;
    public final State state;

    public Immutable (I type, State state) {
        this.type = type;
        this.state = state;
    }
}