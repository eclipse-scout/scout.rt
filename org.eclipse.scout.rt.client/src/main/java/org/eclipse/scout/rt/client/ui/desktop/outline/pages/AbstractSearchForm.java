/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.ISearchFormExtension;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.FormUtility;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * Search form for the outline must contain a SearchHandler<br>
 * The method startSearch() starts the first inner class of type IFormHandler that is found
 */
@ClassId("75cd4723-b739-4f65-a3ba-1793ac2cbf6f")
public abstract class AbstractSearchForm extends AbstractForm implements ISearchForm {

  @Override
  protected void initInternal() {
    // form
    initFormInternal();

    // mark strategy
    FormUtility.setTabBoxMarkStrategy(this, ITabBox.MARK_STRATEGY_SAVE_NEEDED);

    // custom
    interceptInitForm();
  }

  @Override
  protected boolean getConfiguredAskIfNeedSave() {
    return false;
  }

  protected static class LocalSearchFormExtension<OWNER extends AbstractSearchForm> extends LocalFormExtension<OWNER> implements ISearchFormExtension<OWNER> {

    public LocalSearchFormExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected ISearchFormExtension<? extends AbstractSearchForm> createLocalExtension() {
    return new LocalSearchFormExtension<>(this);
  }
}
