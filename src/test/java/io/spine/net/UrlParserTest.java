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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link io.spine.net.UrlParser}.
 */
@DisplayName("`UrlParser` should parse")
class UrlParserTest {

    private static final String HOST = "ulr-parser-should.com";
    private static final String HTTP_PROTOCOL = "http";
    private static final String UNKNOWN_PROTOCOL = "http5";
    private static final String PROTOCOL_HOST = HTTP_PROTOCOL + "://" + HOST;
    private static final String UNKNOWN_PROTOCOL_HOST = UNKNOWN_PROTOCOL + "://" + HOST;
    private static final String PORT = "8080";

    @Test
    @DisplayName("protocol and host")
    void protocolAndHost() {
        var record = new UrlParser(PROTOCOL_HOST).parse();
        assertEquals(HOST, record.getHost());
        assertEquals(Uri.Schema.HTTP, record.getProtocol()
                                            .getSchema());
    }

    @Test
    @DisplayName("host")
    void host() {
        var record = new UrlParser(HOST).parse();

        assertEquals(HOST, record.getHost());
        assertEquals(Uri.Schema.UNDEFINED, record.getProtocol()
                                                 .getSchema());
    }

    @Test
    @DisplayName("unknown protocol")
    void unknownProtocol() {
        var record = new UrlParser(UNKNOWN_PROTOCOL_HOST).parse();
        assertEquals(UNKNOWN_PROTOCOL, record.getProtocol()
                                             .getName());
    }

    @Test
    @DisplayName("credentials")
    void credentials() {
        var userName = "admin";
        var password = "root";

        var userUrl = HTTP_PROTOCOL + "://" + userName + '@' + HOST;
        var userPasswordUrl = HTTP_PROTOCOL + "://" + userName + ':' + password + '@' + HOST;

        var record1 = new UrlParser(userUrl).parse();
        var user1 = record1.getAuth().getUserName();
        assertEquals(userName, user1);

        var record2 = new UrlParser(userPasswordUrl).parse();
        var auth2 = record2.getAuth();
        var user2 = auth2.getUserName();
        assertEquals(userName, user2);
        assertEquals(password, auth2.getPassword());
    }

    @Test
    @DisplayName("port")
    void port() {
        var url = HOST + ':' + PORT;

        var parsedUrl = new UrlParser(url).parse();

        assertEquals(PORT, parsedUrl.getPort());
    }

    @Test
    @DisplayName("path")
    void path() {
        var resource = "index/2";
        var rawUrl = HOST + '/' + resource;

        var url = new UrlParser(rawUrl).parse();

        assertEquals(resource, url.getPath());
    }

    @Test
    @DisplayName("fragment")
    void fragment() {
        var fragment = "reference";
        var rawUrl = HOST + "/index/2#" + fragment;

        var url = new UrlParser(rawUrl).parse();

        assertEquals(fragment, url.getFragment());
    }

    @Test
    @DisplayName("queries")
    void queries() {
        var key1 = "key1";
        var key2 = "key2";

        var value1 = "value1";
        var value2 = "value2";

        var query1 = key1 + '=' + value1;
        var query2 = key2 + '=' + value2;

        var rawUrl = HOST + '?' + query1 + '&' + query2;

        var url = new UrlParser(rawUrl).parse();

        var queries = url.getQueryList();

        assertEquals(2, queries.size());

        var queryInstance1 = queries.get(0);
        var queryInstance2 = queries.get(1);

        assertEquals(key1, queryInstance1.getKey());
        assertEquals(value1, queryInstance1.getValue());
        assertEquals(key2, queryInstance2.getKey());
        assertEquals(value2, queryInstance2.getValue());
    }

    @Test
    @DisplayName("URL with all sub-items")
    void urlWithAllSubItems() {
        var rawUrl = "https://user:password@spine.io/index?auth=none&locale=us#fragment9";

        var record = new UrlParser(rawUrl).parse();

        assertEquals(Uri.Schema.HTTPS, record.getProtocol()
                                             .getSchema());
        assertEquals("user", record.getAuth()
                                   .getUserName());
        assertEquals("password", record.getAuth()
                                       .getPassword());
        assertEquals("spine.io", record.getHost());
        assertEquals("index", record.getPath());
        assertEquals("auth=none", UrlQueryParameters.toString(record.getQuery(0)));
        assertEquals("locale=us", UrlQueryParameters.toString(record.getQuery(1)));
        assertEquals("fragment9", record.getFragment());
    }
}
