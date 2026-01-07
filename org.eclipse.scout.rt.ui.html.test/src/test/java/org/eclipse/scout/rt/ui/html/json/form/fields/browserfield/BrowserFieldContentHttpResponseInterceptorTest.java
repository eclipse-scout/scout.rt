/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.browserfield;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserFieldUIFacade;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.security.csp.BlockAllContentSecurityPolicy;
import org.eclipse.scout.rt.security.csp.ConfigurableContentSecurityPolicy;
import org.eclipse.scout.rt.security.csp.ContentSecurityPolicy;
import org.junit.Test;

public class BrowserFieldContentHttpResponseInterceptorTest {
  @Test
  public void testNoConfig() {
    Map<String, ContentSecurityPolicy> m1 = new HashMap<>();
    assertEquals(BEANS.get(ConfigurableContentSecurityPolicy.class), getInterceptor("whatever", m1).getContentSecurityPolicy());

    m1.put(null, null);
    assertEquals(BEANS.get(ConfigurableContentSecurityPolicy.class), getInterceptor("whatever", m1).getContentSecurityPolicy());

    m1.put("file", null);
    assertEquals(BEANS.get(ConfigurableContentSecurityPolicy.class), getInterceptor("whatever", m1).getContentSecurityPolicy());
  }

  @Test
  public void testDefaultPolicy() {
    ContentSecurityPolicy defaultPolicy = BEANS.get(BlockAllContentSecurityPolicy.class);
    ContentSecurityPolicy filePolicy = BEANS.get(ConfigurableContentSecurityPolicy.class);
    String fileName = "doc.html";
    Map<String, ContentSecurityPolicy> policyMap = new HashMap<>();
    policyMap.put(null, defaultPolicy);
    policyMap.put(fileName, filePolicy);
    assertSame(defaultPolicy, getInterceptor("whatever", policyMap).getContentSecurityPolicy()); // default applies
    assertSame(filePolicy, getInterceptor(fileName, policyMap).getContentSecurityPolicy()); // specific applies
  }

  @Test
  public void testRemove() {
    ContentSecurityPolicy defaultPolicy = BEANS.get(BlockAllContentSecurityPolicy.class);
    String fileName = "doc.html";
    Map<String, ContentSecurityPolicy> policyMap = new HashMap<>();
    policyMap.put(null, defaultPolicy);
    policyMap.put(fileName, null);
    assertSame(defaultPolicy, getInterceptor(fileName, policyMap).getContentSecurityPolicy()); // default applies
  }

  protected BrowserFieldContentHttpResponseInterceptor getInterceptor(String requestFileName, Map<String, ContentSecurityPolicy> policyMap) {
    IBrowserField mock = mock(IBrowserField.class);
    IBrowserFieldUIFacade facade = mock(IBrowserFieldUIFacade.class);
    when(mock.getUIFacade()).thenReturn(facade);
    when(facade.getContentSecurityPolicy(nullable(String.class))).thenAnswer(i -> policyMap.get(i.getArgument(0, String.class)));
    return new BrowserFieldContentHttpResponseInterceptor(mock, BinaryResources.create().withFilename(requestFileName).build());
  }
}
