/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.http;

import java.io.IOException;

import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;

public class BasicAuthHttpTransportManager extends AbstractHttpTransportManager {

  private String m_user;
  private String m_password;

  @Override
  public String getName() {
    return "scout.transport.basic-auth";
  }

  public BasicAuthHttpTransportManager withUser(String user) {
    m_user = user;
    return this;
  }

  public BasicAuthHttpTransportManager withPassword(String password) {
    m_password = password;
    return this;
  }

  @Override
  protected HttpRequestInitializer createHttpRequestInitializer() {
    return new BasicAuthRequestInitializer(m_user, m_password);
  }

  public static class BasicAuthRequestInitializer extends DefaultHttpRequestInitializer implements HttpExecuteInterceptor {
    private String m_user;
    private String m_password;

    public BasicAuthRequestInitializer(String user, String password) {
      m_user = user;
      m_password = password;
    }

    @Override
    public void initialize(HttpRequest request) throws IOException {
      super.initialize(request);
      request.setInterceptor(this);
    }

    @Override
    public void intercept(HttpRequest request) {
      request.getHeaders().setBasicAuthentication(m_user, m_password);
    }
  }
}
