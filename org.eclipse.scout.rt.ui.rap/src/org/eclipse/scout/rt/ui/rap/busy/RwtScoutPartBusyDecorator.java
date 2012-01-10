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
package org.eclipse.scout.rt.ui.rap.busy;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.core.window.IRwtScoutPart;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.widgets.Form;

/**
 * Decorates a {@link IRwtScoutPart}'s {@link IRwtScoutPart#getRwtForm()} header section with a progress bar and a
 * button
 */
public class RwtScoutPartBusyDecorator {
  private static final String ATTACH_MARKER_DATA = RwtScoutPartBusyDecorator.class.getName() + "#marker";

  private final IRwtScoutPart m_part;
  private final boolean m_showCancelButton;
  private boolean m_attached;
  private final IRwtEnvironment m_env;
  private Control m_oldFocus;
  private IContributionItem m_cancelAction;

  public RwtScoutPartBusyDecorator(IRwtScoutPart part, boolean showCancelButton, IRwtEnvironment env) {
    m_part = part;
    m_showCancelButton = showCancelButton;
    m_env = env;
  }

  public void attach(final IProgressMonitor monitor) {
    Form rwtForm = m_part.getUiForm();
    if (rwtForm == null || rwtForm.isDisposed()) {
      return;
    }
    if (rwtForm.getData(ATTACH_MARKER_DATA) != null) {
      return;
    }
    m_attached = true;
    rwtForm.setData(ATTACH_MARKER_DATA, true);
    //backup focus
    Control focusControl = Display.getCurrent().getFocusControl();
    if (focusControl != null) {
      if (RwtUtility.isAncestorOf(rwtForm, focusControl)) {
        m_oldFocus = focusControl;
      }
    }
    rwtForm.getBody().setEnabled(false);
    //show cancel button
    if (m_showCancelButton) {
      if (m_cancelAction != null) {
        m_cancelAction.dispose();
      }
      m_cancelAction = new ContributionItem() {
        private static final long serialVersionUID = 1L;
        private ToolItem m_item;

        @Override
        public void fill(ToolBar parent, int index) {
          if (m_item == null) {
            m_item = new ToolItem(parent, SWT.NONE, index);
            m_item.setImage(m_env.getIcon("progress_stop"));
            //[swt cannot show text right of image] m_item.setText(RwtUtility.getNlsText(Display.getCurrent(), "Cancel"));
            m_item.addSelectionListener(new SelectionAdapter() {
              private static final long serialVersionUID = 1L;

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
      rwtForm.getToolBarManager().add(m_cancelAction);
      rwtForm.getToolBarManager().update(true);
    }
    //there is a central busy spinner/ rwtForm.setBusy(true);
    rwtForm.layout(true);
  }

  public void detach() {
    if (!m_attached) {
      return;
    }
    m_attached = false;
    Form rwtForm = m_part.getUiForm();
    if (rwtForm == null || rwtForm.isDisposed()) {
      return;
    }
    rwtForm.setData(ATTACH_MARKER_DATA, null);
    rwtForm.setBusy(false);
    //hide cancel button
    if (m_cancelAction != null) {
      rwtForm.getToolBarManager().remove(m_cancelAction);
      m_cancelAction.dispose();
      m_cancelAction = null;
      rwtForm.getToolBarManager().update(true);
    }
    rwtForm.getBody().setEnabled(true);
    rwtForm.layout(true);
    //restore focus
    if (m_oldFocus != null && !m_oldFocus.isDisposed()) {
      if (m_part.isActive()) {
        m_oldFocus.forceFocus();
        m_oldFocus = null;
      }
    }
  }

}
