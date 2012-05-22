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

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;

import com.moresby.have.annotations.Given;


/**
 * TODO javadoc.
 *
 * @author Barnabas Sudy (barnabas.sudy@gmail.com)
 * @since 2012
 */
public class MyTests {

    @Given("given text")
    public void givenMethod(final String firstParam, final String secondParam) {
        System.out.println("givenMethod");
    }

    @Given("second text")
    public void secondGivenMethod(final String firstParam, final String secondParam) {
        System.out.println("secondGivenMethod");
    }

    @Test
    public void test() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        new mByHave(this);
    }

    @Test
    @Ignore
    public void regExpTest() {

        final String regex = "blah (\\w*) blah";
        final String input = "blah blah2 blah";

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(input);

        while(matcher.find()) {
            System.out.println("Find: " + matcher.start() + " - " + matcher.end() + " cont: " + matcher.group() + " " + matcher.groupCount());
            for (int i = 0; i <= matcher.groupCount(); i++) {
                System.out.println("Group: " + matcher.group(i));
            }
        }




    }
}

