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

/**
 * Represents a method in the test class which is annotated to be a step and
 * can be matched to a step description.<br>
 * The class consists of the original step definition, the {@link Method method object}
 * the method parameters (in a map which maintains the order of the parameters) and 
 * the {@link Pattern regex pattern} through which the step descriptions will be
 * matched to this {@link StepCandidate}. 
 * 
 * TODO remove map and use an sorted list.
 * 
 * @author Barnabas Sudy (barnabas.sudy@gmail.com)
 * @since 2012
 */
public class StepCandidate {

    public static class MethodParameter {

        private final String paramName;
        private final int    paramPos;

        /**
         * @param paramName
         * @param paramPos
         */
        public MethodParameter(final String paramName, final int paramPos) {
            super();
            this.paramName = paramName;
            this.paramPos  = paramPos;
        }

        public String getParamName() {
            return paramName;
        }
        public int getParamPos() {
            return paramPos;
        }

    }

    private final String                        stepDefinition;
    private final Method                        method;
    private final Pattern                       pattern;
    private final Map<Integer, MethodParameter> parameterPositions;


    /**
     * @param stepDefinition The step definition
     * @param method The method
     * @param parameterPositions The method parameters
     * @param regEx The regular expression
     */
    public StepCandidate(final String stepDefinition, 
    					 final Method method, 
    					 final Map<Integer, MethodParameter> parameterPositions, 
    					 final String regEx) {
        this.stepDefinition     = stepDefinition;
        this.method             = method;
        this.parameterPositions = Collections.unmodifiableMap(parameterPositions);
        this.pattern            = Pattern.compile(regEx);
    }

    public String getStepDefinition() {
        return stepDefinition;
    }

    public Method getMethod() {
        return method;
    }

    public Map<Integer, MethodParameter> getParameterPositions() {
        return parameterPositions;
    }

    public Pattern getPattern() {
        return pattern;
    }

}