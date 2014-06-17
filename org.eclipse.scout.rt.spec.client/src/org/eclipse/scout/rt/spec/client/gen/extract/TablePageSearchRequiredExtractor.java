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
package org.eclipse.scout.rt.spec.client.gen.extract;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.utility.SpecUtility;

/**
 * Extracts the value for {@link IPageWithTable#isSearchRequired()} and returns the text for
 * ({@link SpecUtility#DOC_ID_TRUE} or {@link SpecUtility#DOC_ID_FALSE}.
 */
public class TablePageSearchRequiredExtractor extends AbstractNamedTextExtractor<IPageWithTable<? extends ITable>> {

  public TablePageSearchRequiredExtractor() {
    super(TEXTS.get("org.eclipse.scout.rt.spec.searchRequired"));
  }

  @Override
  public String getText(IPageWithTable<? extends ITable> page) {
    return SpecUtility.getBooleanText(page.isSearchRequired());
  }

}
