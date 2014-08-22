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
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.internal.TablePageTreeMenuWrapper;
import org.eclipse.scout.rt.client.ui.form.FormMenuType;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IForm5;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.AbstractExtensiblePageWithNodes;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.ui.menu.IMenu5;
import org.eclipse.scout.rt.shared.ui.menu.MenuWrapper;
import org.eclipse.scout.service.SERVICES;

public class AbstractPageWithNodes5 extends AbstractExtensiblePageWithNodes {

  public AbstractPageWithNodes5() {
    super();
  }

  public AbstractPageWithNodes5(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * @deprecated Will be removed in the 6.0 Release.
   *             Use {@link #AbstractExtensiblePageWithNodes()} in combination with getter and setter (page variable)
   *             instead.
   */
  @Deprecated
  @SuppressWarnings("deprecation")
  public AbstractPageWithNodes5(org.eclipse.scout.rt.shared.ContextMap contextMap) {
    super(contextMap);
  }

  public AbstractPageWithNodes5(String userPreferenceContext) {
    super(userPreferenceContext);
  }

  public AbstractPageWithNodes5(boolean callInitializer, String userPreferenceContext) {
    super(callInitializer, userPreferenceContext);
  }

  @Override
  public void pageActivatedNotify() {
    super.pageActivatedNotify();
    try {
      ensureDetailFormCreated();
      ensureDetailFormStarted();
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  @Override
  protected void execDisposePage() throws ProcessingException {
    if (getDetailForm() != null) {
      getDetailForm().doClose();
      setDetailForm(null);
    }
  }

  protected IForm execCreateDetailForm() throws ProcessingException {
    return null;
  }

  protected void execStartDetailForm(IForm form) throws ProcessingException {
  }

  protected void ensureDetailFormCreated() throws ProcessingException {
    if (getDetailForm() != null) {
      return;
    }
    IForm form = execCreateDetailForm();
    if (form != null) {
      if (form instanceof IForm5) {
        IForm5 form5 = (IForm5) form;
        List<IMenu> menus = form5.getContextMenu().getChildActions();
        adaptDetailFormMenus(menus);
        if (!CollectionUtility.equalsCollection(menus, ((IForm5) form).getContextMenu().getChildActions())) {
          form5.getContextMenu().setChildActions(menus);
        }
      }
    }
    setDetailForm(form);
  }

  protected void ensureDetailFormStarted() throws ProcessingException {
    if (getDetailForm() == null || getDetailForm().isFormOpen()) {
      return;
    }
    execStartDetailForm(getDetailForm());
  }

  protected void adaptDetailFormMenus(List<IMenu> menus) {
    List<IMenu> copy = new LinkedList<IMenu>(menus);
    //Remove system menus (ok cancel)
    for (IMenu menu : copy) {
      if (menu instanceof IMenu5) {
        if (((IMenu5) menu).getSystemType() != IMenu5.SYSTEM_TYPE_NONE) {
          menus.remove(menu);
        }
      }
    }

    //Add page menus to the form
    for (IMenu menu : getOutline().getContextMenu().getChildActions()) {
      //FIXME CGU improve this
      if (menu instanceof TablePageTreeMenuWrapper && ((TablePageTreeMenuWrapper) menu).getWrappedMenu().getClass().getSimpleName().contains("DrillDownMenu")) {
        continue;
      }
      Set<IMenuType> types = new HashSet<IMenuType>();
      for (IMenuType type : menu.getMenuTypes()) {
        if (type instanceof FormMenuType) {
          types.add(type);
        }
      }

      if (types.isEmpty()) {
        types.add(FormMenuType.Regular);
      }
      MenuWrapper menuWrapper = new MenuWrapper(menu, types);
      menus.add(menuWrapper);
    }
  }
}
