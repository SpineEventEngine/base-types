/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.net.string;

import io.spine.net.EmailAddress;
import io.spine.net.InternetDomain;
import io.spine.net.Url;
import io.spine.string.Stringifier;
import io.spine.string.StringifierRegistry;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.net.string.NetStringifiers.forEmailAddress;
import static io.spine.net.string.NetStringifiers.forInternetDomain;
import static io.spine.net.string.NetStringifiers.forUrl;

@DisplayName("`NetStringifiers` utility should")
class NetStringifiersTest extends UtilityClassTest<NetStringifiers> {

    NetStringifiersTest() {
        super(NetStringifiers.class);
    }

    @Test
    @DisplayName("register stringifiers")
    void registration() {
        assertRegistered(forUrl(), Url.class);
        assertRegistered(forEmailAddress(), EmailAddress.class);
        assertRegistered(forInternetDomain(), InternetDomain.class);
    }

    private static <T> void assertRegistered(Stringifier<T> stringifier, Class<T> cls) {
        Optional<Stringifier<T>> optional = registry().find(cls);
        assertThat(optional).hasValue(stringifier);
    }

    @Nested
    @DisplayName("convert to `String` and back")
    class Convert {

        @Test
        @DisplayName("`Url`")
        void url() {
            assertStringifier(Url.class, "https://spine.io/about");
        }

        @Test
        @DisplayName("`InternetDomain`")
        void internetDomain() {
            assertStringifier(InternetDomain.class, "spine.io");
        }

        @Test
        @DisplayName("`EmailAddress`")
        void emailAddress() {
            assertStringifier(EmailAddress.class, "info@spine.io");
        }

        <T> void assertStringifier(Class<T> cls, String stringForm) {
            // Check that there is a stringifier for the passed class.
            Optional<Stringifier<T>> optional = registry().find(cls);
            assertThat(optional).isPresent();

            var stringifier = optional.get();

            // Parse the object back from the passed string representation.
            var parser = stringifier.reverse();
            var object = parser.convert(stringForm);

            // Now check that object is stringified to the passed form.
            var stringifierForm = stringifier.convert(object);

            assertThat(stringifierForm).isEqualTo(stringForm);
        }
    }

    private static StringifierRegistry registry() {
        return StringifierRegistry.instance();
    }
}
