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
package org.eclipse.scout.rt.client.ui.basic.table.control;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.TEXTS;

/**
 * @since 5.1.0
 */
public class SearchFormTableControl extends AbstractTableControl {

  @Override
  protected void initConfig() {
    super.initConfig();
    setIconId(AbstractIcons.Search);
    setTooltipText(TEXTS.get("Search"));
  }

  public void setSearchForm(ISearchForm searchForm) {
    if (searchForm == null) {
      setEnabled(false);
    }
    else {
      // FIXME AWE: (table) check if this form listener is really needed. delete if not
      setEnabled(true);
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

  @Override
  protected String getConfiguredKeyStroke() {
    return IKeyStroke.F6;
  }
}
