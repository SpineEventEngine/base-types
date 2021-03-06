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

package io.spine.net;

import io.spine.net.string.NetStringifiers;
import io.spine.string.Stringifier;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.net.Patterns.EMAIL_ADDRESS;
import static java.util.Objects.requireNonNull;

/**
 * Utility class for working with {@link EmailAddress}es.
 */
public final class EmailAddresses {

    /** Prevent instantiation of this utility class. */
    private EmailAddresses() {
    }

    /**
     * Obtains pattern for validating email addresses.
     */
    public static Pattern pattern() {
        return EMAIL_ADDRESS;
    }

    /**
     * Verifies if the passed sequence is a valid email address.
     */
    public static boolean isValid(String value) {
        var matcher = pattern().matcher(value);
        var result = matcher.matches();
        return result;
    }

    /**
     * Obtains string representation of the passed email address.
     */
    public static String toString(EmailAddress address) {
        checkNotNull(address);
        var result = stringifier().convert(address);
        return requireNonNull(result);
    }

    /**
     * Creates a new {@code EmailAddress} instance for the passed value.
     *
     * @param value a valid email address
     * @return new {@code EmailAddress} instance
     * @throws IllegalArgumentException if the passed email address is not valid
     */
    public static EmailAddress valueOf(String value) {
        checkNotNull(value);
        checkArgument(isValid(value));
        var result = stringifier().reverse().convert(value);
        return requireNonNull(result);
    }

    private static Stringifier<EmailAddress> stringifier() {
        return NetStringifiers.forEmailAddress();
    }
}
