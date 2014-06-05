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
package org.eclipse.scout.rt.client.ui.html.client.ext;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;

public class SearchFormTableControl extends AbstractTableControl {

  @Override
  protected void initConfig() {
    super.initConfig();

    setCssClass("control-query");
    setLabel("Suche");
    setGroup("Suche");
  }

  public void setSearchForm(ISearchForm searchForm) {
    if (searchForm == null) {
      return;
    }
    searchForm.addFormListener(new FormListener() {
      @Override
      public void formChanged(FormEvent e) throws ProcessingException {
        if (e.getType() == FormEvent.TYPE_LOAD_COMPLETE) {
          setForm(e.getForm());
        }
      }
    });
  }
}
