/*
 * Moresby Coffee Bean
 *
 * Copyright (c) 2012, Barnabas Sudy (barnabas.sudy@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.moresbycoffee.have;

import java.lang.reflect.Type;

/**
 * <p>The container stores a value and the type of the value.</p>
 * <p>The container can be used to cache and pick up a named parameter. If a {@link Container}
 * parameter is added to a <tt>step</tt> definition method, MByHave will pass there a cached
 * container. The container will be picked up from a named cache (map) by the string in the
 * <tt>step description</tt> matching to parameter placeholder.</p>
 * <p>So the container's name is defined in the <tt>step description</tt> and therefore two
 * steps with the same <tt>step definition method</tt> can use different containers.</p>
 * <p>If there is no container existing under the given name, MByHave will create one with a <tt>null</tt>
 * value.</p>
 *
 * @param <T> the type of the value in the container.
 *
 * @author barnabas.sudy@gmail.com
 * @since 2012
 */
public class Container<T> {

    /** The type of the container's value. (NonNull) */
    private final Type type;
    /** The value of the container. (Nullable) */
    private T value;

    /**
     * @param type The type of the container's value.
     */
    Container(final Type type) {
        this.type = type;
    }

    /**
     * @return The type of the container's value.
     */
    public T getValue() {
        return value;
    }

    /**
     * @param value The value of the container to set. (Nullable)
     */
    public void setValue(final T value) {
        //TODO the type should be checked
        this.value = value;
    }

    /**
     * @return The value of the container. (Nullable)
     */
    public Type getType() {
        return type;
    }
}
