/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.authentication;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.server.commons.authentication.ServiceTunnelAccessTokenAccessController.ServiceTunnelAccessTokenAuthConfig;
import org.eclipse.scout.rt.shared.servicetunnel.http.DefaultAuthTokenVerifier;
import org.junit.Test;

public class ServiceTunnelAccessTokenAccessControllerTest {

  @IgnoreBean
  protected static class MockAuthTokenVerifier extends DefaultAuthTokenVerifier {
    protected boolean m_enabled;

    protected MockAuthTokenVerifier(boolean enabled) {
      m_enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
      return m_enabled;
    }
  }

  @Test
  public void testIsEnabled() {
    ServiceTunnelAccessTokenAccessController controller = BEANS.get(ServiceTunnelAccessTokenAccessController.class);
    assertFalse(controller.isEnabled());

    controller.init();
    assertFalse(controller.isEnabled());

    controller.init(new ServiceTunnelAccessTokenAuthConfig().withTokenVerifier(new MockAuthTokenVerifier(false)));
    assertFalse(controller.isEnabled());

    controller.init(new ServiceTunnelAccessTokenAuthConfig().withTokenVerifier(new MockAuthTokenVerifier(true)).withTokenClazz(null));
    assertFalse(controller.isEnabled());

    controller.init(new ServiceTunnelAccessTokenAuthConfig().withTokenVerifier(new MockAuthTokenVerifier(true)).withPrincipalProducer2(null));
    assertFalse(controller.isEnabled());

    controller.init(new ServiceTunnelAccessTokenAuthConfig().withTokenVerifier(new MockAuthTokenVerifier(true)).withEnabled(false));
    assertFalse(controller.isEnabled());

    controller.init(new ServiceTunnelAccessTokenAuthConfig().withTokenVerifier(null));
    assertFalse(controller.isEnabled());

    controller.init(new ServiceTunnelAccessTokenAuthConfig().withTokenVerifier(new MockAuthTokenVerifier(true)));
    assertTrue(controller.isEnabled());
  }
}
