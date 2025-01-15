/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.res;

import static org.eclipse.scout.rt.platform.util.StringUtility.join;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Adler32;

import org.eclipse.scout.rt.client.services.common.icon.IconLocator;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.client.ui.IIconIdPrefix;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResourceUtility;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.res.loader.BinaryRefResourceInfo;
import org.eclipse.scout.rt.ui.html.res.loader.BinaryRefResourceLoader;
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
  public static final Pattern ICON_REGEX_PATTERN = Pattern.compile("([\"'])iconId:([^\"']+)\\1", Pattern.CASE_INSENSITIVE);

  /**
   * Regular expression pattern to find icons, e.g. to find &lt;img src="binaryresource:some_res"&gt;.
   * <p>
   * Pattern does a search for binaryResource:some_res (in quotation marks) and has three groups:
   * <li>1. Type of quotation mark, either " or '.
   * <li>2. Icon name, in the example <b>some_res</b>
   */
  public static final Pattern BINARY_RESOURCE_REGEX_PATTERN = Pattern.compile("([\"'])binaryResource:([^\"']+)\\1", Pattern.CASE_INSENSITIVE);

  /**
   * Regular expression pattern to find binaryRefs, e.g. to find &lt;img src="binref:some_ref"&gt;.
   * <p>
   * Pattern does a search for binref:some_ref (in quotation marks) and has three groups:
   * <li>1. Type of quotation mark, either " or '.
   * <li>2. BinaryRef name, in the example <b>some_ref</b>
   * <li>3. Type of quotation mark, same as 1.
   */
  public static final Pattern BINARY_REF_REGEX_PATTERN = Pattern.compile("([\"'])binref:([^\"']+)\\1", Pattern.CASE_INSENSITIVE);

  /**
   * @return a relative URL for a configured logical icon-name or a font-based icon. For instance:
   * <ul>
   * <li>input: <code>"bookmark"</code>, output: <code>"icon/bookmark.png"</code> (the file extension is
   * included to support auto-detection of the MIME type without looking at the file contents)</li>
   * <li>input: <code>"font:X"</code>, output: <code>"font:X"</code></li>
   * <li>input: <code>"url:/api/create-image/foo"</code>, output: <code>"/api/create-image/foo"</code></li>
   * </ul>
   * The file extension is included to be able to auto-detect the MIME type based on it.
   * <p>
   * Use this method for image-files located in the /resource/icons directories of all jars on the classpath.
   */
  public static String createIconUrl(String iconId) {
    if (!StringUtility.hasText(iconId) || AbstractIcons.Null.equals(iconId)) {
      return null;
    }
    if (iconId.startsWith(IIconIdPrefix.FONT)) {
      return iconId;
    }
    if (iconId.startsWith(IIconIdPrefix.URL)) {
      return StringUtility.removePrefixes(iconId, IIconIdPrefix.URL);
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
    Matcher m = ICON_REGEX_PATTERN.matcher(str);
    StringBuilder ret = new StringBuilder();
    while (m.find()) {
      m.appendReplacement(ret, m.group(1) + createIconUrl(m.group(2)) + m.group(1));
    }
    m.appendTail(ret);
    return ret.toString();
  }

  public static String createDynamicAdapterResourceUrl(IJsonAdapter<?> jsonAdapter, BinaryResource binaryResource) {
    if (!checkDynamicAdapterResourceUrlArguments(jsonAdapter, binaryResource)) {
      return null;
    }
    return new DynamicResourceInfo(jsonAdapter, getFilenameWithFingerprint(binaryResource)).toPath();
  }

  /**
   * @return a relative URL for a resource handled by an adapter, see {@link DynamicResourceLoader}.
   * <p>
   * The calling adapter must implement {@link IBinaryResourceProvider}.
   */
  public static String createDynamicAdapterResourceUrl(IJsonAdapter<?> jsonAdapter, String filename) {
    if (!checkDynamicAdapterResourceUrlArguments(jsonAdapter, filename)) {
      return null;
    }
    return new DynamicResourceInfo(jsonAdapter, filename).toPath();
  }

  /**
   * @return a relative URL for a binaryRef resource, see {@link BinaryRefResourceLoader}.
   */
  public static String createBinaryRefResourceUrl(String binaryRef) {
    if (!checkBinaryRefResourceUrlArguments(binaryRef)) {
      return null;
    }
    return new BinaryRefResourceInfo(binaryRef).toPath();
  }

  private static boolean checkBinaryRefResourceUrlArguments(Object arg) {
    return arg != null;
  }

  /**
   * @param path
   *     decoded path (non URL encoded)
   */
  public static String getFilenameWithFingerprint(IJsonAdapter<?> jsonAdapter, String path) {
    if (!checkDynamicAdapterResourceUrlArguments(jsonAdapter, path)) {
      return null;
    }
    DynamicResourceInfo info = DynamicResourceInfo.fromPath(jsonAdapter, path);
    if (info == null) {
      return null;
    }
    return info.getFileName();
  }

  public static String getFilenameWithFingerprint(BinaryResource binaryResource) {
    return BinaryResourceUtility.createFilenameWithFingerprint(binaryResource);
  }

  private static boolean checkDynamicAdapterResourceUrlArguments(IJsonAdapter<?> jsonAdapter, Object arg) {
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
   * Helper method for {@link #BINARY_RESOURCE_REGEX_PATTERN} to replace all occurrences with the proper URL.
   */
  public static String replaceBinaryResourceHandlerWithUrl(IJsonAdapter<?> jsonAdapter, String str) {
    if (str == null) {
      return null;
    }
    Matcher m = BINARY_RESOURCE_REGEX_PATTERN.matcher(str);
    StringBuilder ret = new StringBuilder();
    while (m.find()) {
      m.appendReplacement(ret, m.group(1) + createDynamicAdapterResourceUrl(jsonAdapter, m.group(2)) + m.group(1));
    }
    m.appendTail(ret);
    return ret.toString();
  }

  /**
   * Helper method for {@link #BINARY_RESOURCE_REGEX_PATTERN} to replace all occurrences with the proper URL.
   */
  public static String replaceBinaryRefHandlerWithUrl(String str) {
    if (str == null) {
      return null;
    }
    Matcher m = BINARY_REF_REGEX_PATTERN.matcher(str);
    StringBuilder ret = new StringBuilder();
    while (m.find()) {
      m.appendReplacement(ret, m.group(1) + createBinaryRefResourceUrl(m.group(2)) + m.group(1));
    }
    m.appendTail(ret);
    return ret.toString();
  }

  /**
   * Helper method to replace all common placeholders for images in the given string.
   * <ol>
   * <li>Icon IDs in the format: <code>iconId:star</code></li>
   * <li>Binary resources in the format: <code>binaryResource:image</code></li>
   * </ol>
   */
  public static String replaceImageUrls(IJsonAdapter<?> jsonAdapter, String str) {
    str = replaceIconIdHandlerWithUrl(str);
    str = replaceBinaryRefHandlerWithUrl(str);
    return replaceBinaryResourceHandlerWithUrl(jsonAdapter, str);
  }

  /**
   * Provides a binary resource (holder) for the given filename, which may contain a fingerprint. The attachmentProvider
   * is a callback to retrieve the right binary resource from the model.
   */
  public static BinaryResourceHolder provideBinaryResource(String filenameWithFingerprint, Function<String, BinaryResource> attachmentProvider) {
    Pair<String, Long> filenameAndFingerprint = BinaryResourceUtility.extractFilenameWithFingerprint(filenameWithFingerprint);
    String filename = filenameAndFingerprint.getLeft();
    BinaryResource attachment = attachmentProvider.apply(filename);
    return attachment == null ? null : new BinaryResourceHolder(attachment);
  }

  public static BinaryResource extractBinaryResource(Object raw, String prefix, String fileExtension) {
    if (raw instanceof BinaryResource) {
      return (BinaryResource) raw;
    }
    if (raw instanceof byte[]) {
      Adler32 crc = new Adler32();
      crc.update((byte[]) raw);
      return new BinaryResource(join("-", prefix, crc.getValue(), ((byte[]) raw).length) + "." + fileExtension, (byte[]) raw);
    }
    return null;
  }
}
