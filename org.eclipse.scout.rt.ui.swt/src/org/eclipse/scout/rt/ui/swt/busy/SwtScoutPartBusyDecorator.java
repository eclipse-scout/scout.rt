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
package org.eclipse.scout.rt.ui.swt.busy;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.scout.rt.ui.swt.Activator;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.widgets.Form;

/**
 * Decorates a {@link ISwtScoutPart}'s {@link ISwtScoutPart#getSwtForm()} header section with a progress bar and a
 * button
 */
public class SwtScoutPartBusyDecorator {

  private final ISwtScoutPart m_part;
  private Control m_oldFocus;
  private IContributionItem m_cancelAction;

  public SwtScoutPartBusyDecorator(ISwtScoutPart part) {
    m_part = part;
  }

  public IProgressMonitor attach() {
    Form swtForm = m_part.getSwtForm();
    if (swtForm == null || swtForm.isDisposed()) {
      return new NullProgressMonitor();
    }
    //backup focus
    Control focusControl = Display.getCurrent().getFocusControl();
    if (focusControl != null) {
      if (SwtUtility.isAncestorOf(swtForm, focusControl)) {
        m_oldFocus = focusControl;
      }
    }
    swtForm.getBody().setEnabled(false);
    //show cancel button
    if (m_cancelAction != null) {
      m_cancelAction.dispose();
    }
    final IProgressMonitor monitor = new NullProgressMonitor();
    m_cancelAction = new ContributionItem() {
      private ToolItem m_item;

      @Override
      public void fill(ToolBar parent, int index) {
        if (m_item == null) {
          m_item = new ToolItem(parent, SWT.NONE, index);
          m_item.setImage(Activator.getIcon("progress_stop"));
          m_item.setText(SwtUtility.getNlsText(Display.getCurrent(), "Cancel"));
          m_item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              monitor.setCanceled(true);
              ((ToolItem) e.getSource()).setEnabled(false);
            }
          });
        }
      }

      @Override
      public void dispose() {
        if (m_item != null) {
          m_item.dispose();
          m_item = null;
        }
      }
    };
    swtForm.getToolBarManager().add(m_cancelAction);
    swtForm.getToolBarManager().update(true);
    swtForm.setBusy(true);
    swtForm.layout(true);
    return monitor;
  }

  public void detach() {
    Form swtForm = m_part.getSwtForm();
    if (swtForm == null || swtForm.isDisposed()) {
      return;
    }
    swtForm.setBusy(false);
    //hide cancel button
    if (m_cancelAction != null) {
      swtForm.getToolBarManager().remove(m_cancelAction);
      m_cancelAction.dispose();
      m_cancelAction = null;
      swtForm.getToolBarManager().update(true);
    }
    swtForm.getBody().setEnabled(true);
    swtForm.layout(true);
    //restore focus
    if (m_oldFocus != null && !m_oldFocus.isDisposed()) {
      if (m_part.isActive()) {
        m_oldFocus.forceFocus();
        m_oldFocus = null;
      }
    }
  }

}
