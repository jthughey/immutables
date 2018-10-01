# immutables
A library for creating and using immutable Java beans.

[Start Here](https://github.com/jthughey/immutables/blob/master/src/main/java/com/immutables/StartHere.java)



    /* Type definition for an "Immutable Type". */
    public static class AnimalType extends LazyImmutableType {
        /* Define a type inside of itself. */
        private static final AnimalType ANIMAL_TYPE = Immutables.def(AnimalType.class);

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
                .set(name, "Lion")
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
