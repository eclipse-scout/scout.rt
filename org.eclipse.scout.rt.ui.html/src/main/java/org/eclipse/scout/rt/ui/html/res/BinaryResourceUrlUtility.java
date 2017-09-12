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
package org.eclipse.scout.rt.ui.html.res;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.services.common.icon.IconLocator;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.res.loader.DynamicResourceInfo;
import org.eclipse.scout.rt.ui.html.res.loader.DynamicResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BinaryResourceUrlUtility {
  private static final Logger LOG = LoggerFactory.getLogger(BinaryResourceUrlUtility.class);

  private BinaryResourceUrlUtility() {
  }

  /**
   * Regular expression pattern to find icons, e.g. to find &lt;img src="iconid:some_icon"&gt;.
   * <p>
   * Pattern does a search for iconid:some_icon (in quotation marks) and has three groups:
   * <li>1. Type of quotation mark, either " or '.
   * <li>2. Icon name, in the example <b>some_icon</b>
   */
  public static final Pattern ICON_REGEX_PATTERN = Pattern.compile("([\"'])iconid:([^\"']+)\\1", Pattern.CASE_INSENSITIVE);

  /**
   * Regular expression pattern to find icons, e.g. to find &lt;img src="binaryResource:some_res"&gt;.
   * <p>
   * Pattern does a search for binaryResource:some_res (in quotation marks) and has three groups:
   * <li>1. Type of quotation mark, either " or '.
   * <li>2. Icon name, in the example <b>some_res</b>
   */
  public static final Pattern BINARY_RESOURCE_REGEX_PATTERN = Pattern.compile("([\"'])binaryResource:([^\"']+)\\1", Pattern.CASE_INSENSITIVE);

  private static final Pattern REGEX_FINGERPRINT_PATTERN = Pattern.compile("^([0-9]*)/?(.*)$");

  /**
   * @return a relative URL for a configured logical icon-name or a font-based icon. For instance:
   *         <ul>
   *         <li>input: <code>"bookmark"</code>, output: <code>"icon/bookmark.png"</code> (the file extension is
   *         included to support auto-detection of the MIME type without looking at the file contents)</li>
   *         <li>input: <code>"font:X"</code>, output: <code>"font:X"</code></li>
   *         </ul>
   *         The file extension is included to be able to auto-detect the MIME type based on it.
   *         <p>
   *         Use this method for image-files located in the /resource/icons directories of all jars on the classpath.
   */
  public static String createIconUrl(String iconId) {
    if (!StringUtility.hasText(iconId) || AbstractIcons.Null.equals(iconId)) {
      return null;
    }
    if (iconId.startsWith("font:")) {
      return iconId;
    }
    IconSpec iconSpec = IconLocator.instance().getIconSpec(iconId);
    if (iconSpec != null) {
      return "icon/" + iconSpec.getName(); // includes file extension
    }
    LOG.warn("iconId '{}' could not be resolved", iconId);
    return null; // may happen, when no icon is available for the requested iconName
  }

  /**
   * Helper method for {@link #ICON_REGEX_PATTERN} to replace all occurrences with the proper url.
   */
  public static String replaceIconIdHandlerWithUrl(String str) {
    if (str == null) {
      return null;
    }
    Matcher m = ICON_REGEX_PATTERN.matcher(String.valueOf(str));
    @SuppressWarnings("squid:S1149")
    StringBuffer ret = new StringBuffer();
    while (m.find()) {
      m.appendReplacement(ret, m.group(1) + createIconUrl(m.group(2)) + m.group(1));
    }
    m.appendTail(ret);
    return ret.toString();
  }

  public static String createDynamicAdapterResourceUrl(IJsonAdapter<?> jsonAdapter, BinaryResource binaryResource) {
    if (!checkCreateDynamicAdapterResourceUrlArguments(jsonAdapter, binaryResource)) {
      return null;
    }
    return new DynamicResourceInfo(jsonAdapter, getFilenameWithFingerprint(binaryResource)).toPath();
  }

  /**
   * @return a relative URL for a resource handled by an adapter, see {@link DynamicResourceLoader}.
   *         <p>
   *         The calling adapter must implement {@link IBinaryResourceProvider}.
   */
  public static String createDynamicAdapterResourceUrl(IJsonAdapter<?> jsonAdapter, String filename) {
    if (!checkCreateDynamicAdapterResourceUrlArguments(jsonAdapter, filename)) {
      return null;
    }
    return new DynamicResourceInfo(jsonAdapter, filename).toPath();
  }

  public static String getFilenameWithFingerprint(BinaryResource binaryResource) {
    if (!binaryResource.hasFilename()) {
      return null;
    }
    if (binaryResource.getFingerprint() <= 0) {
      return binaryResource.getFilename();
    }
    return binaryResource.getFingerprint() + "/" + binaryResource.getFilename();
  }

  public static Pair<String, Long> extractFilenameWithFingerprint(String filenameWithFingerprint) {
    Matcher m = REGEX_FINGERPRINT_PATTERN.matcher(filenameWithFingerprint);
    m.find();

    String fingerprintString = m.group(1);
    String filename = m.group(2);

    Long fingerprint = 0L;
    if (StringUtility.hasText(fingerprintString)) {
      fingerprint = Long.valueOf(fingerprintString);
    }

    return new ImmutablePair<>(filename, fingerprint);
  }

  private static boolean checkCreateDynamicAdapterResourceUrlArguments(IJsonAdapter<?> jsonAdapter, Object arg) {
    if (jsonAdapter == null) {
      return false;
    }
    if (!(jsonAdapter instanceof IBinaryResourceProvider)) {
      LOG.warn("adapter {} is not implementing {}", jsonAdapter, IBinaryResourceProvider.class.getName());
      return false;
    }
    if (arg == null) {
      return false;
    }
    return true;
  }

  /**
   * Helper method for {@link #BINARY_RESOURCE_REGEX_PATTERN} to replace all occurrences with the proper url.
   */
  public static String replaceBinaryResourceHandlerWithUrl(IJsonAdapter<?> jsonAdapter, String str) {
    if (str == null) {
      return null;
    }
    Matcher m = BINARY_RESOURCE_REGEX_PATTERN.matcher(str);
    @SuppressWarnings("squid:S1149")
    StringBuffer ret = new StringBuffer();
    while (m.find()) {
      m.appendReplacement(ret, m.group(1) + createDynamicAdapterResourceUrl(jsonAdapter, m.group(2)) + m.group(1));
    }
    m.appendTail(ret);
    return ret.toString();
  }
}
