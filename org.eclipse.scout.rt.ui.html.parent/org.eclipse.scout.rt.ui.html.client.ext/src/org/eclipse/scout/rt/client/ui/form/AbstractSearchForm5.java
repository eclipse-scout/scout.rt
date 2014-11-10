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
package org.eclipse.scout.rt.client.ui.form;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;

//COPIED from AbstractSearchForm because extending from AbstractForm5 was necessary
public abstract class AbstractSearchForm5 extends AbstractForm5 implements ISearchForm {

  public AbstractSearchForm5() throws ProcessingException {
    super();
  }

  @Override
  public void initForm() throws ProcessingException {
    // form
    initFormInternal();
    // fields
    FormUtility.initFormFields(this);
    // mark strategy
    FormUtility.setTabBoxMarkStrategy(this, ITabBox.MARK_STRATEGY_SAVE_NEEDED);
    // custom
    execInitForm();
  }

  @Override
  protected boolean getConfiguredMinimizeEnabled() {
    return true;
  }

  @Override
  protected boolean getConfiguredAskIfNeedSave() {
    return false;
  }

  private Class<? extends IFormHandler> getConfiguredSearchHandler() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClass(dca, IFormHandler.class);
  }

  @Override
  public abstract void startSearch() throws ProcessingException;

}
