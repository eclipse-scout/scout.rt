/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.scout.rt.server.internal.ServerSessionClassFinder;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link ServerSessionClassFinder}
 */
public class ServerSessionClassFinderTest {
  final IExtensionPoint servletExtensionPoint = mock(IExtensionPoint.class);
  final IExtension servletConfig = mock(IExtension.class);
  IContributor contrib = mock(IContributor.class);
  IConfigurationElement servletExtension = mock(IConfigurationElement.class);

  @Before
  public void setup() {
    when(servletExtensionPoint.getExtensions()).thenReturn(new IExtension[]{servletConfig});
    when(servletExtension.getName()).thenReturn("servlet");
    when(servletExtension.getAttribute("class")).thenReturn("org.eclipse.scout.rt.server.ServiceTunnelServlet");
    when(servletConfig.getConfigurationElements()).thenReturn(new IConfigurationElement[]{servletExtension});
    when(contrib.getName()).thenReturn("org.eclipse.scout.rt.server");
    when(servletExtension.getContributor()).thenReturn(contrib);
  }

  @Test
  public void findServletContributorBundle() throws Exception {
    assertNotNull(getClassFinderNoAlias().findServletContributorBundle(null));
    assertNotNull(getClassFinderWithAlias().findServletContributorBundle(null));
  }

  @Test
  public void testFindServletContributorBundleWithAlias() throws Exception {
    assertNull(getClassFinderNoAlias().findServletContributorBundle("/process"));
    assertNotNull(getClassFinderWithAlias().findServletContributorBundle("/process"));
  }

  @Test
  public void testFindClassByConvention() {
    final Class<? extends IServerSession> clazz = getClassFinderWithAlias().findClassByConvention("/process");
    assertNull("No class org.eclipse.scout.rt.server.ServerSession available", clazz);
  }

  private ServerSessionClassFinder getClassFinderWithAlias() {
    return new ServerSessionClassFinder() {
      @Override
      protected IExtensionPoint getServletExtensionPoint() {
        when(servletExtension.getAttribute("alias")).thenReturn("/process");
        when(contrib.getName()).thenReturn("org.eclipse.scout.rt.server");
        return servletExtensionPoint;
      }
    };
  }

  private ServerSessionClassFinder getClassFinderNoAlias() {
    return new ServerSessionClassFinder() {
      @Override
      protected IExtensionPoint getServletExtensionPoint() {
        return servletExtensionPoint;
      }
    };
  }

}
