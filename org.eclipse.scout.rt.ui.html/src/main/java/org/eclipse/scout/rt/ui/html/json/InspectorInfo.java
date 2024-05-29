/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.servlet.UrlHints;
import org.json.JSONObject;

/**
 * Adds inspector properties ({@value #PROP_CLASS_ID}, {@value #PROP_MODEL_CLASS}) to a {@link JSONObject}.
 *
 * @since 5.2
 */
@ApplicationScoped
public class InspectorInfo {

  public static final String PROP_CLASS_ID = "classId";
  public static final String PROP_MODEL_CLASS = "modelClass";

  /**
   * Regex for a UUID as created by {@link UUID}.
   */
  private static final String UUID_PATTERN = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
  /**
   * Pattern for concatenated classIds as e.g. created by {@link AbstractWidget#classId()}.
   */
  public static final Pattern CLASS_ID_WITH_UUID_PATTERN = Pattern.compile(UUID_PATTERN + "(?:" + ITypeWithClassId.ID_CONCAT_SYMBOL + UUID_PATTERN + ")*");
  private static final MessageDigest SHA256 = createSha256Digest();

  private static MessageDigest createSha256Digest() {
    try {
      return MessageDigest.getInstance("SHA-256");
    }
    catch (NoSuchAlgorithmException e) {
      throw new ProcessingException("Unable to create SHA-256 message digest.", e);
    }
  }

  /**
   * Adds inspector properties ({@value #PROP_CLASS_ID} & {@value #PROP_MODEL_CLASS}) to the given {@link JSONObject}.
   *
   * @param req
   *     The {@link HttpServletRequest} to detect if the inspector is enabled
   *     ({@link UrlHints#isInspectorHint(HttpServletRequest)}). The {@value #PROP_MODEL_CLASS} property is only
   *     added if it is enabled. May be {@code null}.
   * @param json
   *     The target {@link JSONObject} that should receive the properties. May be {@code null} then this method
   *     does nothing.
   * @param model
   *     The model for which the properties should be added. May be {@code null} then this method does nothing.
   * @param classIdExtractor
   *     {@link Function} to extract the value for the {@value #PROP_CLASS_ID} property. May be {@code null}, then
   *     this property is not added. The input to the function is the model. The function may return {@code null}.
   */
  public <T> void put(HttpServletRequest req, JSONObject json, T model, Function<T, String> classIdExtractor) {
    if (json == null || model == null) {
      return;
    }
    if (classIdExtractor != null) {
      String id = classIdExtractor.apply(model);
      if (!StringUtility.isNullOrEmpty(id)) {
        json.put(PROP_CLASS_ID, prepareClassId(id));
      }
    }
    if (UrlHints.isInspectorHint(req)) {
      json.put(PROP_MODEL_CLASS, model.getClass().getName());
    }
  }

  /**
   * Prepares a classId value to be sent to the browser. It hashes Ids which contain non-UUID content to not expose
   * internal names.
   *
   * @param classId
   *          The classId to prepare. Must not be {@code null}.
   * @return The id ready to be sent to the browser.
   */
  public String prepareClassId(String classId) {
    if (CLASS_ID_WITH_UUID_PATTERN.matcher(classId).matches()) {
      // id only consists of UUIDs: allowed to send to the browser
      return classId;
    }

    // Here the uuid may contain e.g. class names -> hide internal details from browser.
    // Use custom hash (and not SecurityUtility.hash and no encryption) to ensure the string gets not too long.
    byte[] hashedId = SHA256.digest(classId.getBytes(StandardCharsets.UTF_8));
    // use Base32 encoding because it is shorter than hex and does not include special characters and is case-insensitive (compared to Base64).
    return new BigInteger(1, hashedId).toString(32);
  }
}
