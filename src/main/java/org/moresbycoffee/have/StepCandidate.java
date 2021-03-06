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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
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
public class StepCandidate implements Comparable<StepCandidate> {

    public static class MethodParameter {

        private final String paramName;
        private final int    paramPos;
        private final Type   paramType;
        
        /**
         * @param paramName The name of the parameter.
         * @param paramPos The position of the parameter.
         * @param paramType The type of the parameter.
         */
        public MethodParameter(final String paramName, final int paramPos, final Type paramType) {
            super();
            this.paramName = paramName;
            this.paramPos  = paramPos;
            this.paramType = paramType;
        }

        public String getParamName() {
            return paramName;
        }
        
        public int getParamPos() {
            return paramPos;
        }
        
        public Type getType() {
            return paramType;
        }

    }

    private final String                        stepDefinition;
    private final Method                        method;
    private final Pattern                       pattern;
    private final Map<Integer, MethodParameter> parameterPositions;
    private final List<MethodParameter>         returnValueParameters;
    /** The priority of the step candidate. The higher value should be picked up first. */
    private final int                           priority;


    /**
     * @param stepDefinition The step definition
     * @param method The method
     * @param parameterPositions The method parameters
     * @param regEx The regular expression
     * @param int priority The priority of the step candidate. The higher value should be picked up first.
     */
    public StepCandidate(final String stepDefinition,
    					 final Method method,
    					 final Map<Integer, MethodParameter> parameterPositions,
    					 final List<MethodParameter> returnValueParameters,
    					 final String regEx,
    					 final int priority) {
        this.stepDefinition        = stepDefinition;
        this.method                = method;
        this.parameterPositions    = Collections.unmodifiableMap(parameterPositions);
        this.returnValueParameters = Collections.unmodifiableList(returnValueParameters);
        this.pattern               = Pattern.compile(regEx);
        this.priority              = priority;
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
    
    public List<MethodParameter> getReturnValueParameters() {
        return returnValueParameters;
    }

    public Pattern getPattern() {
        return pattern;
    }
    
    /**
     * @return the priority The priority of the step candidate. The higher value should be picked up first.
     */
    public int getPriority() {
        return priority;
    }
    
    /** {@inheritDoc} */
    public int compareTo(StepCandidate that) {
        if (that == null) {
            return 1;
        }
        if (this.equals(that)) {
            return 0;
        }
        
        /* Check priority */
        int result = (this.priority < that.priority) ? 1 : ((this.priority == that.priority) ? 0 : -1);
        
        /* Check step definition string. */
        if (result == 0) {
            result = this.stepDefinition.compareTo(that.stepDefinition);
        }
        
        /* Compare methods. */
        if (result == 0) {
            result = this.method.toString().compareTo(that.method.toString());
        }
        
        /* As a last attempt compare hash code. */
        if (result == 0) {
            result = (this.hashCode() < that.hashCode()) ? -1 : ((this.hashCode() == that.hashCode()) ? 0 : 1);
        }
         
        return result;
    }

}