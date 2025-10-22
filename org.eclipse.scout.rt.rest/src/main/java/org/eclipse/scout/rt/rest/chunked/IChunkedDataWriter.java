/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.chunked;

import java.io.Closeable;
import java.io.IOException;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.rest.IRestResource;

/**
 * Writer for a stream of chunked data of type {@code T} within a {@link IRestResource}.
 * <p>
 * Example:
 *
 * <pre>
 * &#064;GET
 * &#064;Path("chunked")
 * &#064;Produces(MediaType.APPLICATION_JSON)
 * public Response load() {
 *   IChunkedDataWriter&#060;ExampleDo&#062; writer = IChunkedDataWriter.create(ExampleDo.class, "\r\n", 100);
 *   Jobs.schedule(() -> {
 *     try (writer) {
 *       // write all ExampleDo instances to writer
 *       writer.write(...);
 *     }
 *   }, Jobs.newInput());
 *   return Response.ok(writer.toEntity()).build();
 * }
 * </pre>
 */
@Bean
public interface IChunkedDataWriter<T> extends Closeable {

  /**
   * @return IChunkedDataWriter for chunks of type {@code type}, using the default delimiter {@code \r\n} and a queue capacity of 100 chunks.
   */
  static <T> IChunkedDataWriter<T> create(Class<T> type) {
    return create(type, null, 100);
  }

  /**
   * @return IChunkedDataWriter for chunks of type {@code type}, using the given {@code delimiter} and the given {@code queueCapacity}.
   * Provide {@code null} as delimiter to use the default delimiter {@code \r\n}.
   * If the queue capacity (e.g. enqueued chunks) is greater than 0, the queue is bounded and will block writing when full. Use {@code -1} for an unbounded queue.
   * Caution: the queue is hold in memory until reader consumes the stream.
   */
  static <T> IChunkedDataWriter<T> create(Class<T> type, String delimiter, int queueCapacity) {
    //noinspection unchecked
    IChunkedDataWriter<T> writer = BEANS.get(IChunkedDataWriter.class);
    writer.init(type, delimiter, queueCapacity);
    return writer;
  }

  /**
   * Initialize writer for chunks of type {@code type}, using the given {@code delimiter} and the given {@code queueCapacity}.
   * Provide {@code null} as delimiter to use the default delimiter {@code \r\n}.
   * If the queue capacity (e.g. enqueued chunks) is greater than 0, the queue is bounded and will block writing when full. Use {@code -1} for an unbounded queue.
   * Caution: the queue is hold in memory until reader consumes the stream.
   *
   * @throws AssertionException
   *     if already initialized
   */
  void init(Class<T> type, String delimiter, int queueCapacity);

  /**
   * @return writer as entity to be used as response payload, see {@code Response#ok(entity)}
   */
  GenericType<T> toEntity();

  /**
   * Writes given {@code chunk} into the wrapped {@link Response}.
   */
  void write(T chunk) throws IOException;

  /**
   * @return {@code true} if stream was closed, otherwise {@code false}.
   */
  boolean isClosed();
}
