/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.basic.filechooser;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.FileUtility;

/**
 * Microsoft internet explorer is not correctly handlig mime types in the 'accept' atttribute.
 * <p>
 * For example valid text/csv is not recognized.
 * <p>
 * Therefore as a fallback for IE only we use file extensions for selected white-listed types.
 * <p>
 * This bean builds the content of the accept attribute in
 * <code>&lt;input accept="file_extension|audio/*|video/*|image/*|media_type"&gt;</code>
 *
 * @since 5.2
 */
@Bean
public class JsonFileChooserAcceptAttributeBuilder {
  private final HashMap<String, String> m_mimeTypeToAcceptType = new HashMap<>();

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
      if (mimeTypeOrExtension.indexOf('/') > 0) {
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
    return new HashSet<String>(m_mimeTypeToAcceptType.values());
  }

  /**
   * @param mimeType
   *          never null
   * @param ext
   *          without leading '*' or '.'. May be null.
   * @return the text used in the accept tag in the input element, never null
   *         <p>
   *         typically this is the mime type or the file extension with a leading '.'
   */
  protected String convertToAcceptType(String mimeType, String ext) {
    switch (mimeType) {
      case "text/csv":
      case "text/comma-separated-values":
        return ".csv";
    }
    if (ext != null) {
      switch (ext) {
        case "csv":
          return ".csv";
      }
    }
    return mimeType;
  }
}
