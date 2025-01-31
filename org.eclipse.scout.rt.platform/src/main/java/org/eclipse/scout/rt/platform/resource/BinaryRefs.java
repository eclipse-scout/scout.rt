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
import java.util.Map;
import java.util.function.Function;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;

/*
 * Implementation note: as {@link BinaryResource} does currently not support a 'lazy' loading of content, displayText,
 * filename and contentType retrieval are supported through separate methods.
 */
public final class BinaryRefs {
  public static final String URI_SCHEME = "binref";

  private BinaryRefs() {
  }

  public static boolean isBinaryRef(URI uri) {
    return uri != null && URI_SCHEME.equals(uri.getScheme());
  }

  /**
   * Looks up display texts for all provided URIs. For all URIs a display text is guaranteed to be returned. Even if
   * such a resources does not exist.
   * <p>
   * Any non binref URI is not contained in the result map.
   * <p>
   * Usually, the display text of a binary ref URI is simply the filename. E.g. 'notes.txt', 'portrait.png' or 'Unknown
   * URI [binref:/documents/1337]'. Use this for user interactions.
   *
   * @param uris
   *     may be null or empty or contain null elements
   * @return non null modifiable map containing display text for all URIs
   */
  public static Map<URI, String> getDisplayTexts(Collection<URI> uris) {
    return BEANS.get(BinaryRefSupport.class).getDisplayTexts(uris, uri -> "Unknown URI [" + uri + "]");
  }

  /**
   * Looks up display texts for all provided URIs. If for a URI no display text should be determined, the
   * fallbackFunction is called to provide a display text.
   * <p>
   * Any non binref URI is not contained in the result map.
   * <p>
   * Usually, the display text of a binary ref URI is simply the filename. Use this for user interactions.
   *
   * @param uris
   *     may be null or empty or contain null elements, any non binref URI is ignored
   * @param fallbackFunction
   *     custom function called in case a URI display text could not determined
   * @return non null modifiable map containing display text for all URIs
   */
  public static Map<URI, String> getDisplayTexts(Collection<URI> uris, Function<URI, String> fallbackFunction) {
    return BEANS.get(BinaryRefSupport.class).getDisplayTexts(uris, fallbackFunction);
  }

  /**
   * Looks up display text for a URI. A default text is returned if URI is not a 'binref' URI or resource
   * (BinaryResource) was not found.<br/>
   * Usually, the display text of a binary ref URI is simply the filename. Examples: 'notes.txt', 'portrait.png' or
   * 'Unknown URI [binref:/documents/1337]'. Use this for user interactions.
   *
   * @param uri
   *     may be null
   * @return returns null if no URI is provided else a non-null, non-empty string
   */
  public static String getDisplayText(URI uri) {
    return getDisplayTexts(Collections.singleton(uri)).get(uri);
  }

  /**
   * Looks up filenames for provided URIs. Filenames are returned only for resources which were found.<br/>
   * In contrast to {@link #getDisplayTexts} returns only valid filenames from the referenced BinaryResource.
   *
   * @param uris
   *     may be null or empty or contain null elements
   * @return non null modifiable map containing filenames
   */
  public static Map<URI, String> getFilenames(Collection<URI> uris) {
    return BEANS.get(BinaryRefSupport.class).getFilenames(uris);
  }

  /**
   * Looks up filename for a URI. A filename is returned only if resource was found.<br/>
   * In contrast to {@link #getDisplayText} returns only valid filenames from the referenced BinaryResource.
   */
  public static String getFilename(URI uri) {
    return getFilenames(Collections.singleton(uri)).get(uri);
  }

  /**
   * Looks up content types for provided URIs. Content type are returned only for resources which were found.
   *
   * @param uris
   *     may be null or empty or contain null elements
   * @return non null modifiable map containing content types
   */
  public static Map<URI, String> getContentTypes(Collection<URI> uris) {
    return BEANS.get(BinaryRefSupport.class).getContentTypes(uris);
  }

  /**
   * Looks up content type for a URI. Content type is returned only if resource was found.
   */
  public static String getContentType(URI uri) {
    return getContentTypes(Collections.singleton(uri)).get(uri);
  }

  /**
   * Gets resource to given URI as {@link BinaryResource}.
   *
   * @return BinaryResource of URI or null if URI is null or not a binref URI
   * @throws VetoException
   *     if for given binref URI no handler or content can be found
   */
  public static BinaryResource loadBinaryResource(URI uri) {
    return BEANS.get(BinaryRefSupport.class).loadBinaryResource(uri);
  }

  /**
   * Gets resource to given URI as {@link BinaryResource}. Returns null if URI is null, not a binref URI or resource was
   * not found.
   *
   * @return BinaryResource of URI
   */
  public static BinaryResource loadBinaryResourceOrNull(URI uri) {
    return BEANS.get(BinaryRefSupport.class).loadBinaryResourceOrNull(uri);
  }
}
