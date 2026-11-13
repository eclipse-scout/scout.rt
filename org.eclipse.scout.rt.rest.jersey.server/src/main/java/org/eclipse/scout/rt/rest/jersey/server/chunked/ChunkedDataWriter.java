/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.server.chunked;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

import jakarta.ws.rs.core.GenericType;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.ConnectionErrorDetector;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.rest.chunked.IChunkedDataWriter;
import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scout REST API wrapper around Jersey {@link ChunkedOutput} instance.
 * <p>
 * See documentation
 * <a href="https://eclipse-ee4j.github.io/jersey.github.io/documentation/latest/user-guide.html#chunked-output">Jersey chunked output</a>
 */
public class ChunkedDataWriter<T> implements IChunkedDataWriter<T> {

  private static final Logger LOG = LoggerFactory.getLogger(ChunkedDataWriter.class);

  protected ChunkedOutput<T> m_output;

  @Override
  public void init(Class<T> type, String delimiter, int queueCapacity) {
    assertNull(m_output, "already initialized");

    // Use delimiter specified by org.glassfish.jersey.client.ChunkedInput.parser (e.g. "\r\n") if no custom delimiter was specified.
    // Providing a null or empty delimiter to ChunkedOutput leads to no delimiter at all which cannot be parsed by ChunkedInput later on.
    // See https://stackoverflow.com/questions/40429196/chunkedoutput-response-from-jersey-rest-services
    byte[] delimiterBytes = (StringUtility.isNullOrEmpty(delimiter) ? "\r\n" : delimiter).getBytes();
    m_output = new ChunkedOutputEx<>(type, delimiterBytes, queueCapacity);
  }

  @Override
  public GenericType<T> toEntity() {
    return m_output;
  }

  @Override
  public void write(T chunk) throws IOException {
    m_output.write(chunk);
  }

  @Override
  public boolean isClosed() {
    return m_output.isClosed();
  }

  @Override
  public void close() throws IOException {
    m_output.close();
  }

  @Override
  public void writeAsync(IRunnable runnable) {
    CountDownLatch jobStarted = new CountDownLatch(1);
    Jobs.schedule(() -> {
      jobStarted.countDown();
      try (ChunkedDataWriter<T> ignored = ChunkedDataWriter.this) {
        runnable.run();
      }
      catch (IOException e) {
        if (BEANS.get(ConnectionErrorDetector.class).isConnectionError(e)) {
          // Ignore disconnect errors: we do not want to throw an exception, if the client closed the connection.
          LOG.debug("Connection error for data load operation", e);
        }
        else {
          throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
        }
      }
    }, Jobs.newInput()
        .withName("ChunkedDataWriter")
        .withRunContext(RunContexts.copyCurrent()));
    try {
      jobStarted.await();
    }
    catch (InterruptedException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  /**
   * {@link ChunkedOutput} with serialized {@link #flushQueue()} invocations. Workaround for a race condition in Jersey's
   * {@link ChunkedOutput} (observed with Jersey 4.0.0) that leads to empty or truncated chunked responses without any
   * error on the server side.
   * <p>
   * <b>Problem</b><br>
   * {@code ChunkedOutput.flushQueue()} is invoked concurrently by two threads:
   * <ul>
   * <li><b>Container thread:</b> once the resource method returned the response, Jersey calls the package-private
   * {@code ChunkedOutput.setContext(...)}, which attaches the response context and flushes all queued chunks.</li>
   * <li><b>Writer thread:</b> the job scheduled by {@link ChunkedDataWriter#writeAsync(IRunnable)} calls
   * {@link #write(Object)} for each chunk and finally {@link #close()}, both of which flush the queue too.</li>
   * </ul>
   * <b>Workaround</b><br>
   * {@link #flushQueue()} is overridden as {@code synchronized}. Because {@code setContext(...)}, {@link #write(Object)}
   * and {@link #close()} all call {@code flushQueue()} via virtual dispatch, all flushes are serialized: a
   * {@link #close()} issued by the writer thread waits until the container thread's flush has completed and then closes
   * the response regularly after all chunks have been written.
   * <p>
   * <b>Queue capacity</b><br>
   * The {@link ChunkedOutput.TypedBuilder} (the only way to configure both chunk type and queue capacity) creates
   * instances via a private constructor and therefore cannot be used for subclasses, and {@code GenericType.getType()}
   * is final. Hence, this class uses the public {@link ChunkedOutput#ChunkedOutput(java.lang.reflect.Type, byte[])}
   * constructor, which creates an unbounded queue, and replaces the private final {@code queue} field by reflection with
   * a bounded queue if {@code queueCapacity > 0} (same semantics as {@code ChunkedOutput.Builder#queueCapacity(int)}).
   * <p>
   * TODO pbz: Check with newer Jersey implementations, see Jersey issue https://github.com/eclipse-ee4j/jersey/issues/6150
   */
  static class ChunkedOutputEx<T> extends ChunkedOutput<T> {
    ChunkedOutputEx(Class<T> type, byte[] delimiterBytes, int queueCapacity) {
      super(type, delimiterBytes); // creates an unbounded queue
      if (queueCapacity <= 0) {
        return; // same semantics as ChunkedOutput.Builder#queueCapacity: values <= 0 mean unbounded
      }
      try {
        Field field = ChunkedOutput.class.getDeclaredField("queue");
        field.setAccessible(true);
        field.set(this, new LinkedBlockingDeque<>(queueCapacity));
      }
      catch (NoSuchFieldException | IllegalAccessException e) {
        throw new PlatformException("failed to init chunked output queue", e);
      }
    }

    @Override
    protected synchronized void flushQueue() throws IOException {
      super.flushQueue();
    }
  }
}
