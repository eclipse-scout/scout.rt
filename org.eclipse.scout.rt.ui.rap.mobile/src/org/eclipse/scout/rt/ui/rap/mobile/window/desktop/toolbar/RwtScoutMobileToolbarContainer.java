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
package org.eclipse.scout.rt.ui.rap.mobile.window.desktop.toolbar;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.rt.client.mobile.services.IMobileNavigationService;
import org.eclipse.scout.rt.client.mobile.ui.forms.OutlineChooserForm;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutToolbar;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @since 3.8.0
 */
public class RwtScoutMobileToolbarContainer extends RwtScoutComposite<IDesktop> implements IRwtScoutToolbar<IDesktop> {
  public static final String VARIANT_TOOLBAR_CONTAINER = "mobileToolbarContainer";
  private P_DesktopListener m_desktopListener;
  private IForm m_currentVisibleForm;
  private IRwtScoutToolbar m_uiToolbar;

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    container.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TOOLBAR_CONTAINER);
    setUiContainer(container);

    createToolbar(container);

    initLayout();
  }

  private void initLayout() {
    getUiContainer().setLayout(new FillLayout());
  }

  private IForm getCurrentFormOnDesktop() {
    return SERVICES.getService(IMobileNavigationService.class).getCurrentForm();
  }

  @Override
  protected void attachScout() {
    super.attachScout();

    if (m_desktopListener == null) {
      m_desktopListener = new P_DesktopListener();
    }
    getScoutObject().addDesktopListener(m_desktopListener);
  }

  @Override
  protected void detachScout() {
    if (m_desktopListener != null) {
      getScoutObject().removeDesktopListener(m_desktopListener);
      m_desktopListener = null;
    }

    super.detachScout();
  }

  protected Control createToolbar(Composite parent) {
    if (showFormToolButtonBar()) {
      RwtScoutMobileFormToolbar formToolbar = new RwtScoutMobileFormToolbar();
      formToolbar.createUiField(parent, m_currentVisibleForm, getUiEnvironment());

      m_uiToolbar = formToolbar;
    }
    else {
      RwtScoutMobileOutlineToolbar outlineToolbar = new RwtScoutMobileOutlineToolbar();
      outlineToolbar.createUiField(parent, getScoutObject(), getUiEnvironment());

      m_uiToolbar = outlineToolbar;
    }

    return m_uiToolbar.getUiContainer();
  }

  public IRwtScoutToolbar getUiToolbar() {
    return m_uiToolbar;
  }

  private boolean showFormToolButtonBar() {
    if (m_currentVisibleForm == null) {
      return false;
    }

    if (m_currentVisibleForm instanceof OutlineChooserForm || m_currentVisibleForm instanceof IOutlineTableForm) {
      return false;
    }

    return true;
  }

  private void rebuildToolButtonBarFromScout() {
    getUiEnvironment().invokeUiLater(new Runnable() {

      @Override
      public void run() {
        if (getUiToolbar() != null) {
          getUiToolbar().dispose();
        }

        createToolbar(getUiContainer());
        initLayout();

        getUiContainer().layout(true, true);
      }

    });
  }

  @Override
  public void handleRightViewPositionChanged(int rightViewX) {
    // nothing to do on mobile because there is no view on the right side.
  }

  private class P_DesktopListener implements DesktopListener {
    @Override
    public void desktopChanged(final DesktopEvent e) {
      switch (e.getType()) {
        case DesktopEvent.TYPE_FORM_ADDED: {
          m_currentVisibleForm = e.getForm();

          rebuildToolButtonBarFromScout();
          break;
        }
        case DesktopEvent.TYPE_FORM_REMOVED: {
          if (getCurrentFormOnDesktop() == null) {
            m_currentVisibleForm = null;

            rebuildToolButtonBarFromScout();
          }
          break;
        }
      }

    }
  }
}
