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

import com.google.common.base.Splitter;
import io.spine.net.Uri.Protocol;
import io.spine.net.Uri.Schema;

/**
 * Parses given URL to {@link Uri} instance.
 */
@SuppressWarnings({
        "CheckReturnValue" /* of calls to methods of fields that are builders */,
        "PMD.BeanMembersShouldSerialize" /* which is dubious rule. */
})
final class UrlParser {

    private static final char SEMICOLON = ':';
    static final String PROTOCOL_ENDING = "://";
    static final char CREDENTIALS_ENDING = '@';
    static final char CREDENTIALS_SEPARATOR = SEMICOLON;
    static final char HOST_ENDING = '/';
    static final char HOST_PORT_SEPARATOR = SEMICOLON;

    static final char FRAGMENT_START = '#';
    static final char QUERIES_START = '?';
    static final char QUERY_SEPARATOR = '&';
    private static final Splitter querySplitter = Splitter.on(QUERY_SEPARATOR);

    private final String originalUrl;

    private Uri.Builder record;
    private String unProcessedInput;

    /**
     * Creates an new instance of {@code UrlParser} with given String URL to parse.
     *
     * @param url String URL to parse
     */
    UrlParser(String url) {
        this.originalUrl = url;
    }

    /**
     * Performs the parsing.
     */
    Uri parse() {
        init();

        parseProtocol();
        parseCredentials();
        parseFragment();
        parseQueries();
        parseHost();
        parsePath();


        return record.build();
    }

    /** Initializes the parser. */
    private void init() {
        record = Uri.newBuilder();
        unProcessedInput = originalUrl;
    }

    /**
     * Parses protocol from remembered URL String and saves it to the state.
     *
     * <ul>
     *     <li>If no suitable protocol found, saves UNDEFINED value.
     *     <li>If some value is found but the schema is unknown, saves raw value.
     * </ul>
     */
    private void parseProtocol() {
        var protocolBuilder = Protocol.newBuilder();
        var protocolEndingIndex = unProcessedInput.indexOf(PROTOCOL_ENDING);
        if (protocolEndingIndex == -1) {
            protocolBuilder.setSchema(Schema.UNDEFINED);
            record.setProtocol(protocolBuilder);
            return;
        }
        var protocol = unProcessedInput.substring(0, protocolEndingIndex);
        unProcessedInput = unProcessedInput.substring(protocolEndingIndex +
                                                      PROTOCOL_ENDING.length());

        var schema = Schemas.parse(protocol);

        if (schema == Schema.UNDEFINED) {
            protocolBuilder.setName(protocol);
        } else {
            protocolBuilder.setSchema(schema);
        }

        record.setProtocol(protocolBuilder.build());
    }

    /** Parses credentials from remembered URL String and saves them to the state. */
    private void parseCredentials() {
        var credentialsEndingIndex = unProcessedInput.indexOf(CREDENTIALS_ENDING);
        if (credentialsEndingIndex == -1) {
            return;
        }

        var credential = unProcessedInput.substring(0, credentialsEndingIndex);
        unProcessedInput = unProcessedInput.substring(credentialsEndingIndex + 1);

        var auth = Uri.Authorization.newBuilder();

        var credentialsSeparatorIndex = credential.indexOf(CREDENTIALS_SEPARATOR);
        if (credentialsSeparatorIndex != -1) {
            var userName = credential.substring(0, credentialsSeparatorIndex);
            var password = credential.substring(credentialsSeparatorIndex + 1);
            auth.setPassword(password);
            auth.setUserName(userName);
        } else {
            auth.setUserName(credential);
        }

        record.setAuth(auth.build());
    }

    /** Parses host and port and saves them to the state. */
    private void parseHost() {
        var hostEndingIndex = unProcessedInput.indexOf(HOST_ENDING);
        String host;

        if (hostEndingIndex == -1) {
            host = unProcessedInput;
            unProcessedInput = "";
        } else {
            host = unProcessedInput.substring(0, hostEndingIndex);
            unProcessedInput = unProcessedInput.substring(hostEndingIndex + 1);
        }

        var portIndex = host.indexOf(HOST_PORT_SEPARATOR);
        if (portIndex != -1) {
            var port = host.substring(portIndex + 1);
            record.setPort(port);
            var hostAddress = host.substring(0, portIndex);
            record.setHost(hostAddress);
        } else {
            record.setHost(host);
        }
    }

    /** Parses fragment and saves it to the state. */
    private void parseFragment() {
        var fragmentIndex = unProcessedInput.lastIndexOf(FRAGMENT_START);
        if (fragmentIndex == -1) {
            return;
        }

        var fragment = unProcessedInput.substring(fragmentIndex + 1);
        unProcessedInput = unProcessedInput.substring(0, fragmentIndex);

        record.setFragment(fragment);
    }

    /**
     * Parses query parameters and saves them to the state.
     *
     * @throws IllegalArgumentException in case of bad-formed parameter
     */
    private void parseQueries() {
        var queriesStartIndex = unProcessedInput.indexOf(QUERIES_START);
        if (queriesStartIndex == -1) {
            return;
        }

        var queriesString = unProcessedInput.substring(queriesStartIndex + 1);
        unProcessedInput = unProcessedInput.substring(0, queriesStartIndex);

        Iterable<String> queries = querySplitter.split(queriesString);
        for (var query : queries) {
            var param = UrlQueryParameters.parse(query);
            record.addQuery(param);
        }
    }

    /** Parses the URL resource path from the remaining part of URL. */
    private void parsePath() {
        if (unProcessedInput.isEmpty()) {
            return;
        }
        record.setPath(unProcessedInput);
        unProcessedInput = "";
    }
}
