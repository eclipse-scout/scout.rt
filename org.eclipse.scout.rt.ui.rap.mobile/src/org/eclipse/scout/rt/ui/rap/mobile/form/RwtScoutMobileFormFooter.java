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

import java.util.List;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.mobile.ui.action.ActionButtonBarUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
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

  private List<IMenu> m_leftBarActions;

  @Override
  protected void initializeUi(Composite parent) {
    setMenuOpeningDirection(SWT.UP);
    if (getScoutObject().getRootGroupBox().getCustomProcessButtonCount() > 0) {
      m_leftBarActions = createLeftBarActions();
    }

    super.initializeUi(parent);

    getUiContainer().setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_FORM_FOOTER);
  }

  @Override
  protected void collectMenusForLeftButtonBar(final List<IMenu> menuList) {
    if (m_leftBarActions != null && m_leftBarActions.size() > 0) {
      menuList.addAll(m_leftBarActions);
    }
  }

  protected List<IMenu> convertCustomProcessButtons() {
    IButton[] customProcessButtons = getScoutObject().getRootGroupBox().getCustomProcessButtons();
    if (customProcessButtons == null || customProcessButtons.length == 0) {
      return null;
    }

    return ActionButtonBarUtility.convertButtonsToActions(getScoutObject().getRootGroupBox().getCustomProcessButtons());
  }

  protected List<IMenu> createLeftBarActions() {
    return convertCustomProcessButtons();
  }

}
