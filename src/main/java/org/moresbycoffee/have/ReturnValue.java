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
 * <p>The purpose of this class is to store return values from the <tt>step definition methods</tt>.</p>
 * <p>If a <tt>step definition method</tt> has {@link ReturnValue} parameter, the parameter placeholder
 * should be in <tt>step definition pattern</tt> because this value is passed to the method by the
 * MByHave framework.</p>
 * <p>The return value parameter will always represent the last return value of which the type matches
 * to the ReturnValue's generic parameter (<code>T</code>).</p>
 * <p>If there is no matching return value, a <tt>null</tt> object will be provided.</p>
 *
 * @param <T> The type of the cached return value.
 *
 * @author bsudy
 * @since 2012
 */
public class ReturnValue<T> {

    /** The type of the return value. (NonNull) */
    private final Type type;
    /**
     * The value of the return value.
     * Can be <tt>null</tt>, if the returned value was <tt>null</tt>. (Nullable)
     */
    private final T value;

    /**
     * @param type The type of the return value. (NonNull)
     * @param value The value of the return value. (Nullable)
     */
    ReturnValue(final Type type, final T value) {
        super();
        this.type  = type;
        this.value = value;
    }

    /**
     * @return The type of the return value. (NonNull)
     */
    public Type getType() {
        return type;
    }

    /**
     * @return The value of the return value.
     *         Can be <tt>null</tt>, if the returned value was <tt>null</tt>. (Nullable)
     */
    public T getValue() {
        return value;
    }





}
