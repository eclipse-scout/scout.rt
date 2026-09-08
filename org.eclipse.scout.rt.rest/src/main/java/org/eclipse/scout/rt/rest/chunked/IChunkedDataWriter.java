/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
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
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
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
 *   return writer.toResponse(() -> fetchData()));   // load data from source, e.g. database
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

  /**
   * Schedules a new job to write data asynchronously. The provided runnable calls {@link #write} on this object.<br/>
   * Scheduled job copies current run context.
   */
  void writeAsync(IRunnable runnable);

  /**
   * Schedules job which consumes given iterator and returns response created by {@link #toEntity()}.
   *
   * @deprecated Instead, use {@link #toResponse(Supplier)}
   */
  @Deprecated(forRemoval = true, since = "26.2")
  default Response toResponse(Iterator<T> iterator) {
    writeAsync(() -> {
      while (iterator.hasNext()) {
        write(iterator.next());
      }
    });
    return Response.ok(toEntity()).build();
  }

  /**
   * Schedules job that invokes given supplier and that writes its values, and returns a response created by {@link #toEntity()}.
   * The iterator supplier as well as the iteration loop are performed within the same job in order to use
   * the same transaction. This method blocks until the supplier completes and re-throws any unchecked exception.
   */
  default Response toResponse(Supplier<Iterator<T>> iteratorSupplier) {
    final CountDownLatch supplierDone = new CountDownLatch(1);
    final AtomicReference<Throwable> uncheckedException = new AtomicReference<>();

    writeAsync(() -> {
      // invoke supplier within the same transaction that also runs over the iterator,
      // so that txn-related resources are still available (e.g. db connection)
      Iterator<T> iterator;
      try {
        iterator = iteratorSupplier.get();
      }
      catch (RuntimeException | Error e) {
        uncheckedException.set(e);
        throw e;
      }
      finally {
        supplierDone.countDown();
      }

      // write chunked data
      while (iterator.hasNext()) {
        write(iterator.next());
      }
    });

    // wait for the supplier to complete
    try {
      supplierDone.await();
    }
    catch (InterruptedException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }

    // rethrow any unchecked exception occurred by the supplier
    Throwable throwable = uncheckedException.get();
    if (throwable instanceof RuntimeException runtimeException) {
      throw runtimeException;
    }
    if (throwable instanceof Error error) {
      throw error;
    }

    return Response.ok(toEntity()).build();
  }
}
