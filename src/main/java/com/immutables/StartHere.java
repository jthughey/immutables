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

import com.immutables.State.GetSet;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StartHere {

    void start () {
        simple();
        extension();
        dynamic();
        fun();
    }

    /* Type definition for an "Immutable Type". */
    public static class AnimalType extends LazyImmutableType {
        /* Define a type inside of itself? O_o */
        private static final AnimalType ANIMAL_TYPE = Immutables.def(AnimalType.class);
        public static final String LION = "LION";

        //Define the fields.
        public final ImmutableField<String> name = ImmutableField.of(String.class);
        public final ImmutableField<Integer> populationCount = ImmutableField.of(Integer.class);
        public final ImmutableField<Integer> legs = ImmutableField.of(Integer.class);

        //We can add validation.
        public final FieldValidation<String> nameUppercase = FieldValidation.on(name,
            v -> {
                if (v.toUpperCase().equals(v)) {
                    return Optional.empty();
                }
                return Optional.of("Name must be upper case.");
            }
        );


        public AnimalType () {
            autoDefine();
        }

        /* Re-usable populator based on the fields defined in AnimalType. */
        public State populate (State state) {
            return state
                .populator()
                .set(name, LION)
                .set(legs, 0)
                .set(populationCount, 1)
                .done();
        }

        /**
         * A quick way to get the type definition.
         * @return The type.
         */
        public static final AnimalType type() {
            return ANIMAL_TYPE;
        }

    };

    public void simple() {
        println("# Simple");

        AnimalType animal = AnimalType.type();

        // Set the instance's values, each set call creates a new state object.
        State state = AnimalType.type()
            .state()
            .field(animal.name).set(AnimalType.LION)
            .field(animal.populationCount).set(50000);

        try {
            //You can't access a field if it hasn't explicitly had a value set.
            state.field(animal.legs).get();
        } catch (IllegalStateException e) {
            println("EXCEPTION: " + e.getMessage());
        }

        println("");
        // Alternatively, the more efficient way of doing things.  It's basically a builder.
        state = animal
            .populator()
            .set(animal.name, AnimalType.LION)
            .set(animal.legs, 4)
            .set(animal.populationCount, 50000)
            .done();

        // Get the values.
        String name = state.field(animal.name).get();
        println("state.name: " + name);
        Integer legs = state.field(animal.legs).get();
        println("state.legs: " + legs);
        Integer population = state.field(animal.populationCount).get();
        println("state.populationCount: " + population);

        //We can print all the field values for a state instance if we want.
        println("");
        State finalState = state;
        animal.getFields().forEach(f -> println(f.getName() + ":" + finalState.field(f).get()));
    }

    /* Type definition for an immutable type that composes itself using the previously defined "AnimalType".
     * No extension needed! */
    public static class FuzzyAnimalType extends LazyImmutableType {
        public final AnimalType animalType = Immutables.def(AnimalType.class);
        public final ImmutableField<Boolean> fuzzy = ImmutableField.of(Boolean.class);

        public FuzzyAnimalType () {
            autoDefine();
        }

        //Here is where type specific helper methods can be added.
    }

    public void extension () {
        println("\n# Extension");
        // Create a new type that uses the same field declarations as AnimalType.
        FuzzyAnimalType fuzzyAnimalType = Immutables.def(FuzzyAnimalType.class);

        // Re-use the AnimalType field declarations.
        State state = AnimalType.type().populate(fuzzyAnimalType.state());

        // Set the new field declaration.
        state = fuzzyAnimalType.fuzzy.set(state, true);

        //You can see all of the type's fields and any composed type's fields.
        ImmutableField<String> nameField = fuzzyAnimalType.animalType.name;
        println("nameField = fuzzyAnimalType.animalType.name");
        ImmutableField<Boolean> fuzzyField = fuzzyAnimalType.fuzzy;
        println("fuzzyField = fuzzyAnimalType.fuzzy");
        println("");
        // Now read some values.
        state = state.field(nameField).set("LION");
        String name = state.field(nameField).get();
        println("state.field(nameField).get(): " + name);

        // Get the value, field first.
        Boolean isFuzzy1 = fuzzyField.get(state);
        println("fuzzyField.get(state): " + isFuzzy1);

        // Or get the value, state first.
        Boolean isFuzzy2 = state.field(fuzzyField).get();
        println("state.field(fuzzyField).get(): " + isFuzzy2);

        // The field first approach can also be used to hold a reference to the "GetSet".
        GetSet<Boolean> getterSetter = state.field(fuzzyField);
        println("getterSetter.get(): " + getterSetter.get());

    }

    public void dynamic () {
        println("\n# Dynamic");
        // Dynamic field definition.
        ImmutableField<Fish> fishField = ImmutableField.of(Fish.class);

        // The following is effectively an anonymous type as we don't have a class declaration to
        // use with the Immutables.def(...) and Immutables.of(...) methods.
        AnimalType animal = AnimalType.type();
        ImmutableType anonFishType = animal.addField(fishField);

        // Create a new state from our anonymous type.
        State fishState = anonFishType.state();

        // Populate it using the same popluate method available on the AnimalType.
        // This works because the fields are shared.
        fishState = animal.populate(fishState);
       
        //It's not a lion!
        try {
            fishState = fishState.field(animal.name).set("fish");
        } catch (FieldValidationException e) {
            //Whoops, uppercase animal names only!
            System.err.println(e.getMessages().stream().collect(Collectors.joining("\n")));
        }
        fishState = fishState.field(animal.name).set("FISH");

        // Now set the newly created fish field.
        State goldfishState = fishState.field(fishField).set(new Fish("goldfish"));

        // Check on the state of the fish.
        stateOfFish(goldfishState, fishField);

        // Check on the state of the fish after naming it.
        fishState = goldfishState.field(animal.name).set("SCALEXANDER");
        stateOfFish(fishState, fishField);

        //You can also do fun things with the state.
        State s = new State(goldfishState.type()) {
            @Override
            public <T> Function<T, State> set (ImmutableField<T> field) {
                Function<T, State> fn = goldfishState.set(field);
                if (animal.name.equals(field)) {
                    return fn.compose(v -> {
                        println(String.format("You named your fish, %s.", v));
                        return v;
                    });
                }
                return fn;
            }
        };
        println("\n## Now use the custom state type.");
        s.set(animal.name).apply("MOBY DICK");
    }

    public void stateOfFish(State fs, ImmutableField<Fish> fishField) {
        AnimalType animal = AnimalType.type();
        if ("FISH".equals(animal.name.get(fs))) {
            println(String.format("Are you going to give your %s a name?", fishField.get(fs).type));
        } else {
            println(String.format("Alas poor %s %s, we barely knew him.",
                fishField.get(fs).type,
                animal.name.get(fs)));
        }
    }

    /* Fun */
    public void fun () {
        println("\n# Fun");
        PersonType type = Immutables.def(PersonType.class);
        // Create a person.
        State froedrick = type.state()
            .field(type.last).set("Frankenstein")
            .field(type.first).set("Froedrick");

        ImmutableField<Boolean> normalBrain = ImmutableField.bool();

        // Add a field to create a new type, person with brain.
        ImmutableType personWithBrain = type.addField(normalBrain);

        // Create a person with a brain, populator is available with anonymous immutable types.
        State monster = personWithBrain
            .state().populator()
            .set(normalBrain, false)
            .set(type.first, "The")
            .set(type.last, "Monster").done();

        stateOfPerson(froedrick, type, normalBrain);
        stateOfPerson(monster, type, normalBrain);

    }

    /* Print the state of a person.*/
    public void stateOfPerson(State s, PersonType type, ImmutableField<Boolean> normalBrain) {
        println("\nHello, " + s.field(type.first).get() + " " + s.field(type.last).get());

        //We need to see if the state instance provided knows about normal brains.
        if (s.knows(normalBrain)) {
            if (s.field(normalBrain).get()) {
                println("You are normal.");
            } else {
                println("You are abby-normal!");
            }
        } else {
            println("I can't tell if you're normal or not...");
        }

    }

    public static class PersonType extends LazyImmutableType {
        public final ImmutableField<String> first = ImmutableField.string();
        public final ImmutableField<String> last = ImmutableField.string();

        public PersonType () {
            autoDefine();
        }
    }

    private static class Fish {
        public final String type;

        Fish (String type) {
            this.type = type;
        }
    }

    public void println(Object s) {
        System.out.println(s);
    }

    public static void main (String[] args) {
        try {
            new StartHere().start();
        } catch (FieldValidationException fve) {
            System.err.println(fve.getMessages().stream().collect(Collectors.joining("\n")));
        }
    }

}
