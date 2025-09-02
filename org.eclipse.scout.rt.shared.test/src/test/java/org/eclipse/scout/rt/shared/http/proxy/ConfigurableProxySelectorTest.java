/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http.proxy;

import static org.eclipse.scout.rt.platform.util.Assertions.assertInstance;
import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ConfigurableProxySelectorTest {

  @Test
  public void testAddProxyToProxyMap() {
    assertThrows(IndexOutOfBoundsException.class, () -> testAddProxyToProxyMapInternal("foo", null, null)); // at least one equal sign is required

    testAddProxyToProxyMapInternal("a.*=127.0.0.1:1234",
        p -> {
          assertTrue(p.matcher("a").matches());
          assertTrue(p.matcher("ab").matches());
          assertFalse(p.matcher("ba").matches());
        },
        p -> {
          assertEquals(Type.HTTP, p.type());
          InetSocketAddress inetSocketAddress = assertInstance(p.address(), InetSocketAddress.class);
          assertEquals("127.0.0.1", inetSocketAddress.getHostString());
          assertEquals(1234, inetSocketAddress.getPort());
        });
    testAddProxyToProxyMapInternal("b.*=proxy.example.com:80",
        p -> assertEquals("b.*", p.toString()),
        p -> {
          assertEquals(Type.HTTP, p.type());
          InetSocketAddress inetSocketAddress = assertInstance(p.address(), InetSocketAddress.class);
          assertEquals("proxy.example.com", inetSocketAddress.getHostString());
          assertEquals(80, inetSocketAddress.getPort());
        });
    testAddProxyToProxyMapInternal("c.*=SOCKS=::1:1080",
        p -> assertEquals("c.*", p.toString()),
        p -> {
          assertEquals(Type.SOCKS, p.type());
          InetSocketAddress inetSocketAddress = assertInstance(p.address(), InetSocketAddress.class);
          assertEquals("0:0:0:0:0:0:0:1", inetSocketAddress.getHostString()); // ipv6 address expanded
          assertEquals(1080, inetSocketAddress.getPort());
        });
    testAddProxyToProxyMapInternal("d.*=DIRECT=",
        p -> assertEquals("d.*", p.toString()),
        p -> {
          assertEquals(Type.DIRECT, p.type());
          assertNull(p.address());
        });
  }

  protected void testAddProxyToProxyMapInternal(String proxyConfiguration, Consumer<Pattern> patternVerifier, Consumer<Proxy> proxyVerifier) {
    Map<Pattern, Proxy> proxyMap = new HashMap<>();
    ConfigurableProxySelector selector = new ConfigurableProxySelector((List<String>) null, null);

    selector.addProxyToProxyMap(proxyMap, proxyConfiguration);
    assertEquals(1, proxyMap.size());
    Entry<Pattern, Proxy> mapEntry = proxyMap.entrySet().iterator().next();

    patternVerifier.accept(mapEntry.getKey());
    proxyVerifier.accept(mapEntry.getValue());
  }

  @Test
  public void testSelect() throws URISyntaxException {
    ConfigurableProxySelector selector = new ConfigurableProxySelector(List.of("\\w+://a.*=127.0.0.1:1234", "\\w+://b.*=SOCKS=::1:1080"), List.of("\\w+://ab.*"));

    List<Proxy> proxyList = selector.select(new URI("imap://b.example.com"));
    assertEquals(1, proxyList.size());
    Proxy proxy = proxyList.getFirst();
    assertEquals(Type.SOCKS, proxy.type());

    proxyList = selector.select(new URI("imap://c.example.com"));
    assertEquals(1, proxyList.size());
    proxy = proxyList.getFirst();
    assertEquals(Type.DIRECT, proxy.type());

    proxyList = selector.select(new URI("http://ab.example.com"));
    assertEquals(1, proxyList.size());
    proxy = proxyList.getFirst();
    assertEquals(Type.DIRECT, proxy.type());

    proxyList = selector.select(new URI("http://ac.example.com"));
    assertEquals(1, proxyList.size());
    proxy = proxyList.getFirst();
    assertEquals(Type.HTTP, proxy.type());
  }
}
