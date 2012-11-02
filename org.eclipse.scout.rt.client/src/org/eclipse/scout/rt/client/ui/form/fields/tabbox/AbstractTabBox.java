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
package org.eclipse.scout.rt.client.ui.form.fields.tabbox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractCompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.internal.TabBoxGrid;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractTabBox extends AbstractCompositeField implements ITabBox {

  private ITabBoxUIFacade m_uiFacade;
  private TabBoxGrid m_grid;

  public AbstractTabBox() {
    this(true);
  }

  public AbstractTabBox(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */

  @ConfigPropertyValue("true")
  @Override
  protected boolean getConfiguredGridUseUiHeight() {
    return true;
  }

  @Override
  @ConfigPropertyValue("FULL_WIDTH")
  protected int getConfiguredGridW() {
    return FULL_WIDTH;
  }

  @ConfigOperation
  @Order(70)
  protected void execTabSelected(IGroupBox selectedBox) throws ProcessingException {
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    m_grid = new TabBoxGrid(this);
    super.initConfig();
    addPropertyChangeListener(PROP_SELECTED_TAB, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        // single observer exec
        try {
          execTabSelected(getSelectedTab());
        }
        catch (ProcessingException ex) {
          SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
        }
        catch (Throwable t) {
          SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
        }
      }
    });
  }

  /*
   * Runtime
   */

  @Override
  public void rebuildFieldGrid() {
    m_grid.validate();
    if (isInitialized()) {
      if (getForm() != null) {
        getForm().structureChanged(this);
      }
    }
  }

  // box is only visible when it has at least one visible item
  @Override
  protected void handleFieldVisibilityChanged() {
    super.handleFieldVisibilityChanged();
    if (isInitialized()) {
      rebuildFieldGrid();
    }
    IGroupBox selectedBox = getSelectedTab();
    if (selectedBox == null) {
      for (IGroupBox box : getGroupBoxes()) {
        if (box.isVisible()) {
          setSelectedTab(box);
          break;
        }
      }
    }
    else if (!selectedBox.isVisible()) {
      int index = getFieldIndex(selectedBox);
      IGroupBox[] boxes = getGroupBoxes();
      // next to right side
      for (int i = index + 1; i < getFieldCount(); i++) {
        if (boxes[i].isVisible()) {
          setSelectedTab(boxes[i]);
          break;
        }
      }
      if (getSelectedTab() == selectedBox) {
        // next to left side
        for (int i = index - 1; i >= 0; i--) {
          if (boxes[i].isVisible()) {
            setSelectedTab(boxes[i]);
            break;
          }
        }
      }
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
  public IGroupBox[] getGroupBoxes() {
    IGroupBox[] a = new IGroupBox[getFieldCount()];
    if (a.length > 0) {
      System.arraycopy(getFields(), 0, a, 0, a.length);
    }
    return a;
  }

  @Override
  public void setSelectedTab(IGroupBox box) {
    if (box.getParentField() == this) {
      propertySupport.setProperty(PROP_SELECTED_TAB, box);
    }
  }

  @Override
  public IGroupBox getSelectedTab() {
    return (IGroupBox) propertySupport.getProperty(PROP_SELECTED_TAB);
  }

  @Override
  public ITabBoxUIFacade getUIFacade() {
    return m_uiFacade;
  }

  private class P_UIFacade implements ITabBoxUIFacade {
    @Override
    public void setSelectedTabFromUI(IGroupBox box) {
      setSelectedTab(box);
    }
  }
}
