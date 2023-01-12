/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http;

import org.eclipse.scout.rt.shared.servicetunnel.http.MultiSessionCookieStoreInstaller;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport.Builder;

/**
 * <p>
 * Factory to create the {@link NetHttpTransport} instances.
 * </p>
 * <p>
 * Unfortunately these transports do not support cookie handling per instance. Several settings must be set VM wide. If
 * cookies per session should be used it might be helpful to activate {@link MultiSessionCookieStoreInstaller}.
 * </p>
 */
public class NetHttpTransportFactory implements IHttpTransportFactory {

  @Override
  public HttpTransport newHttpTransport(IHttpTransportManager manager) {
    Builder builder = new Builder();

    interceptNewHttpTransport(builder, manager);
    manager.interceptNewHttpTransport(new NetHttpTransportBuilder(builder));

    return builder.build();
  }

  /**
   * Intercept the building of the new {@link HttpTransport}.
   */
  protected void interceptNewHttpTransport(Builder builder, IHttpTransportManager manager) {
    // nop
  }

  public static class NetHttpTransportBuilder implements IHttpTransportBuilder {
    private final Builder m_builder;

    public NetHttpTransportBuilder(Builder builder) {
      m_builder = builder;
    }

    public Builder getBuilder() {
      return m_builder;
    }
  }
}
