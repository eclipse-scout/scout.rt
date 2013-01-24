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

import org.eclipse.scout.rt.shared.Activator;

/**
 * Holds information about the kind of user interface used on the client side like {@link IUiLayer} and
 * {@link IUiDeviceType}. <br/>
 * There is also a device id ({@link UserAgent#getUiDeviceId()} which holds even more
 * information about the used device. As default it only provides information about the underlying operation system. In
 * case of a web based ui {@link IUiLayer#isWebUi()} it provides the original user agent string containing information
 * about the browser.
 * <p>
 * In order to export the user agent data as string you can use {@link #createIdentifier()} which uses
 * {@link DefaultUserAgentParser}. If you would like to export it in a custom format just create a custom
 * {@link IUserAgentParser} and call {@link #createIdentifier(IUserAgentParser)}.
 * </p>
 * 
 * @since 3.8.0
 */
public final class UserAgent {

  private String m_uiDeviceId;
  private IUiLayer m_uiLayer;
  private IUiDeviceType m_uiDeviceType;

  private UserAgent(IUiLayer uiLayer, IUiDeviceType uiDeviceType, String uiDeviceId) {
    if (uiLayer == null || uiDeviceType == null || uiDeviceId == null) {
      throw new IllegalArgumentException("Arguments must not be null.");
    }

    m_uiLayer = uiLayer;
    m_uiDeviceType = uiDeviceType;
    m_uiDeviceId = uiDeviceId;
  }

  public IUiDeviceType getUiDeviceType() {
    return m_uiDeviceType;
  }

  public IUiLayer getUiLayer() {
    return m_uiLayer;
  }

  public String getUiDeviceId() {
    return m_uiDeviceId;
  }

  public void setUiDeviceId(String uiDeviceId) {
    m_uiDeviceId = uiDeviceId;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }

    if (other == null || other.getClass() != getClass()) {
      return false;
    }

    UserAgent otherUserAgent = (UserAgent) other;
    boolean equal = otherUserAgent.getUiDeviceType().equals(getUiDeviceType());
    equal &= otherUserAgent.getUiLayer().equals(getUiLayer());
    equal &= otherUserAgent.getUiDeviceId().equals(getUiDeviceId());

    return equal;
  }

  @Override
  public int hashCode() {
    int hash = 17 + getUiLayer().hashCode();
    hash = hash * 17 + getUiDeviceType().hashCode();
    hash = hash * 17 + getUiDeviceId().hashCode();

    return hash;
  }

  @Override
  public String toString() {
    return createIdentifier();
  }

  public String createIdentifier() {
    return createIdentifier(new DefaultUserAgentParser());
  }

  public String createIdentifier(IUserAgentParser parser) {
    return parser.createIdentifier(this);
  }

  public static UserAgent create(IUiLayer uiLayer, IUiDeviceType uiDeviceType) {
    String osName = Activator.getDefault().getBundle().getBundleContext().getProperty("org.osgi.framework.os.name");

    return new UserAgent(uiLayer, uiDeviceType, osName);
  }

  public static UserAgent create(IUiLayer uiLayer, IUiDeviceType uiDeviceType, String uiDeviceId) {
    return new UserAgent(uiLayer, uiDeviceType, uiDeviceId);
  }

  public static UserAgent createDefault() {
    return create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN);
  }

  public static UserAgent createByIdentifier(IUserAgentParser parser, String userAgent) {
    return parser.parseIdentifier(userAgent);
  }

  public static UserAgent createByIdentifier(String userAgent) {
    return createByIdentifier(new DefaultUserAgentParser(), userAgent);
  }

}
