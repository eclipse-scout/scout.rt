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

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.util.BrowserInfo;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.window.IRwtScoutPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>RwtScoutViewStack</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.7.0 June 2011
 */
public class RwtScoutViewStack extends Composite implements IRwtScoutViewStack {
  private static final long serialVersionUID = 1L;

  private static final String VARIANT_VIEW_PART = "viewPart";
  private static final String VARIANT_VIEW_PART_NO_RADIUS = "viewPartNoRadius";
  private static final String VARIANT_FORM = "form";
  private static final String VARIANT_VIEW_TAB_AREA = "formTabArea";

  private IRwtEnvironment m_uiEnvironment;
  private HashMap<IForm, RwtScoutDesktopForm> m_openForms;
  ArrayList<RwtScoutDesktopForm> m_formStack;

  private Composite m_tabBar;

  private Composite m_container;

  public RwtScoutViewStack(Composite parent, IRwtEnvironment uiEnvironment) {
    super(parent, SWT.NONE);
    m_uiEnvironment = uiEnvironment;
    m_openForms = new HashMap<IForm, RwtScoutDesktopForm>();
    m_formStack = new ArrayList<RwtScoutDesktopForm>(3);
    getUiEnvironment().getScoutDesktop().addDesktopListener(new P_DesktopListner());
    BrowserInfo info = RwtUtility.getBrowserInfo();
    if (info != null && info.isMozillaFirefox()) {
      setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_VIEW_PART);
    }
    else {
      setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_VIEW_PART_NO_RADIUS);
    }
    createContent(this);
  }

  protected void createContent(Composite parent) {
    m_tabBar = getUiEnvironment().getFormToolkit().createComposite(parent);
    m_tabBar.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_VIEW_TAB_AREA);
    m_container = getUiEnvironment().getFormToolkit().createComposite(parent);
    m_container.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_FORM);

    // layout
    GridLayout layout = new GridLayout(1, true);
    layout.horizontalSpacing = 0;
    layout.marginHeight = 0;
    layout.marginTop = 0;
    layout.marginWidth = 0;
    layout.verticalSpacing = 0;
    parent.setLayout(layout);

    GridData tabBarData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    tabBarData.exclude = false;
    m_tabBar.setLayoutData(tabBarData);

    RowLayout tabBarLayout = new RowLayout();
    tabBarLayout.marginBottom = 0;
    tabBarLayout.marginLeft = 0;
    tabBarLayout.marginTop = 0;
    m_tabBar.setLayout(tabBarLayout);

    GridData containerData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
    m_container.setLayoutData(containerData);

    StackLayout containerLayout = new StackLayout();
    m_container.setLayout(containerLayout);
  }

  @Override
  public IRwtEnvironment getUiEnvironment() {
    return m_uiEnvironment;
  }

  @Override
  public IRwtScoutPart addForm(IForm form) {
    RwtScoutDesktopForm ui = new RwtScoutDesktopForm();
    ViewStackTabButton button = new ViewStackTabButton(m_tabBar);
    button.setLayoutData(new RowData(SWT.DEFAULT, 22));
    button.addViewTabListener(new P_ViewTabSelectionListener(ui));
    ui.createPart(this, m_container, button, form, getUiEnvironment());
    m_formStack.add(0, ui);
    m_openForms.put(form, ui);
    setPartVisibleImpl(form);
    return ui;
  }

  @Override
  public boolean isPartVisible(IRwtScoutPart part) {
    //resolve
    part = m_openForms.get(part.getScoutObject());
    if (part == null) {
      return false;
    }
    StackLayout stackLayout = (StackLayout) m_container.getLayout();
    return RwtUtility.isAncestorOf(stackLayout.topControl, part.getUiForm());
  }

  @Override
  public void setPartVisible(IRwtScoutPart part) {
    //resolve
    part = m_openForms.get(part.getScoutObject());
    if (part == null) {
      return;
    }
    setPartVisibleImpl(part.getScoutObject());
  }

  private void removeForm(IForm form) {
    RwtScoutDesktopForm uiForm = m_openForms.remove(form);
    if (uiForm != null) {
      m_formStack.remove(uiForm);
      IForm topForm = null;
      if (m_formStack.size() > 0) {
        topForm = m_formStack.get(0).getScoutObject();
      }
      setPartVisibleImpl(topForm);
    }
  }

  private void setPartVisibleImpl(IForm form) {
    RwtScoutDesktopForm uiForm = m_openForms.get(form);
    if (uiForm != null) {
      StackLayout stackLayout = (StackLayout) m_container.getLayout();
      stackLayout.topControl = uiForm.getUiForm();
      if (m_formStack.remove(uiForm)) {
        GridData tabBarData = (GridData) m_tabBar.getLayoutData();
        if (m_formStack.isEmpty()) {
          m_tabBar.setVisible(false);
          tabBarData.exclude = true;
        }
        else {
          m_tabBar.setVisible(true);
          tabBarData.exclude = false;
          for (RwtScoutDesktopForm f : m_formStack) {
            f.getTabButton().setActive(false);
          }
          uiForm.getTabButton().setActive(true);
        }
        m_formStack.add(0, uiForm);
      }
    }
    getParent().layout(true, true);
  }

  @Override
  public boolean getVisible() {
    return m_container.getChildren().length > 0;
  }

  private void handleScoutDesktopEvent(DesktopEvent e) {
    switch (e.getType()) {
      case DesktopEvent.TYPE_FORM_ENSURE_VISIBLE:
        setPartVisibleImpl(e.getForm());
        break;
      case DesktopEvent.TYPE_FORM_REMOVED:
        removeForm(e.getForm());
        break;
    }
  }

  private class P_ViewTabSelectionListener implements IViewTabSelectionListener {
    private final RwtScoutDesktopForm m_form;

    public P_ViewTabSelectionListener(RwtScoutDesktopForm form) {
      m_form = form;
    }

    @Override
    public void handleEvent(ViewTabSelectionEvent event) {
      if (event.getEventType() == ViewTabSelectionEvent.TYPE_VIEW_TAB_SELECTION) {
        Runnable job = new Runnable() {
          @Override
          public void run() {
            m_form.getScoutObject().getUIFacade().fireFormActivatedFromUI();
          }
        };
        getUiEnvironment().invokeScoutLater(job, 0);
        setPartVisibleImpl(m_form.getScoutObject());
      }
      else if (event.getEventType() == ViewTabSelectionEvent.TYPE_VIEW_TAB_CLOSE_SELECTION) {
        Runnable job = new Runnable() {
          @Override
          public void run() {
            m_form.getScoutObject().getUIFacade().fireFormClosingFromUI();
          }
        };
        getUiEnvironment().invokeScoutLater(job, 0);
      }

    }
  } // end class P_ViewTabSelectionListener

  private class P_DesktopListner implements DesktopListener {
    @Override
    public void desktopChanged(final DesktopEvent e) {
      if (getUiEnvironment().getDisplay() != null && !getUiEnvironment().getDisplay().isDisposed()) {
        Runnable t = new Runnable() {
          @Override
          public void run() {
            handleScoutDesktopEvent(e);
          }
        };
        getUiEnvironment().invokeUiLater(t);
      }
    }
  }

}
