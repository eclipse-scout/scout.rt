/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.window.desktop.toolbar;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractFormToolButton;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.services.common.patchedclass.IPatchedClassService;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * <h3>ViewButtonBar</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.7.0 June 2011
 */
public class RwtScoutToolButtonBar extends RwtScoutComposite<IDesktop> implements IRwtScoutToolButtonBar<IDesktop> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutToolButtonBar.class);

  private static final String VARIANT_TOOL_BUTTON_BAR = "toolButtonBar";
  private static final String VARIANT_TOOL_BUTTON_BAR_ACTIVE = "toolButtonBar-active";
  private static final String VARIANT_TOOLBAR_CONTAINER = "toolbarContainer";
  private static final String VARIANT_TOOL_BUTTON_BAR_COLLAPSE_BUTTON = "toolButtonBarCollapseButton";
  private static final String VARIANT_TOOL_BUTTON_BUTTON_ACTIVE = "toolButton-active";
  private static final String VARIANT_TOOL_BUTTON = "toolButton";

  private HashMap<IToolButton, IRwtScoutToolButton> m_toolTabItems;
  private Label m_toolButtonsLabel;

  private PropertyChangeListener m_toolbuttonPropertyListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(final PropertyChangeEvent e) {
      Runnable t = new Runnable() {
        @Override
        public void run() {
          if (!isUiDisposed()) {
            //
            handleScoutToolButtonPropertyChange(e.getPropertyName(), e.getNewValue());
          }
        }
      };
      getUiEnvironment().invokeUiLater(t);
    }
  };
  private Composite m_toolButtonContainer;
  private Button m_toolButtonCollapseButton;

  public RwtScoutToolButtonBar() {
    m_toolTabItems = new HashMap<IToolButton, IRwtScoutToolButton>();
  }

  @Override
  protected void initializeUi(Composite parent) {
    Composite toolButtonBar = getUiEnvironment().getFormToolkit().createComposite(parent);
    toolButtonBar.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TOOL_BUTTON_BAR);
    m_toolButtonCollapseButton = getUiEnvironment().getFormToolkit().createButton(toolButtonBar, "", SWT.PUSH);
    m_toolButtonCollapseButton.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TOOL_BUTTON_BAR_COLLAPSE_BUTTON);
    m_toolButtonCollapseButton.addSelectionListener(new SelectionAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetSelected(SelectionEvent e) {
        collapseToolView();
      }
    });
    m_toolButtonsLabel = getUiEnvironment().getFormToolkit().createLabel(toolButtonBar, "", SWT.CENTER);
    m_toolButtonsLabel.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TOOL_BUTTON);
    m_toolButtonsLabel.addMouseListener(new MouseAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void mouseDown(MouseEvent e) {
        // Simulate click on collapse button
        m_toolButtonCollapseButton.notifyListeners(SWT.Selection, null);
      }
    });
    m_toolButtonContainer = getUiEnvironment().getFormToolkit().createComposite(toolButtonBar);
    m_toolButtonContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TOOL_BUTTON_BAR);
    for (IToolButton scoutButton : getScoutObject().getToolButtons()) {
      if (!(scoutButton instanceof AbstractFormToolButton)) {
        continue;
      }

      String simpleClassName = scoutButton.getClass().getSimpleName();
      String variant = VARIANT_TOOL_BUTTON;
      String activeVariant = VARIANT_TOOL_BUTTON_BUTTON_ACTIVE;
      variant += "-" + simpleClassName;
      activeVariant += "-" + simpleClassName;
      IRwtScoutToolButtonForPatch uiToolButton = SERVICES.getService(IPatchedClassService.class).createRwtScoutToolButton(false, true, variant, activeVariant);
      uiToolButton.createUiField(m_toolButtonContainer, scoutButton, getUiEnvironment());
      m_toolTabItems.put(scoutButton, uiToolButton);
    }

    //layout
    GridLayout toolButtonBarLayout = RwtLayoutUtility.createGridLayoutNoSpacing(3, false);
    toolButtonBarLayout.marginLeft = 10;
    toolButtonBarLayout.marginRight = 10;
    toolButtonBar.setLayout(toolButtonBarLayout);

    GridData collapseButtonLayoutData = new GridData(GridData.VERTICAL_ALIGN_CENTER);
    collapseButtonLayoutData.heightHint = 15;
    collapseButtonLayoutData.widthHint = 15;
    collapseButtonLayoutData.exclude = true;
    m_toolButtonCollapseButton.setLayoutData(collapseButtonLayoutData);

    GridData labelData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
    labelData.exclude = true;
    m_toolButtonsLabel.setLayoutData(labelData);

    GridData tabFolderLayoutData = new GridData(GridData.HORIZONTAL_ALIGN_END);
    m_toolButtonContainer.setLayoutData(tabFolderLayoutData);
    RowLayout layout = new RowLayout(SWT.HORIZONTAL);
    layout.marginBottom = 0;
    layout.spacing = 15;
    m_toolButtonContainer.setLayout(layout);

    for (IRwtScoutToolButton b : m_toolTabItems.values()) {
      RowData data = new RowData();
      Object o = b.getUiField().getLayoutData();
      if (o instanceof RowData) {
        data = (RowData) o;
      }
      data.width = 32;
      data.height = 28;
      b.getUiField().setLayoutData(data);
    }

    setUiContainer(toolButtonBar);
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    for (IToolButton scoutButton : getScoutObject().getToolButtons()) {
      scoutButton.addPropertyChangeListener(m_toolbuttonPropertyListener);
    }
    updateToolButtonLabel();
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    for (IToolButton scoutButton : getScoutObject().getToolButtons()) {
      scoutButton.removePropertyChangeListener(m_toolbuttonPropertyListener);
    }
  }

  protected void collapseToolView() {
    Runnable r = new Runnable() {
      @Override
      public void run() {
        for (IToolButton scoutButton : getScoutObject().getToolButtons()) {
          if (scoutButton.isSelected()) {
            scoutButton.getUIFacade().setSelectedFromUI(false);
          }
        }
      }
    };
    getUiEnvironment().invokeScoutLater(r, 0);

  }

  protected void updateToolButtonLabel() {
    String label = null;
    for (IToolButton scoutButton : getScoutObject().getToolButtons()) {
      if (!(scoutButton instanceof AbstractFormToolButton)) {
        continue;
      }
      if (scoutButton.isSelected()) {
        label = scoutButton.getText();
        m_toolButtonContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TOOL_BUTTON_BAR_ACTIVE);
      }
    }
    GridData collapseButtonData = (GridData) m_toolButtonCollapseButton.getLayoutData();
    GridData labelData = (GridData) m_toolButtonsLabel.getLayoutData();
    if (label == null) {
      collapseButtonData.exclude = true;
      m_toolButtonCollapseButton.setVisible(false);
      labelData.exclude = true;
      m_toolButtonsLabel.setVisible(false);
      label = "";
    }
    else {
      collapseButtonData.exclude = false;
      m_toolButtonCollapseButton.setVisible(true);
      labelData.exclude = false;
      m_toolButtonsLabel.setVisible(true);
    }
    m_toolButtonsLabel.setText(label);
    getUiContainer().getParent().layout(true, true);
  }

  protected void handleScoutToolButtonPropertyChange(String propertyName, Object newValue) {
    updateToolButtonLabel();
  }

}
