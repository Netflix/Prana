/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.prana.http;

import io.netty.handler.codec.http.HttpResponseStatus;
import rx.Observable;

import java.util.List;

/**
 * A context is a collection of functions to aid in the request handling process
 */
public interface Context {

    /**
     * Serializes an object and writes the bytes to the response stream
     *
     * @param object response object
     */
    Observable<Void> send(Object object);

    /**
     * Sends a simple text message back through the response stream
     *
     * @param message the message to deliver
     */
    Observable<Void> sendSimple(String message);

    /**
     * Serializes a simple error message back to the response using the supplied status code
     *
     * @param status the status code for the response
     * @param message the message to send back to the caller
     */
    Observable<Void> sendError(HttpResponseStatus status, String message);

    /**
     * Retrieves the value of the specified header
     *
     * @param name the name of the header to retrieve
     * @return the header value
     */
    String getHeader(String name);

    /**
     * Sets the response header value
     *
     * @param name the header's name
     * @param value the value of the header
     */
    void setHeader(String name, String value);

    /**
     * Convenience method for retrieving a query parameter off of the request
     *
     * @param key the query parameter
     * @return the query param value
     */
    String getQueryParam(String key);

    /**
     * Convenience method for retrieving a query parameter off of the request that contains multiple values
     *
     * @param key the name of the query parameter
     * @return the list of values from the query parameter
     */
    List<String> getQueryParams(String key);
}
