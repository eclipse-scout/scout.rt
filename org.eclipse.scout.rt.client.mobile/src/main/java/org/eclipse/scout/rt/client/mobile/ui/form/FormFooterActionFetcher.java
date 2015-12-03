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
package org.eclipse.scout.rt.client.mobile.ui.form;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * Converts the custom process buttons of the main box to actions.
 * <p>
 * The custom process buttons will be placed on the left side.
 */
public class FormFooterActionFetcher extends AbstractFormActionFetcher {

  public FormFooterActionFetcher(IForm form) {
    super(form);
  }

  @Override
  public List<IMenu> fetch() {
    List<IMenu> formActions = new LinkedList<IMenu>();
    if (getForm().getRootGroupBox().getCustomProcessButtonCount() > 0) {
      List<IMobileAction> leftActions = createLeftFooterActions();
      for (IMobileAction action : leftActions) {
        action.setHorizontalAlignment(IMobileAction.HORIZONTAL_ALIGNMENT_LEFT);
      }
      formActions.addAll(leftActions);
    }
    return formActions;
  }

  protected List<IMobileAction> createLeftFooterActions() {
    return convertCustomProcessButtons();
  }

}
