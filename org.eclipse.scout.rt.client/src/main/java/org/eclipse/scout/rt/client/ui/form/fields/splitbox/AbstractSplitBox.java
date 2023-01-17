/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.splitbox;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.splitbox.ISplitBoxExtension;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractCompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.internal.SplitBoxGrid;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * @since 3.1.12 16.07.2008
 */
@ClassId("2b156923-e659-4993-8d5d-559f140ec59d")
public abstract class AbstractSplitBox extends AbstractCompositeField implements ISplitBox {

  private SplitBoxGrid m_grid;
  private ISplitBoxUIFacade m_uiFacade;
  private boolean m_cacheSplitterPosition;
  private String m_cacheSplitterPositionPropertyName;

  public AbstractSplitBox() {
    this(true);
  }

  public AbstractSplitBox(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected boolean getConfiguredGridUseUiHeight() {
    return true;
  }

  @Override
  protected int getConfiguredGridW() {
    return FULL_WIDTH;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(250)
  protected boolean getConfiguredSplitHorizontal() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(300)
  protected boolean getConfiguredSplitterEnabled() {
    return true;
  }

  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(340)
  protected double getConfiguredSplitterPosition() {
    return 0.5;
  }

  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(345)
  protected Double getConfiguredMinSplitterPosition() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(340)
  protected String getConfiguredSplitterPositionType() {
    return SPLITTER_POSITION_TYPE_RELATIVE_FIRST;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(355)
  protected boolean getConfiguredCacheSplitterPosition() {
    return false;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(360)
  protected String getConfiguredCacheSplitterPositionPropertyName() {
    return getClass().getName();
  }

  @Override
  public String getPreferenceBaseKey() {
    return getConfiguredCacheSplitterPositionPropertyName();
  }

  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(370)
  protected Class<? extends IFormField> getConfiguredCollapsibleField() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(380)
  protected boolean getConfiguredFieldCollapsed() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(400)
  protected boolean getConfiguredFieldMinimized() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(400)
  protected boolean getConfiguredMinimizeEnabled() {
    return true;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(410)
  protected String getConfiguredToogleCollapseKeyStroke() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(420)
  protected String getConfiguredFirstCollapseKeyStroke() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(430)
  protected String getConfiguredSecondCollapseKeyStroke() {
    return null;
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    m_grid = new SplitBoxGrid();
    super.initConfig();
    setSplitHorizontal(getConfiguredSplitHorizontal());
    setSplitterEnabled(getConfiguredSplitterEnabled());
    setCacheSplitterPosition(getConfiguredCacheSplitterPosition());
    setCacheSplitterPositionPropertyName(getConfiguredCacheSplitterPositionPropertyName());
    setMinSplitterPosition(getConfiguredMinSplitterPosition());
    setSplitterPositionType(getConfiguredSplitterPositionType());

    Double cachedSplitterPosition = ClientUIPreferences.getInstance().getSplitterPosition(this);
    if (isCacheSplitterPosition() && cachedSplitterPosition != null) {
      setSplitterPosition(cachedSplitterPosition);
    }
    else {
      setSplitterPosition(getConfiguredSplitterPosition());
    }
    if (getConfiguredCollapsibleField() != null) {
      setCollapsibleField(getFieldByClass(getConfiguredCollapsibleField()));
    }
    Boolean cachedFieldCollapsed = ClientUIPreferences.getInstance().getSplitBoxFieldCollapsed(this);
    if (isCacheSplitterPosition() && cachedFieldCollapsed != null) {
      setFieldCollapsed(cachedFieldCollapsed);
    }
    else {
      setFieldCollapsed(getConfiguredFieldCollapsed());
    }

    setToggleCollapseKeyStroke(getConfiguredToogleCollapseKeyStroke());
    setFirstCollapseKeyStroke(getConfiguredFirstCollapseKeyStroke());
    setSecondCollapseKeyStroke(getConfiguredSecondCollapseKeyStroke());

    Boolean cachedFieldMinimized = ClientUIPreferences.getInstance().getSplitBoxFieldMinimized(this);
    if (isCacheSplitterPosition() && cachedFieldMinimized != null) {
      setFieldMinimized(cachedFieldMinimized);
    }
    else {
      setFieldMinimized(getConfiguredFieldMinimized());
    }
    setMinimizeEnabled(getConfiguredMinimizeEnabled());

    getChildren().stream()
        .filter(child -> child instanceof IGroupBox)
        .map(child -> (IGroupBox) child)
        .filter(groupBox -> groupBox.isResponsive().isUndefined())
        .forEach(groupBox -> groupBox.setResponsive(true));
  }

  @Override
  public void rebuildFieldGrid() {
    m_grid.validate(this);
    if (isInitConfigDone() && getForm() != null) {
      getForm().structureChanged(this);
    }
  }

  @Override
  public SplitBoxGrid getFieldGrid() {
    return m_grid;
  }

  @Override
  protected void handleChildFieldVisibilityChanged() {
    super.handleChildFieldVisibilityChanged();
    if (isInitConfigDone()) {
      rebuildFieldGrid();
    }
  }

  @Override
  public boolean isSplitHorizontal() {
    return propertySupport.getPropertyBool(PROP_SPLIT_HORIZONTAL);
  }

  @Override
  public void setSplitHorizontal(boolean horizontal) {
    propertySupport.setPropertyBool(PROP_SPLIT_HORIZONTAL, horizontal);
  }

  @Override
  public boolean isSplitterEnabled() {
    return propertySupport.getPropertyBool(PROP_SPLITTER_ENABLED);
  }

  @Override
  public void setSplitterEnabled(boolean enabled) {
    propertySupport.setPropertyBool(PROP_SPLITTER_ENABLED, enabled);
  }

  @Override
  public double getSplitterPosition() {
    return propertySupport.getPropertyDouble(PROP_SPLITTER_POSITION);
  }

  @Override
  public void setSplitterPosition(double position) {
    propertySupport.setPropertyDouble(PROP_SPLITTER_POSITION, position);
  }

  @Override
  public Double getMinSplitterPosition() {
    return (Double) propertySupport.getProperty(PROP_MIN_SPLITTER_POSITION);
  }

  @Override
  public void setMinSplitterPosition(Double minPosition) {
    propertySupport.setProperty(PROP_MIN_SPLITTER_POSITION, minPosition);
  }

  @Override
  public String getSplitterPositionType() {
    return propertySupport.getPropertyString(PROP_SPLITTER_POSITION_TYPE);
  }

  @Override
  public void setSplitterPositionType(String splitterPositionType) {
    propertySupport.setPropertyString(PROP_SPLITTER_POSITION_TYPE, splitterPositionType);
  }

  @Override
  public boolean isFieldMinimized() {
    return propertySupport.getPropertyBool(PROP_FIELD_MINIMIZED);
  }

  @Override
  public void setFieldMinimized(boolean minimized) {
    propertySupport.setPropertyBool(PROP_FIELD_MINIMIZED, minimized);
  }

  @Override
  public boolean isMinimizeEnabled() {
    return propertySupport.getPropertyBool(PROP_MINIMIZE_ENABLED);
  }

  @Override
  public void setMinimizeEnabled(boolean enabled) {
    propertySupport.setPropertyBool(PROP_MINIMIZE_ENABLED, enabled);
  }

  @Override
  public boolean isCacheSplitterPosition() {
    return m_cacheSplitterPosition;
  }

  @Override
  public void setCacheSplitterPosition(boolean b) {
    m_cacheSplitterPosition = b;
  }

  @Override
  public String getCacheSplitterPositionPropertyName() {
    return m_cacheSplitterPositionPropertyName;
  }

  @Override
  public void setCacheSplitterPositionPropertyName(String propName) {
    m_cacheSplitterPositionPropertyName = propName;
  }

  @Override
  public void setCollapsibleField(IFormField field) {
    propertySupport.setProperty(PROP_COLLAPSIBLE_FIELD, field);
  }

  @Override
  public IFormField getCollapsibleField() {
    return (IFormField) propertySupport.getProperty(PROP_COLLAPSIBLE_FIELD);
  }

  @Override
  public void setFieldCollapsed(boolean collapsed) {
    propertySupport.setProperty(PROP_FIELD_COLLAPSED, collapsed);
  }

  @Override
  public boolean isFieldCollapsed() {
    return propertySupport.getPropertyBool(PROP_FIELD_COLLAPSED);
  }

  @Override
  public void setToggleCollapseKeyStroke(String keyStroke) {
    propertySupport.setProperty(PROP_TOGGLE_COLLAPSE_KEY_STROKE, keyStroke);
  }

  @Override
  public String getToggleCollapseKeyStroke() {
    return propertySupport.getPropertyString(PROP_TOGGLE_COLLAPSE_KEY_STROKE);
  }

  @Override
  public void setFirstCollapseKeyStroke(String keyStroke) {
    propertySupport.setProperty(PROP_FIRST_COLLAPSE_KEY_STROKE, keyStroke);
  }

  @Override
  public String getFirstCollapseKeyStroke() {
    return propertySupport.getPropertyString(PROP_FIRST_COLLAPSE_KEY_STROKE);
  }

  @Override
  public void setSecondCollapseKeyStroke(String keyStroke) {
    propertySupport.setProperty(PROP_SECOND_COLLAPSE_KEY_STROKE, keyStroke);
  }

  @Override
  public String getSecondCollapseKeyStroke() {
    return propertySupport.getPropertyString(PROP_SECOND_COLLAPSE_KEY_STROKE);
  }

  @Override
  public ISplitBoxUIFacade getUIFacade() {
    return m_uiFacade;
  }

  protected class P_UIFacade implements ISplitBoxUIFacade {

    @Override
    public void setSplitterPositionFromUI(double splitterPosition) {
      setSplitterPosition(splitterPosition);
      ClientUIPreferences.getInstance().setAllSplitBoxPreferences(AbstractSplitBox.this);
    }

    @Override
    public void setMinSplitterPositionFromUI(Double minSplitterPosition) {
      setMinSplitterPosition(minSplitterPosition);
    }

    @Override
    public void setFieldCollapsedFromUI(boolean collapsed) {
      setFieldCollapsed(collapsed);
      ClientUIPreferences.getInstance().setAllSplitBoxPreferences(AbstractSplitBox.this);
    }

    @Override
    public void setFieldMinimizedFromUI(boolean minimized) {
      setFieldMinimized(minimized);
      ClientUIPreferences.getInstance().setAllSplitBoxPreferences(AbstractSplitBox.this);
    }

    @Override
    public void setMinimizeEnabledFromUI(boolean enabled) {
      setMinimizeEnabled(enabled);
    }
  } // end UIFacade

  protected static class LocalSplitBoxExtension<OWNER extends AbstractSplitBox> extends LocalCompositeFieldExtension<OWNER> implements ISplitBoxExtension<OWNER> {

    public LocalSplitBoxExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected ISplitBoxExtension<? extends AbstractSplitBox> createLocalExtension() {
    return new LocalSplitBoxExtension<>(this);
  }

}
