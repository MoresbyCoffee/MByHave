/*
 * Moresby Coffee Bean
 *
 * Copyright (c) 2012, Barnabas Sudy (barnabas.sudy@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
package com.moresby.have;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

public class StepCandidate {

    public static class MethodParameter {

        private final String paramName;
        private final int   paramPos;

        /**
         * @param paramName
         * @param paramPos
         */
        public MethodParameter(final String paramName, final int paramPos) {
            super();
            this.paramName = paramName;
            this.paramPos = paramPos;
        }

        public String getParamName() {
            return paramName;
        }
        public int getParamPos() {
            return paramPos;
        }


    }


    private final String               value;
    private final Method               method;
    private final Map<Integer, StepCandidate.MethodParameter> parameterPositions;
    private final Pattern              pattern;


    public StepCandidate(final String value, final Method method, final Map<Integer, StepCandidate.MethodParameter> parameterPositions, final String regEx) {
        this.value              = value;
        this.method             = method;
        this.parameterPositions = Collections.unmodifiableMap(parameterPositions);
        this.pattern            = Pattern.compile(regEx);
    }

    public String getValue() {
        return value;
    }

    public Method getMethod() {
        return method;
    }

    public Map<Integer, StepCandidate.MethodParameter> getParameterPositions() {
        return parameterPositions;
    }

    public Pattern getPattern() {
        return pattern;
    }

}