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
package org.eclipse.scout.rt.ui.rap.mobile.form;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.mobile.ui.form.AbstractMobileAction;
import org.eclipse.scout.rt.client.mobile.ui.form.AbstractMobileForm;
import org.eclipse.scout.rt.client.mobile.ui.form.FormFooterActionFetcher;
import org.eclipse.scout.rt.client.mobile.ui.form.IActionFetcher;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.mobile.action.AbstractRwtScoutActionBar;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutFormFooter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.9.0
 */
public class RwtScoutMobileFormFooter extends AbstractRwtScoutActionBar<IForm> implements IRwtScoutFormFooter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutMobileFormFooter.class);
  private static final String VARIANT_FORM_FOOTER = "mobileFormFooter";

  private List<IMenu> m_actions;

  @Override
  protected void initializeUi(Composite parent) {
    setMenuOpeningDirection(SWT.UP);
    m_actions = fetchActions();

    super.initializeUi(parent);
  }

  @Override
  protected String getActionBarContainerVariant() {
    return VARIANT_FORM_FOOTER;
  }

  @Override
  protected void collectMenusForLeftButtonBar(final List<IMenu> menuList) {
    if (m_actions == null) {
      return;

    }

    for (IMenu action : m_actions) {
      if (AbstractMobileAction.getHorizontalAlignment(action) < 0) {
        menuList.add(action);
      }
    }
  }

  @Override
  protected void collectMenusForRightButtonBar(List<IMenu> menuList) {
    if (m_actions == null) {
      return;

    }

    for (IMenu action : m_actions) {
      if (AbstractMobileAction.getHorizontalAlignment(action) > 0) {
        menuList.add(action);
      }
    }
  }

  public List<IMenu> fetchActions() {
    final List<IMenu> actionList = new LinkedList<IMenu>();
    Runnable t = new Runnable() {
      @Override
      public void run() {
        //Don't fetch actions if the form has already been removed from the desktop.
        if (!getScoutObject().isShowing()) {
          return;
        }

        IActionFetcher actionFetcher = AbstractMobileForm.getFooterActionFetcher(getScoutObject());
        if (actionFetcher == null) {
          actionFetcher = new FormFooterActionFetcher(getScoutObject());
        }
        try {
          List<IMenu> actions = actionFetcher.fetch();
          if (actions != null) {
            actionList.addAll(actions);
          }
        }
        catch (ProcessingException e) {
          LOG.error("could not initialze actions.", e);
        }
      }
    };

    JobEx job = getUiEnvironment().invokeScoutLater(t, 5000);
    try {
      job.join(2000);
    }
    catch (InterruptedException ex) {
      LOG.warn("Exception occured while collecting menus.", ex);
    }

    return actionList;
  }
}
