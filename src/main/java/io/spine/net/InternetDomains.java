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

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.net.Patterns.HOST_NAME_PATTERN;
import static java.util.Objects.requireNonNull;

/**
 * Utility class for working with {@link InternetDomain}s.
 */
public final class InternetDomains {

    /** Prevent instantiation of this  utility class. */
    private InternetDomains() {
    }

    /**
     * Obtains {@code Pattern} for checking an Internet host name.
     *
     * <p>The pattern does not accept IP addresses.
     */
    public static Pattern pattern() {
        return HOST_NAME_PATTERN;
    }

    /**
     * Verifies if the passed sequence is a valid internet domain name.
     */
    public static boolean isValid(String name) {
        var matcher = pattern().matcher(name);
        var result = matcher.matches();
        return result;
    }

    /**
     * Obtains string representation of the passed internet domain.
     */
    public static String toString(InternetDomain domain) {
        checkNotNull(domain);
        var result = NetStringifiers.forInternetDomain().convert(domain);
        return requireNonNull(result);
    }

    /**
     * Creates a new {@code InternetDomain} instance for the passed name.
     *
     * @param name
     *         a valid Internet domain name
     * @return new {@code InternetDomain} instance
     * @throws IllegalArgumentException
     *         if the passed domain name is not valid
     */
    public static InternetDomain valueOf(String name) {
        checkNotNull(name);
        checkArgument(isValid(name));
        var converter = NetStringifiers.forInternetDomain().reverse();
        var result = converter.convert(name);
        return requireNonNull(result);
    }
}
