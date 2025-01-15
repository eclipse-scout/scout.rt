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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.FileUtility;

/**
 * Implementations of this interface can handle binref {@link URI} for a specific path.
 * <p>
 * No method should not be called directly. Only {@link BinaryRefSupport} should call methods of this interface.
 */
@ApplicationScoped
public interface IBinaryRefHandler {

  /**
   * URI [base-] path under which this handler is registered. {@link URI} starting with this path will be handled by
   * this handler except there is a handler registered with a more specific path.<br/>
   * Note that this path should be an absolute path and therefore should begin with a slash character ('/')
   *
   * @return non null registration path
   */
  String getRegistrationPath();

  /**
   * Gets resource to given URI as {@link BinaryResource}. Returns null if resource not found.
   *
   * @param uri
   *     non null URI with a path matching {@link #getRegistrationPath()}
   * @return BinaryResource of URI
   */
  BinaryResource loadBinaryResource(URI uri);

  /**
   * Lookups display texts for all provided URIs. Display texts are only returned for resources which were found. Input
   * URI collection must contain only URIs for which this handler is registered.
   *
   * @param resultCollector
   *     collector map in which implementor puts resolved display texts
   * @param uris
   *     non null collection of {@link URI} for this handler to resolve display texts
   */
  default void getDisplayTexts(Map<URI, String> resultCollector, Collection<URI> uris) {
    // use as display texts the filenames
    getFilenames(resultCollector, uris);
  }

  /**
   * Looks up filenames for provided URIs. Filenames are only returned for resources which were found. Input URI
   * collection must contain only URIs for which this handler is registered.
   *
   * @param resultCollector
   *     collector map in which implementor puts resolved filenames
   * @param uris
   *     non null collection of {@link URI} for this handler to resolve filenames
   */
  void getFilenames(Map<URI, String> resultCollector, Collection<URI> uris);

  /**
   * Looks up content types for provided URIs. Content types are only returned for resources which were found. Input URI
   * collection must contain only URIs for which this handler is registered.
   *
   * @param resultCollector
   *     collector map in which implementor puts resolved filenames
   * @param uris
   *     non null collection of {@link URI} for this handler to resolve content types
   */
  default void getContentTypes(Map<URI, String> resultCollector, Collection<URI> uris) {
    // get content types from filenames
    Map<URI, String> filenames = new HashMap<>();
    getFilenames(filenames, uris);
    for (Entry<URI, String> entry : filenames.entrySet()) {
      resultCollector.put(entry.getKey(), FileUtility.getMimeType(entry.getValue()));
    }
  }
}
