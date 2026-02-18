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
import java.util.concurrent.CountDownLatch;

import jakarta.ws.rs.core.GenericType;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
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

    m_output = ChunkedOutput.<T> builder(type)
        .chunkDelimiter(delimiterBytes)
        .queueCapacity(queueCapacity)
        .build();
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
}
