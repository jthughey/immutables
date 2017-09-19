package com.immutables;

import org.hamcrest.core.Is;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class ImmutableFieldTest {

    @Test
    public void testSingleField() {
        TestType st = TestType.type();
        State state = st.state();
        Object val = new Object();
        state = st.field.set(state, val);
        assertThat(val,  Is.is(st.field.get(state)));
    }

    public static class TestType extends LazyImmutableType {

        /* Define a type inside of itself? O_o */
        private static final TestType SELF = Immutables.def(TestType.class);

        final ImmutableField<Object> field = ImmutableField.of(Object.class);

        public TestType () {
            autoDefine();
        }

        /**
         * A quick way to get the type definition.
         * @return The type.
         */
        public static final TestType type() {
            return SELF;
        }
    }
}
