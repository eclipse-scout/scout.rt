/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.resource;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * This bean should be called only from {@link BinaryRefs}. It provides the implementation of the static methods of
 * {@link BinaryRefs}.
 */
@ApplicationScoped
public class BinaryRefSupport implements IPlatformListener {

  private Map<String, IBinaryRefHandler> m_handlers = Collections.emptyMap();

  @Override
  public void stateChanged(PlatformEvent event) {
    if (event.getState() == IPlatform.State.BeanManagerValid) {
      ensureLoaded();
    }
  }

  public void ensureLoaded() {
    if (m_handlers.isEmpty()) {
      reload();
    }
  }

  public synchronized void reload() {
    m_handlers = new HashMap<>(collectHandlers()); // create new private copy
  }

  protected Map<String, IBinaryRefHandler> collectHandlers() {
    Map<String, IBinaryRefHandler> handlers = new HashMap<>();
    for (IBinaryRefHandler handler : BEANS.all(IBinaryRefHandler.class)) {
      String path = Assertions.assertNotNull(handler.getRegistrationPath(), "Registration path may not be null. [handler={}]", handler);
      handlers.put(path, handler);
    }
    Assertions.assertNotNull(handlers.get(""), "No root handler is registered. A root handler is required.");
    List<String> invalidRegistrations = handlers.keySet().stream().filter(p -> p.endsWith("/")).collect(Collectors.toList());
    Assertions.assertTrue(invalidRegistrations.isEmpty(), "There are handlers registered for a path ending with a slash character ('/') - Path must not end with slash character");
    invalidRegistrations = handlers.keySet().stream().filter(p -> !p.startsWith("/")).collect(Collectors.toList());
    invalidRegistrations.remove(""); // root handler may start without "/"
    Assertions.assertTrue(invalidRegistrations.isEmpty(), "There are handlers registered for a path not beginning with a slash character ('/') - Path must start with a slash character (except for root handler)");
    return handlers;
  }

  protected IBinaryRefHandler getRootHandler() {
    return m_handlers.get("");
  }

  /**
   * Looks up handler for a binref URI. URI must be a binref URI. At least the root handler is returned.
   *
   * @param uri
   *     non null binref {@link URI} (see {@link BinaryRefs#isBinaryRef})
   * @return non null handler for URI
   */
  protected IBinaryRefHandler lookupHandlerUnsafe(URI uri) {
    String path = uri.getPath();
    if (StringUtility.isNullOrEmpty(path) || !path.startsWith("/")) {
      // empty path -> return root handler
      // relative path ->  we do not know context - return root handler
      return getRootHandler();
    }
    IBinaryRefHandler handler = m_handlers.get(path);
    String p = path;
    while (handler == null) { // root handler (path empty string) must be always registered
      p = p.substring(0, p.lastIndexOf('/')); // for path '/' this will result in the empty string -> root handler
      handler = m_handlers.get(p);
    }
    return handler;
  }

  /**
   * @return null handler for URI or null if URI is not a binref URI
   */
  public IBinaryRefHandler lookupHandler(URI uri) {
    if (!BinaryRefs.isBinaryRef(uri)) {
      return null;
    }
    return lookupHandlerUnsafe(uri);
  }

  public BinaryResource loadBinaryResourceOrNull(URI uri) {
    if (!BinaryRefs.isBinaryRef(uri)) {
      return null;
    }
    // handler cannot be null - but result from handler#getBinaryResource call may be null (handler cannot find resource)
    return lookupHandlerUnsafe(uri).loadBinaryResource(uri);
  }

  public BinaryResource loadBinaryResource(URI uri) {
    if (!BinaryRefs.isBinaryRef(uri)) {
      return null;
    }
    // handler cannot be null - but result from handler#getBinaryResource call may be null (handler cannot find resource)
    BinaryResource result = lookupHandlerUnsafe(uri).loadBinaryResource(uri);
    if (result == null) {
      throw createResourceNotFoundException(uri);
    }
    return result;
  }

  protected RuntimeException createResourceNotFoundException(URI uri) {
    return new IllegalStateException("Invalid binary reference: " + uri);
  }

  public Map<URI, String> getDisplayTexts(Collection<URI> uris, Function<URI, String> fallbackFunction) {
    Map<URI, String> resultCollector = lookup(uris, h -> h::getDisplayTexts);

    // provide a default display text for any URI which could not be handled
    if (uris != null) {
      for (URI uri : uris) {
        if (BinaryRefs.isBinaryRef(uri)) {
          resultCollector.computeIfAbsent(uri, fallbackFunction);
        }
      }
    }

    return resultCollector;
  }

  public Map<URI, String> getFilenames(Collection<URI> uris) {
    return lookup(uris, h -> h::getFilenames);
  }

  public Map<URI, String> getContentTypes(Collection<URI> uris) {
    return lookup(uris, h -> h::getContentTypes);
  }

  protected <T> Map<URI, T> lookup(Collection<URI> uris, Function<IBinaryRefHandler, BiConsumer<Map<URI, T>, Collection<URI>>> handlerLookup) {
    if (uris == null) {
      return new HashMap<>();
    }
    Map<URI, T> resultCollector = new HashMap<>();
    Map<IBinaryRefHandler, List<URI>> handlerCalls = uris.stream()
        .filter(BinaryRefs::isBinaryRef)
        .collect(Collectors.groupingBy(this::lookupHandlerUnsafe));
    for (Entry<IBinaryRefHandler, List<URI>> entry : handlerCalls.entrySet()) {
      IBinaryRefHandler handler = entry.getKey(); // handler cannot be null - lookupHandler never returns null
      handlerLookup.apply(handler).accept(resultCollector, uris);
    }
    return resultCollector;
  }
}
