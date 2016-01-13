/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.splitbox;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.splitbox.ISplitBoxExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractCompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.internal.SplitBoxGrid;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * <h3>AbstractSplitBox</h3> ...
 *
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

  // configuration

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

  @ConfigProperty(ConfigProperty.STRING)
  @Order(340)
  protected String getConfiguredSplitterPositionType() {
    return SPLITTER_POSITION_TYPE_RELATIVE;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(355)
  protected boolean getConfiguredCacheSplitterPosition() {
    return true;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(360)
  protected String getConfiguredCacheSplitterPositionPropertyName() {
    return getClass().getName();
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    m_grid = new SplitBoxGrid(this);
    super.initConfig();
    setSplitHorizontal(getConfiguredSplitHorizontal());
    setSplitterEnabled(getConfiguredSplitterEnabled());
    setSplitterPosition(getConfiguredSplitterPosition());
    setSplitterPositionType(getConfiguredSplitterPositionType());
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

  @Override
  public final int getGridColumnCount() {
    return m_grid.getGridColumnCount();
  }

  @Override
  public final int getGridRowCount() {
    return m_grid.getGridRowCount();
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
  public String getSplitterPositionType() {
    return propertySupport.getPropertyString(PROP_SPLITTER_POSITION_TYPE);
  }

  @Override
  public void setSplitterPositionType(String splitterPositionType) {
    propertySupport.setPropertyString(PROP_SPLITTER_POSITION_TYPE, splitterPositionType);
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
  public ISplitBoxUIFacade getUIFacade() {
    return m_uiFacade;
  }

  protected class P_UIFacade implements ISplitBoxUIFacade {

    @Override
    public void setSplitterPositionFromUI(double splitterPosition) {
      setSplitterPosition(splitterPosition);
    }
  } // end UIFacade

  protected static class LocalSplitBoxExtension<OWNER extends AbstractSplitBox> extends LocalCompositeFieldExtension<OWNER> implements ISplitBoxExtension<OWNER> {

    public LocalSplitBoxExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected ISplitBoxExtension<? extends AbstractSplitBox> createLocalExtension() {
    return new LocalSplitBoxExtension<AbstractSplitBox>(this);
  }

}
