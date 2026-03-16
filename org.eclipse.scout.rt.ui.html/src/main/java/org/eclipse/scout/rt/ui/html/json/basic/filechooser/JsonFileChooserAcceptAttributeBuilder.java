/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.basic.filechooser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.FileUtility;

/**
 * This bean builds the content of the accept attribute in
 * <code>&lt;input accept="file_extension|audio/*|video/*|image/*|media_type"&gt;</code>
 * In this class we correct for browser-side bugs such as:
 * <ul>
 *   <li>
 *     Microsoft Internet Explorer is not correctly handling mime types in the 'accept' attribute. <br/>
 *     For example valid text/csv is not recognized. <br/>
 *     Therefore as a fallback for IE only we use file extensions for selected white-listed types.
 *   </li>
 *   <li>
 *     Mozilla Firefox has a bug (Bugzilla 201480) with the MIME type text/javascript. <br/>
 *     As a fallback we also use the file extensions defined in RFC 9239 for this MIME type.
 *   </li>
 * </ul>
 *
 * @since 5.2
 */
@Bean
public class JsonFileChooserAcceptAttributeBuilder {
  private final Map<String, Set<String>> m_mimeTypeToAcceptType = new HashMap<>();

  /**
   * append the collection of media types to the list
   * <p>
   * use {@link #build()} to finish building and get the result set
   *
   * @return this for method chaining used in fluent api
   */
  public JsonFileChooserAcceptAttributeBuilder withTypes(Collection<String> mimeTypeOrExtensions) {
    if (mimeTypeOrExtensions != null) {
      for (String t : mimeTypeOrExtensions) {
        withType(t);
      }
    }
    return this;
  }

  /**
   * append the media type to the list
   * <p>
   * use {@link #build()} to finish building and get the result set
   *
   * @return this for method chaining used in fluent api
   */
  public JsonFileChooserAcceptAttributeBuilder withType(String mimeTypeOrExtension) {
    if (mimeTypeOrExtension != null) {
      if (mimeTypeOrExtension.indexOf('/') > -1) {
        //mime type
        m_mimeTypeToAcceptType.put(mimeTypeOrExtension, convertToAcceptType(mimeTypeOrExtension, null));
      }
      else {
        //file extension
        String ext = mimeTypeOrExtension;
        if (ext.startsWith("*")) {
          ext = ext.substring(1);
        }
        if (ext.startsWith(".")) {
          ext = ext.substring(1);
        }
        String mimeType = FileUtility.getContentTypeForExtension(ext);
        if (mimeType == null) {
          mimeType = ext;
        }
        m_mimeTypeToAcceptType.put(mimeType, convertToAcceptType(mimeType, ext));
      }
    }
    return this;
  }

  /**
   * @return the completed set of accept types
   */
  public Set<String> build() {
    return m_mimeTypeToAcceptType.values().stream()
        .flatMap(s -> s.stream())
        .collect(Collectors.toSet());
  }

  /**
   * @param mimeType
   *     never null
   * @param ext
   *     without leading '*' or '.'. May be null.
   * @return the text used in the accept tag in the input element, never null
   * <p>
   * typically this is the mime type or the file extension with a leading '.'
   */
  protected Set<String> convertToAcceptType(String mimeType, String ext) {
    switch (mimeType) {
      case "text/csv", "text/comma-separated-values" -> {
        return CollectionUtility.hashSet(".csv");
      }
      case "text/javascript" -> {
        return CollectionUtility.hashSet(".js", ".mjs");
      }
    }
    if (ext != null) {
      switch (ext) {
        case "csv" -> {
          return CollectionUtility.hashSet(".csv");
        }
        case "js" -> {
          return CollectionUtility.hashSet(".js");
        }
        case "mjs" -> {
          return CollectionUtility.hashSet(".mjs");
        }
      }
    }
    return CollectionUtility.hashSet(mimeType);
  }
}
