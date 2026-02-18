/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.client.chunked;

import java.io.Closeable;

import jakarta.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.CloseableIterator;
import org.eclipse.scout.rt.rest.client.IRestClientHelper;

/**
 * Reader for a stream of chunked data of type {@code T} e.g. by using a {@link IRestClientHelper}.
 * <p>
 * Example:
 *
 * <pre>
 * Example:
 *
 * Response response = helper()
 *     .target("chunked")
 *     .request()
 *     .accept(MediaType.APPLICATION_JSON)
 *     .get();
 *
 * IChunkedDataReader&#060;ExampleDo&#062; reader = IChunkedDataReader.create(response, ExampleDo.class, "\r\n");
 * ExampleDo chunk;
 * while ((chunk = reader.read()) != null) {
 *   // process chunk
 * }
 * </pre>
 */
@Bean
public interface IChunkedDataReader<T> extends Closeable {

  /**
   * @return IChunkedDataReader for given {@code response} providing chunks of type {@code type}, the default delimiter {@code \r\n}.
   */
  static <T> IChunkedDataReader<T> create(Response response, Class<T> type) {
    return create(response, type, null);
  }

  /**
   * @return IChunkedDataReader for given {@code response} providing chunks of type {@code type}, using given {@code delimiter}.
   * Provide {@code null} as delimiter to use the default delimiter {@code \r\n}.
   */
  static <T> IChunkedDataReader<T> create(Response response, Class<T> type, String delimiter) {
    //noinspection unchecked
    IChunkedDataReader<T> reader = BEANS.get(IChunkedDataReader.class);
    reader.init(response, type, delimiter);
    return reader;
  }

  /**
   * Initialize reader for given {@code response} providing chunks of type {@code type}, using given {@code delimiter}.
   * Provide {@code null} as delimiter to use the default delimiter {@code \r\n}.
   *
   * @throws AssertionException
   *     if already initialized
   */
  void init(Response response, Class<T> type, String delimiter);

  /**
   * @return next chunk of data or {@code null} if no more data available.
   */
  T read();

  /**
   * @return {@code true} if stream was closed, otherwise {@code false}.
   */
  boolean isClosed();

  /**
   * @return new iterator consuming reader
   */
  CloseableIterator<T> iterator();
}
