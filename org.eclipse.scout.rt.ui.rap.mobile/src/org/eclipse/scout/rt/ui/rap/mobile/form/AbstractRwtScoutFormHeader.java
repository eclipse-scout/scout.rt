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

import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.mobile.ui.form.AbstractMobileForm;
import org.eclipse.scout.rt.client.mobile.ui.form.FormHeaderActionFetcher;
import org.eclipse.scout.rt.client.mobile.ui.form.IActionFetcher;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.mobile.action.AbstractRwtScoutActionBar;
import org.eclipse.scout.rt.ui.rap.mobile.action.ActionButtonBar;
import org.eclipse.scout.rt.ui.rap.window.desktop.IRwtScoutFormHeader;

/**
 * @since 3.9.0
 */
public class AbstractRwtScoutFormHeader extends AbstractRwtScoutActionBar<IForm> implements IRwtScoutFormHeader {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractRwtScoutFormHeader.class);
  private static final String VARIANT_FORM_HEADER = "mobileFormHeader";

  @Override
  protected String getActionBarContainerVariant() {
    return VARIANT_FORM_HEADER;
  }

  @Override
  public boolean isAlwaysVisible() {
    return true;
  }

  @Override
  protected void attachScout() {
    super.attachScout();

    setTitle(getScoutObject().getTitle());
  }

  @Override
  protected void adaptLeftButtonBar(ActionButtonBar buttonBar) {
    buttonBar.setPilingEnabled(false);
  }

  @Override
  protected void adaptRightButtonBar(ActionButtonBar buttonBar) {
    buttonBar.setMinNumberOfAlwaysVisibleButtons(1);
    buttonBar.setMaxNumberOfAlwaysVisibleButtons(1);
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);

    if (name.equals(IForm.PROP_TITLE)) {
      setTitle((String) newValue);
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

        IActionFetcher actionFetcher = AbstractMobileForm.getHeaderActionFetcher(getScoutObject());
        if (actionFetcher == null) {
          actionFetcher = new FormHeaderActionFetcher(getScoutObject());
        }
        List<IMenu> actions = actionFetcher.fetch();
        if (actions != null) {
          actionList.addAll(actions);
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
