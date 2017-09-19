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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public final class Immutables {

    //Default behavior is never evict, that's what we want.
    private static final TypeCache TYPE_CACHE = new TypeCache();

    public static <I extends ImmutableType> Immutable<I> of (Class<I> from) {
        I type = def(from);
        return new Immutable<I>(type, new State(type));
    }

    public static <T extends ImmutableType> T def (Class<T> from) {
        try {
            return (T) TYPE_CACHE.get(CacheKey.of(from), () ->  from.newInstance());
         } catch (Exception e) {
             throw new IllegalArgumentException("Unable to retrieve immutable type definition.", e);
         }
    }

    private static class TypeCache {
        private volatile Map<CacheKey, ImmutableType> cache = Collections.emptyMap();
        private final Object lock = new Object();

        @SuppressWarnings("unchecked")
        public <T extends ImmutableType> T get(CacheKey key, Callable<T> c) throws Exception {
            T t = (T) cache.get(key);
            if (t != null) {
                return t;
            }
            synchronized (lock) {
                //If someone did the work for us we skip trying to create it again.
                if (!cache.containsKey(key)) {
                    ImmutableType it = c.call();
                    if (cache.containsKey(key)) {
                        //THIS IS NECESSARY DO NOT REMOVE
                        return (T) cache.get(key);
                    }
                    Map<CacheKey, ImmutableType> tmp = new HashMap<>(cache);
                    tmp.put(key, it);
                    cache = Collections.unmodifiableMap(tmp);
                }
                return (T) cache.get(key);
            }
        }
    }

    private static class CacheKey implements Comparable<CacheKey>, Serializable {

        private static final long serialVersionUID = 1L;

        private final Class<?> keyClass;
        private final String className;
        private final int hashCode;

        /**
         * Default constructor.
         *
         * @param keyClass The {@link Class} to create a key for.
         */
        public CacheKey (Class<?> keyClass) {
            this.keyClass = keyClass;
            this.className = keyClass.getName();
            this.hashCode = className.hashCode();
        }

        @Override
        public int compareTo (CacheKey o) {
            return className.compareTo(o.className);
        }

        @Override
        public boolean equals (Object o) {
            if (o == this) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (o.getClass() != getClass()) {
                return false;
            }
            CacheKey other = (CacheKey) o;
            return other.keyClass == keyClass;
        }

        @Override
        public int hashCode () {
            return hashCode;
        }

        @Override
        public String toString () {
            return className;
        }

        public static final CacheKey of (Class<?> cl) {
            return new CacheKey(cl);
        }
    }

}
