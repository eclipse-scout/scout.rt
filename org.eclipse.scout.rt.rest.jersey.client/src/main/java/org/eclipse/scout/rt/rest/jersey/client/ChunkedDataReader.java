/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.client;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNull;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.rest.client.chunked.IChunkedDataReader;
import org.glassfish.jersey.client.ChunkedInput;

/**
 * Scout REST API wrapper around Jersey {@link ChunkedInput} instance.
 * <p>
 * See documentation
 * <a href="https://eclipse-ee4j.github.io/jersey.github.io/documentation/latest/user-guide.html#d0e10936">Jersey chunked input</a>
 */
public class ChunkedDataReader<T> implements IChunkedDataReader<T> {

  protected ChunkedInput<T> m_input;

  @Override
  public void init(Response response, Class<T> type, String delimiter) {
    assertNull(m_input, "already initialized");

    m_input = response.readEntity(new GenericType<>(new ParameterizedType() {
      @Override
      public Type[] getActualTypeArguments() {
        return new Type[]{type};
      }

      @Override
      public Type getRawType() {
        return ChunkedInput.class;
      }

      @Override
      public Type getOwnerType() {
        return null;
      }
    }));

    // Use default parser if no custom delimiter was specified.
    // See org.glassfish.jersey.client.ChunkedInput.parser (uses default delimiter "\r\n")
    if (!StringUtility.isNullOrEmpty(delimiter)) {
      m_input.setParser(ChunkedInput.createParser(delimiter));
    }
  }

  @Override
  public T read() {
    return m_input.read();
  }

  @Override
  public boolean isClosed() {
    return m_input.isClosed();
  }

  @Override
  public void close() throws IOException {
    m_input.close();
  }
}
