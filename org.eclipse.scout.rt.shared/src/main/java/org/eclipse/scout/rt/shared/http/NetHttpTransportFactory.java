/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
