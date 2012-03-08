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
package org.eclipse.scout.rt.ui.rap.window.desktop;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.IRwtStandaloneEnvironment;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.core.window.IRwtScoutPart;
import org.eclipse.scout.rt.ui.rap.core.window.desktop.IRwtDesktop;
import org.eclipse.scout.rt.ui.rap.core.window.desktop.IRwtScoutActionBar;
import org.eclipse.scout.rt.ui.rap.core.window.desktop.viewarea.ILayoutListener;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.window.desktop.toolbar.RwtScoutToolbar;
import org.eclipse.scout.rt.ui.rap.window.desktop.viewarea.ViewArea;
import org.eclipse.scout.rt.ui.rap.window.desktop.viewarea.ViewArea.SashKey;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Sash;

/**
 * <h3>RwtScoutDesktop</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.7.0 June 2011
 */
public class RwtScoutDesktop extends RwtScoutComposite<IDesktop> implements IRwtDesktop {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutDesktop.class);

  private static final String VARIANT_VIEWS_AREA = "viewsArea";
  private ViewArea m_viewArea;
  private RwtScoutToolbar m_uiToolbar;

  public RwtScoutDesktop() {
  }

  @Override
  protected void attachScout() {
    super.attachScout();
  }

  @Override
  protected void detachScout() {
    super.detachScout();
  }

  @Override
  protected void initializeUi(Composite parent) {
    try {
      Composite desktopComposite = parent;
      Control toolbar = createToolBar(desktopComposite);
      Control viewsArea = createViewsArea(desktopComposite);
      Control actionBar = createActionBar(desktopComposite);
      viewsArea.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_VIEWS_AREA);

      createLayout(desktopComposite, toolbar, actionBar, viewsArea);

      setUiContainer(desktopComposite);
    }
    catch (Throwable t) {
      LOG.error("Exception occured while creating ui desktop.", t);
    }
  }

  protected void createLayout(Composite container, Control toolbar, Control actionBar, Control viewsArea) {
    GridLayout layout = RwtUtility.createGridLayoutNoSpacing(1, true);
    container.setLayout(layout);

    if (toolbar != null) {
      GridData toolbarData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
      toolbar.setLayoutData(toolbarData);
    }

    if (viewsArea != null) {
      GridData viewsAreaData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
      viewsArea.setLayoutData(viewsAreaData);
    }

    if (actionBar != null) {
      GridData actionBarData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
      actionBar.setLayoutData(actionBarData);
    }
  }

  protected Control createToolBar(Composite parent) {
    m_uiToolbar = new RwtScoutToolbar();
    m_uiToolbar.createUiField(parent, getScoutObject(), getUiEnvironment());
    return m_uiToolbar.getUiContainer();
  }

  /**
   * There is no action bar created as default. The method is intended for being used by subclasses.
   */
  protected Control createActionBar(Composite parent) {
    return null;
  }

  protected Control createViewsArea(Composite parent) {
    m_viewArea = new ViewArea(parent);
    m_viewArea.getLayout().addLayoutListener(new ILayoutListener() {
      @Override
      public void handleCompositeLayouted() {
        int xOffset = -1;
        Sash sash = m_viewArea.getSash(SashKey.VERTICAL_RIGHT);
        if (sash.getVisible()) {
          Rectangle sashBounds = sash.getBounds();
          xOffset = sashBounds.x + sashBounds.width;
        }
        getUiToolbar().handleRightViewPositionChanged(xOffset);
      }
    });
    return m_viewArea;
  }

  @Override
  public IRwtStandaloneEnvironment getUiEnvironment() {
    return (IRwtStandaloneEnvironment) super.getUiEnvironment();
  }

  @Override
  public IRwtScoutPart addForm(IForm form) {
    RwtScoutViewStack stack = m_viewArea.getStackForForm(form);
    IRwtScoutPart rwtForm = stack.addForm(form);
    updateLayout();
    return rwtForm;
  }

  @Override
  public void updateLayout() {
    m_viewArea.layout();
  }

  @Override
  public RwtScoutToolbar getUiToolbar() {
    return m_uiToolbar;
  }

  @Override
  public IRwtScoutActionBar getUiActionBar() {
    return null;
  }

  @Override
  public ViewArea getViewArea() {
    return m_viewArea;
  }

}
