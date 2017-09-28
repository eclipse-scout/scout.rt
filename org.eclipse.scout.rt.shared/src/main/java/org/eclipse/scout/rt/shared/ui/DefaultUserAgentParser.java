/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.ui;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This default parser is able to create a string representation (identifier) of a {@link UserAgent}, as well as parse
 * such a string representation. It has the following form:
 * <p>
 * uiLayer|uiDeviceType|uiEngineType|uiSystem|uiDeviceId
 * <p>
 * Examples:
 * <ul>
 * <li>HTML|MOBILE|CHROME|WINDOWS|Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko)
 * Chrome/18.0.1025.162 Safari/535.19</li>
 * <li>UNKNOWN|UNKNOWN|UNKNOWN|UNKNOWN</li>
 * </ul>
 * </p>
 *
 * @since 3.8.0
 */
public class DefaultUserAgentParser implements IUserAgentParser {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultUserAgentParser.class);

  public static final String DELIMITER = "|";

  @Override
  public UserAgent parseIdentifier(String userAgent) {
    String[] tokens = StringUtility.tokenize(userAgent, DELIMITER.charAt(0));
    if (tokens.length != 5) {
      throw new PlatformException("Invalid number of tokens");
    }

    try {
      return UserAgents
          .create()
          .withUiLayer(UiLayer.createByIdentifier(tokens[0]))
          .withUiDeviceType(UiDeviceType.createByIdentifier(tokens[1]))
          .withUiEngineType(UiEngineType.createByIdentifier(tokens[2]))
          .withUiSystem(UiSystem.createByIdentifier(tokens[3]))
          .withDeviceId(tokens[4])
          .build();
    }
    catch (IllegalArgumentException e) {
      throw BEANS.get(PlatformExceptionTranslator.class)
          .translate(e)
          .withContextInfo("UserAgent", userAgent);
    }
  }

  @Override
  public String createIdentifier(UserAgent userAgent) {
    String uiDeviceId = userAgent.getUiDeviceId();
    if (uiDeviceId.contains(DELIMITER)) {
      uiDeviceId = uiDeviceId.replaceAll("\\" + DELIMITER, "_");
      LOG.warn("Character which is used as delimiter has been found in uiDeviceId. Replaced with '_'. Old uiDeviceId: {}. New uiDeviceId: {}", userAgent.getUiDeviceId(), uiDeviceId);
    }
    return StringUtility.concatenateTokens(
        userAgent.getUiLayer().getIdentifier(), DELIMITER,
        userAgent.getUiDeviceType().getIdentifier(), DELIMITER,
        userAgent.getUiEngineType().getIdentifier(), DELIMITER,
        userAgent.getUiSystem().getIdentifier(), DELIMITER,
        uiDeviceId);
  }

}
