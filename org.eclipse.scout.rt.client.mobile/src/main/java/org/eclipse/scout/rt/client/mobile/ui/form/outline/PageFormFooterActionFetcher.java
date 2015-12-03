/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.mobile.ui.form.outline;

import java.util.List;

import org.eclipse.scout.rt.client.mobile.ui.action.ActionButtonBarUtility;
import org.eclipse.scout.rt.client.mobile.ui.form.AbstractMobileAction;
import org.eclipse.scout.rt.client.mobile.ui.form.FormFooterActionFetcher;
import org.eclipse.scout.rt.client.mobile.ui.form.IMobileAction;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

/**
 * Additionally fetches the actions of the current page and places them on the left side.
 */
public class PageFormFooterActionFetcher extends FormFooterActionFetcher {

  public PageFormFooterActionFetcher(PageForm form) {
    super(form);
  }

  @Override
  public PageForm getForm() {
    return (PageForm) super.getForm();
  }

  @Override
  public List<IMenu> fetch() {
    List<IMenu> footerActions = super.fetch();
    IPage<?> page = getForm().getPage();

    List<IMenu> nodeActions = ActionButtonBarUtility.fetchPageActions(page);
    for (IMenu action : nodeActions) {
      AbstractMobileAction.setHorizontalAlignment(action, IMobileAction.HORIZONTAL_ALIGNMENT_LEFT);
    }
    footerActions.addAll(0, nodeActions);

    return footerActions;
  }

}
