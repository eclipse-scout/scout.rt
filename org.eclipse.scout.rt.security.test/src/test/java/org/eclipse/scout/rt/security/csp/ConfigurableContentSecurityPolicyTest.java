/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security.csp;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

import java.io.Serial;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.security.csp.ConfigurableContentSecurityPolicy.CspConfig;
import org.eclipse.scout.rt.security.csp.ContentSecurityPolicyConfigProperties.CspDirectiveProperty;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.Test;

public class ConfigurableContentSecurityPolicyTest {

  public static final String LOGIN_MEDIA = ContentSecurityPolicy.DIRECTIVE_MEDIA_SRC + ConfigurableContentSecurityPolicy.SEPARATOR_ENTRY_POINT + "login.html";

  /**
   * Test that a plain instance uses the programmed defaults
   */
  @Test
  public void testDefaults() {
    assertTrue(BEANS.get(ConfigurableContentSecurityPolicy.class).toToken().contains(ContentSecurityPolicy.EXPRESSION_SELF));
  }

  /**
   * Tests that Default, config and additional config are correctly merged
   */
  @Test
  public void testConfigLevels() {
    ConfigurableContentSecurityPolicy csp = new ConfigurableContentSecurityPolicy() {
      @Serial
      private static final long serialVersionUID = -4939203210680409560L;

      @Override
      protected Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put(ContentSecurityPolicy.DIRECTIVE_IMG_SRC, "https://img.example.com"); // replace defaults
        config.put(LOGIN_MEDIA, "https://media.example.com"); // replace defaults for specific entrypoint
        config.put(ContentSecurityPolicy.DIRECTIVE_CHILD_SRC, "https://child.example.com");
        return config;
      }

      @Override
      protected Map<String, String> getConfigAppend() {
        Map<String, String> config = new HashMap<>();
        config.put(ContentSecurityPolicy.DIRECTIVE_IMG_SRC, "https://img2.example.com https://img3.example.com"); // append to configured
        config.put(LOGIN_MEDIA, "https://media2.example.com 'unsave-inline'"); // append to configured for specific entrypoint
        config.put(ContentSecurityPolicy.DIRECTIVE_FONT_SRC, "https://img2.example.com https://img3.example.com"); // append to default value
        config.put("not-existing", "blob:"); // completely new directive
        return config;
      }
    };

    // for all entrypoints
    csp.initFromConfig();
    assertEquals(ContentSecurityPolicy.EXPRESSION_SELF, csp.get(ContentSecurityPolicy.DIRECTIVE_WORKER_SRC)); // nothing configured. Should match the default.
    assertEquals("https://img.example.com https://img2.example.com https://img3.example.com", csp.get(ContentSecurityPolicy.DIRECTIVE_IMG_SRC)); // configured and additional, replacing the default.
    assertEquals(ContentSecurityPolicy.EXPRESSION_SELF, csp.get(ContentSecurityPolicy.DIRECTIVE_MEDIA_SRC)); // nothing configured. Should match the default.
    assertEquals("https://child.example.com", csp.get(ContentSecurityPolicy.DIRECTIVE_CHILD_SRC)); // replaced the default without modification
    assertEquals("'self' https://img2.example.com https://img3.example.com", csp.get(ContentSecurityPolicy.DIRECTIVE_FONT_SRC)); // append to the Scout default.
    assertEquals("blob:", csp.get("not-existing")); // append to the Scout default.

    // for login.html
    csp.initForPath("login.html");
    assertEquals(ContentSecurityPolicy.EXPRESSION_SELF, csp.get(ContentSecurityPolicy.DIRECTIVE_WORKER_SRC)); // nothing configured. Should match the default.
    assertEquals("https://img.example.com https://img2.example.com https://img3.example.com", csp.get(ContentSecurityPolicy.DIRECTIVE_IMG_SRC)); // configured and additional, replacing the default.
    assertEquals("https://media.example.com https://media2.example.com 'unsave-inline'", csp.get(ContentSecurityPolicy.DIRECTIVE_MEDIA_SRC)); // configured for this entrypoint.
    assertEquals("https://child.example.com", csp.get(ContentSecurityPolicy.DIRECTIVE_CHILD_SRC)); // replaced the default without modification
    assertEquals("'self' https://img2.example.com https://img3.example.com", csp.get(ContentSecurityPolicy.DIRECTIVE_FONT_SRC)); // append to the Scout default.
    assertEquals("blob:", csp.get("not-existing")); // append to the Scout default.
  }

  @Test
  public void testCspConfigParse() {
    assertEquals(new CspConfig("script-src", "'self'", null), CspConfig.parse(new SimpleEntry<>("script-src", "'self'")));
    assertEquals(new CspConfig("script-src", "'self'", "MyFile.html"), CspConfig.parse(new SimpleEntry<>("script-src#MyFile.html", "'self'")));
    assertEquals(new CspConfig("script-src", "'self'", ""), CspConfig.parse(new SimpleEntry<>("script-src#", "'self'")));
    assertEquals(new CspConfig("", "'self'", "MyFile.html"), CspConfig.parse(new SimpleEntry<>("#MyFile.html", "'self'")));
  }

  @Test
  public void testCspConfigAcceptPathInfo() {
    CspConfig noEntryPoint = new CspConfig("script-src", "'self'", null);
    assertTrue(noEntryPoint.acceptPathInfo("MyFile.html"));
    assertTrue(noEntryPoint.acceptPathInfo(null));

    CspConfig withEntryPoint = new CspConfig("script-src", "'self'", "MyFile.html");
    assertTrue(withEntryPoint.acceptPathInfo("MyFile.html"));
    assertTrue(withEntryPoint.acceptPathInfo("/MyFile.html"));
    assertTrue(withEntryPoint.acceptPathInfo("/folder/subfolder/MyFile.html"));
    assertFalse(withEntryPoint.acceptPathInfo("MyFile2.html"));
    assertFalse(withEntryPoint.acceptPathInfo(null));
  }

  @Test
  public void testMerge() {
    CspDirectiveProperty mock = mock(CspDirectiveProperty.class);
    Map<String, String> fixture = new LinkedHashMap<>();
    fixture.put("script-src", ContentSecurityPolicy.EXPRESSION_SELF);
    fixture.put("script-src#MyFile.html", "https://eclipse.org/");
    fixture.put("connect-src", "https://eclipse.org/");

    when(mock.getValue(nullable(String.class))).thenAnswer(i -> fixture);
    IBean<CspDirectiveProperty> configBean = BeanTestingHelper.get().registerBean(new BeanMetaData(CspDirectiveProperty.class).withApplicationScoped(true).withInitialInstance(mock));
    try {
      ConfigurableContentSecurityPolicy csp = BEANS.get(ConfigurableContentSecurityPolicy.class);

      // default uses global config which ignores the config for MyFile.html
      assertEquals(ContentSecurityPolicy.EXPRESSION_SELF, csp.get(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC));

      // MyFile2 is not found. Only global config remains
      csp.initForPath("MyFile2.html");
      assertEquals(ContentSecurityPolicy.EXPRESSION_SELF, csp.get(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC));

      // The global expressions and the specific ones for MyFile.html
      csp.initForPath("MyFile.html");
      assertEquals(ContentSecurityPolicy.EXPRESSION_SELF + ContentSecurityPolicy.SEPARATOR_EXPRESSION + "https://eclipse.org/", csp.get(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC));
    }
    finally {
      BeanTestingHelper.get().unregisterBean(configBean);
    }
  }
}
