/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.toggleswitch;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("8f5e0016-cd17-47ce-bdac-034888f6915f")
public class AbstractToggleSwitch extends AbstractWidget implements IToggleSwitch {

  private IToggleSwitchUIFacade m_uiFacade;

  public AbstractToggleSwitch() {
    this(true);
  }

  public AbstractToggleSwitch(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(createUIFacade(), ModelContext.copyCurrent());
    super.initConfig();
    setActivated(getConfiguredActivated());
    setLabel(getConfiguredLabel());
    setLabelHtmlEnabled(getConfiguredLabelHtmlEnabled());
    setLabelVisible(getConfiguredLabelVisible());
    setTooltipText(getConfiguredTooltipText());
    setIconVisible(getConfiguredIconVisible());
    setDisplayStyle(getConfiguredDisplayStyle());
    setTabbable(getConfiguredTabbable());
  }

  @Override
  public IToggleSwitchUIFacade getUIFacade() {
    return m_uiFacade;
  }

  protected IToggleSwitchUIFacade createUIFacade() {
    return new P_UIFacade();
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(10)
  protected boolean getConfiguredActivated() {
    return false;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(20)
  protected String getConfiguredLabel() {
    return null;
  }

  /**
   * Configures, if HTML rendering is enabled.
   * <p>
   * Subclasses can override this method. Default is {@code false}. Make sure that any user input (or other insecure
   * input) is encoded (security), if this property is enabled.
   *
   * @return {@code true}, if HTML rendering is enabled, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(21)
  protected boolean getConfiguredLabelHtmlEnabled() {
    return false;
  }

  /**
   * <ul>
   * <li>{@code true}: label is visible
   * <li>{@code false}: label is invisible
   * <li>{@code null}: label is visible depending on whether {@link #getLabel()} contains text
   * </ul>
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(22)
  protected Boolean getConfiguredLabelVisible() {
    return null;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(30)
  protected String getConfiguredTooltipText() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(40)
  protected boolean getConfiguredIconVisible() {
    return false;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(50)
  protected String getConfiguredDisplayStyle() {
    return DISPLAY_STYLE_DEFAULT;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(60)
  protected boolean getConfiguredTabbable() {
    return false;
  }

  @Override
  public boolean isActivated() {
    return propertySupport.getPropertyBool(PROP_ACTIVATED);
  }

  @Override
  public void setActivated(boolean activated) {
    propertySupport.setPropertyBool(PROP_ACTIVATED, activated);
  }

  @Override
  public String getLabel() {
    return propertySupport.getPropertyString(PROP_LABEL);
  }

  @Override
  public void setLabel(String label) {
    propertySupport.setPropertyString(PROP_LABEL, label);
  }

  @Override
  public boolean isLabelHtmlEnabled() {
    return propertySupport.getPropertyBool(PROP_LABEL_HTML_ENABLED);
  }

  @Override
  public void setLabelHtmlEnabled(boolean labelHtmlEnabled) {
    propertySupport.setPropertyBool(PROP_LABEL_HTML_ENABLED, labelHtmlEnabled);
  }

  @Override
  public Boolean getLabelVisible() {
    return propertySupport.getProperty(PROP_LABEL_VISIBLE, Boolean.class);
  }

  @Override
  public void setLabelVisible(Boolean labelVisible) {
    propertySupport.setProperty(PROP_LABEL_VISIBLE, labelVisible);
  }

  @Override
  public String getTooltipText() {
    return propertySupport.getPropertyString(PROP_TOOLTIP_TEXT);
  }

  @Override
  public void setTooltipText(String tooltipText) {
    propertySupport.setPropertyString(PROP_TOOLTIP_TEXT, tooltipText);
  }

  @Override
  public boolean isIconVisible() {
    return propertySupport.getPropertyBool(PROP_ICON_VISIBLE);
  }

  @Override
  public void setIconVisible(boolean iconVisible) {
    propertySupport.setPropertyBool(PROP_ICON_VISIBLE, iconVisible);
  }

  @Override
  public String getDisplayStyle() {
    return propertySupport.getPropertyString(PROP_DISPLAY_STYLE);
  }

  @Override
  public void setDisplayStyle(String displayStyle) {
    propertySupport.setPropertyString(PROP_DISPLAY_STYLE, displayStyle);
  }

  @Override
  public boolean isTabbable() {
    return propertySupport.getPropertyBool(PROP_TABBABLE);
  }

  @Override
  public void setTabbable(boolean tabbable) {
    propertySupport.setPropertyBool(PROP_TABBABLE, tabbable);
  }

  protected class P_UIFacade implements IToggleSwitchUIFacade {

    @Override
    public void setActivatedFromUI(boolean activated) {
      if (!isEnabledIncludingParents()) {
        return;
      }
      setActivated(activated);
    }
  }
}
