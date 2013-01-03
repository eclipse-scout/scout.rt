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

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.FormUtility;
import org.eclipse.scout.rt.client.ui.form.IFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;

/**
 * Search form for the outline must contain a SearchHandler<br>
 * The method startSearch() starts the first inner class of type IFormHandler
 * that is found
 */
public abstract class AbstractSearchForm extends AbstractForm implements ISearchForm {

  public AbstractSearchForm() throws ProcessingException {
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

  @ConfigPropertyValue("true")
  @Override
  protected boolean getConfiguredMinimizeEnabled() {
    return true;
  }

  @ConfigPropertyValue("false")
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
