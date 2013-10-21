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

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.IRwtStandaloneEnvironment;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.scout.rt.ui.rap.window.IRwtScoutPart;
import org.eclipse.scout.rt.ui.rap.window.desktop.toolbar.RwtScoutToolbar;
import org.eclipse.scout.rt.ui.rap.window.desktop.viewarea.ILayoutListener;
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

    setTitleFromScout(getScoutObject().getTitle());
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
      viewsArea.setData(RWT.CUSTOM_VARIANT, getViewsAreaVariant());

      initLayout(desktopComposite, toolbar, viewsArea);

      setUiContainer(desktopComposite);
    }
    catch (Throwable t) {
      LOG.error("Exception occured while creating ui desktop.", t);
    }
  }

  protected String getViewsAreaVariant() {
    return VARIANT_VIEWS_AREA;
  }

  protected void initLayout(Composite container, Control toolbar, Control viewsArea) {
    GridLayout layout = RwtLayoutUtility.createGridLayoutNoSpacing(1, true);
    container.setLayout(layout);

    if (toolbar != null) {
      GridData toolbarData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
      toolbar.setLayoutData(toolbarData);
    }

    if (viewsArea != null) {
      GridData viewsAreaData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
      viewsArea.setLayoutData(viewsAreaData);
    }

  }

  protected Control createToolBar(Composite parent) {
    m_uiToolbar = new RwtScoutToolbar();
    m_uiToolbar.createUiField(parent, getScoutObject(), getUiEnvironment());
    return m_uiToolbar.getUiContainer();
  }

  protected Control createViewsArea(Composite parent) {
    m_viewArea = createViewArea(parent);
    m_viewArea.getLayout().addLayoutListener(new ILayoutListener() {
      @Override
      public void handleCompositeLayouted() {
        int xOffset = -1;
        Sash sash = m_viewArea.getSash(SashKey.VERTICAL_RIGHT);
        if (sash != null && sash.getVisible()) {
          Rectangle sashBounds = sash.getBounds();
          xOffset = sashBounds.x + sashBounds.width;
        }
        if (getUiToolbar() != null) {
          getUiToolbar().handleRightViewPositionChanged(xOffset);
        }
      }
    });
    return m_viewArea;
  }

  protected ViewArea createViewArea(Composite parent) {
    return new ViewArea(parent);
  }

  @Override
  public IRwtStandaloneEnvironment getUiEnvironment() {
    return (IRwtStandaloneEnvironment) super.getUiEnvironment();
  }

  @Override
  public IRwtScoutPart addForm(IForm form) {
    IRwtScoutViewStack stack = getViewArea().getStackForForm(form);
    if (stack == null) {
      LOG.error("No view stack for the form '" + form.getFormId() + "' with the display view id '" + form.getDisplayViewId() + "' found. Please check your view configuration. See class ViewArea for details.");
      return null;
    }

    IRwtScoutPart rwtForm = stack.addForm(form);

    m_viewArea.updateSashPositionForViewStack(stack);
    updateLayout();

    return rwtForm;
  }

  @Override
  public void updateLayout() {
    getViewArea().layout();
  }

  @Override
  public IRwtScoutToolbar getUiToolbar() {
    return m_uiToolbar;
  }

  @Override
  public IViewArea getViewArea() {
    return m_viewArea;
  }

  protected void setTitleFromScout(String title) {
    if (title == null) {
      title = "";
    }
    JavaScriptExecutor executor = RWT.getClient().getService(JavaScriptExecutor.class);
    if (executor != null) {
      executor.execute("window.document.title=\"" + title + "\"");
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (IDesktop.PROP_TITLE.equals(name)) {
      setTitleFromScout((String) newValue);
    }
  }
}
