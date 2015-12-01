/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.ui;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This default parser is able to create a string representation (identifier) of {@link UserAgent} in the form of
 * uiLayer|uiDeviceType|uiDeviceId. It is also able to parse such a identifier and create a {@link UserAgent}.
 * <p>
 * Examples of identifiers:
 * <ul>
 * <li>SWING|DESKTOP|Windows7</li>
 * <li>RAP|MOBILE|Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162
 * Safari/535.19</li
 * </ul>
 * </p>
 *
 * @since 3.8.0
 */
public class DefaultUserAgentParser implements IUserAgentParser {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultUserAgentParser.class);

  public static String DELIMITER = "|";

  @Override
  public UserAgent parseIdentifier(String userAgent) {
    String[] tokens = StringUtility.tokenize(userAgent, DELIMITER.charAt(0));
    if (tokens.length != 3) {
      LOG.warn("UserAgentIdentifier could not be parsed. Invalid number of tokens. UserAgent: " + userAgent);
      return UserAgent.createDefault();
    }

    try {
      IUiLayer uiLayer = UiLayer.createByIdentifier(tokens[0]);
      IUiDeviceType uiDeviceType = UiDeviceType.createByIdentifier(tokens[1]);
      String uiDeviceId = tokens[2];

      return UserAgent.create(uiLayer, uiDeviceType, uiDeviceId);
    }
    catch (Exception t) {
      LOG.warn("UserAgentIdentifier could not be parsed. Exception occured while parsing. UserAgent: " + userAgent, t);
      return UserAgent.createDefault();
    }
  }

  @Override
  public String createIdentifier(UserAgent userAgent) {
    String uiDeviceId = userAgent.getUiDeviceId();
    if (uiDeviceId.contains(DELIMITER)) {
      uiDeviceId = uiDeviceId.replaceAll(DELIMITER, "_");

      LOG.warn("Character which is used as delimiter has been found in uiDeviceId. Replaced with '_'. Old uiDeviceId: " + userAgent.getUiDeviceId() + ". New uiDeviceId: " + uiDeviceId);
      userAgent.setUiDeviceId(uiDeviceId);
    }
    return StringUtility.concatenateTokens(userAgent.getUiLayer().getIdentifier(), DELIMITER + "", userAgent.getUiDeviceType().getIdentifier(), DELIMITER + "", uiDeviceId);
  }

}
