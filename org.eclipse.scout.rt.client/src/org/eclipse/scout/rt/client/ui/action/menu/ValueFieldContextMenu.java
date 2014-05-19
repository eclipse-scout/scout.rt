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
package org.eclipse.scout.rt.client.ui.action.menu;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

/**
 *
 */
public class ValueFieldContextMenu extends FormFieldContextMenu<IValueField> {

  /**
   * @param owner
   */
  public ValueFieldContextMenu(IValueField owner) {
    super(owner);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    handleOwnerValueChanged();
  }

  @Override
  protected void afterChildMenusAdd(List<? extends IMenu> newChildMenus) {
    super.afterChildMenusAdd(newChildMenus);
    handleOwnerEnabledChanged();
  }

  @Override
  protected void afterChildMenusRemove(List<? extends IMenu> childMenusToRemove) {
    super.afterChildMenusRemove(childMenusToRemove);
    handleOwnerEnabledChanged();
  }

  protected void handleOwnerValueChanged() {
    if (getOwner() != null) {
      final Object ownerValue = getOwner().getValue();
      acceptVisitor(new IActionVisitor() {
        @Override
        public int visit(IAction action) {
          if (action instanceof IMenu) {
            IMenu menu = (IMenu) action;
            try {
              menu.handleOwnerValueChanged(ownerValue);
            }
            catch (ProcessingException ex) {
              SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
            }
          }
          return CONTINUE;
        }
      });
    }
  }

  @Override
  protected void handleOwnerPropertyChanged(PropertyChangeEvent evt) {
    super.handleOwnerPropertyChanged(evt);
    if (IValueField.PROP_VALUE.equals(evt.getPropertyName())) {
      handleOwnerValueChanged();
    }
  }

}
