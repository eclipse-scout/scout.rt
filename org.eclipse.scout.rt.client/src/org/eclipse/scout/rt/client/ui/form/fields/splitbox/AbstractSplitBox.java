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
package org.eclipse.scout.rt.client.ui.form.fields.splitbox;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractCompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.internal.SplitBoxGrid;

/**
 * <h3>AbstractSplitBox</h3> ...
 * 
 * @since 3.1.12 16.07.2008
 */
public abstract class AbstractSplitBox extends AbstractCompositeField implements ISplitBox {

  private SplitBoxGrid m_grid;
  private ISplitboxUIFacade m_uiFacade;
  private boolean m_cacheSplitterPosition;
  private String m_cacheSplitterPositionPropertyName;

  public AbstractSplitBox() {
    super();
  }

  // configuration

  @ConfigPropertyValue("true")
  @Override
  protected boolean getConfiguredGridUseUiHeight() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(250)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredSplitHorizontal() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(300)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredSplitterEnabled() {
    return true;
  }

  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(340)
  @ConfigPropertyValue("0.5")
  protected double getConfiguredSplitterPosition() {
    return 0.5;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(355)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredCacheSplitterPosition() {
    return true;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(360)
  @ConfigPropertyValue("getClass().getName")
  protected String getConfiguredCacheSplitterPositionPropertyName() {
    return getClass().getName();
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    m_grid = new SplitBoxGrid(this);
    super.initConfig();
    setSplitHorizontal(getConfiguredSplitHorizontal());
    setSpliterEnabled(getConfiguredSplitterEnabled());
    setSplitterPosition(getConfiguredSplitterPosition());
    setCacheSplitterPosition(getConfiguredCacheSplitterPosition());
    setCacheSplitterPositionPropertyName(getConfiguredCacheSplitterPositionPropertyName());
  }

  @Override
  public void rebuildFieldGrid() {
    m_grid.validate();
    if (isInitialized()) {
      if (getForm() != null) {
        getForm().structureChanged(this);
      }
    }
  }

  @Override
  protected void handleFieldVisibilityChanged() {
    super.handleFieldVisibilityChanged();
    if (isInitialized()) {
      rebuildFieldGrid();
    }
  }

  public final int getGridColumnCount() {
    return m_grid.getGridColumnCount();
  }

  public final int getGridRowCount() {
    return m_grid.getGridRowCount();
  }

  public boolean isSplitHorizontal() {
    return propertySupport.getPropertyBool(PROP_SPLIT_HORIZONTAL);
  }

  public void setSplitHorizontal(boolean horizontal) {
    propertySupport.setPropertyBool(PROP_SPLIT_HORIZONTAL, horizontal);
  }

  public boolean isSplitterEnabled() {
    return propertySupport.getPropertyBool(PROP_SPLITTER_ENABLED);
  }

  public void setSpliterEnabled(boolean enabled) {
    propertySupport.setPropertyBool(PROP_SPLITTER_ENABLED, enabled);
  }

  public double getSplitterPosition() {
    return propertySupport.getPropertyDouble(PROP_SPLITTER_POSITION);
  }

  public void setSplitterPosition(double position) {
    propertySupport.setPropertyAlwaysFire(PROP_SPLITTER_POSITION, position);
  }

  public boolean isCacheSplitterPosition() {
    return m_cacheSplitterPosition;
  }

  public void setCacheSplitterPosition(boolean b) {
    m_cacheSplitterPosition = b;
  }

  public String getCacheSplitterPositionPropertyName() {
    return m_cacheSplitterPositionPropertyName;
  }

  public void setCacheSplitterPositionPropertyName(String propName) {
    m_cacheSplitterPositionPropertyName = propName;
  }

  public ISplitboxUIFacade getUIFacade() {
    return m_uiFacade;
  }

  private class P_UIFacade implements ISplitboxUIFacade {
  } // end UIFacade

}
