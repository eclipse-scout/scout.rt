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
package org.eclipse.scout.rt.ui.rap.window.desktop.nonmodalFormBar;

import java.util.HashMap;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.services.common.patchedclass.IPatchedClassService;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

public class RwtScoutFormButtonBar extends RwtScoutComposite<IDesktop> {
  private static final String VARIANT_FORMBAR_CONTAINER = "formbarContainer";
  private static final String VARIANT_FORM_BUTTON_ACTIVE = "formButton-active";
  private static final String VARIANT_FORM_BUTTON = "formButton";

  private HashMap<IForm, IRwtScoutFormButton> m_formTabItems;
  private Composite m_buttonBar;

  public RwtScoutFormButtonBar() {
    m_formTabItems = new HashMap<IForm, IRwtScoutFormButton>();
  }

  public void addFormButton(final IForm form) {
    IAction formButton = new AbstractAction() {

      @Override
      protected void execInitAction() throws ProcessingException {
        setText(form.getTitle());
      }

      @Override
      protected void execAction() throws ProcessingException {
        form.activate();
        form.toFront();
      }
    };
    form.addFormListener(new FormListener() {

      @Override
      public void formChanged(final FormEvent e) throws ProcessingException {
        getUiEnvironment().invokeUiLater(new Runnable() {

          @Override
          public void run() {
            IRwtScoutFormButton uiButton = m_formTabItems.get(e.getForm());
            if (uiButton != null && !uiButton.isUiDisposed()) {
              if (e.getType() == FormEvent.TYPE_TO_FRONT) {
                uiButton.makeButtonActive();
              }
              else if (e.getType() == FormEvent.TYPE_TO_BACK) {
                uiButton.makeButtonInactive();
              }
            }
          }
        });
      }
    });
    IRwtScoutFormButtonForPatch uiButton = SERVICES.getService(IPatchedClassService.class).createRwtScoutFormButton(true, false, VARIANT_FORM_BUTTON, VARIANT_FORM_BUTTON_ACTIVE);
    uiButton.createUiField(m_buttonBar, formButton, getUiEnvironment());
    m_formTabItems.put(form, uiButton);
  }

  public void removeFormButton(IForm form) {
    IRwtScoutFormButton uiButton = m_formTabItems.remove(form);
    if (uiButton != null && uiButton.isCreated() && !uiButton.isUiDisposed()) {
      uiButton.dispose();
      m_buttonBar.layout(true, true);
    }
  }

  public int getFormButtonBarCount() {
    return m_formTabItems.size();
  }

  @Override
  protected void initializeUi(Composite parent) {
    m_buttonBar = getUiEnvironment().getFormToolkit().createComposite(parent);
    m_buttonBar.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_FORMBAR_CONTAINER);
    RowLayout layout = new RowLayout(SWT.HORIZONTAL);
    layout.marginBottom = 0;
    layout.marginTop = 0;

    m_buttonBar.setLayout(layout);

    setUiContainer(m_buttonBar);
  }
}
