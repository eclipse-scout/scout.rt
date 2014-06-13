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

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiUtility;

/**
 * A {@link IDocTextExtractor} for the SearchForm of a TablePage
 */
public class SearchFormExtractor extends AbstractNamedTextExtractor<IPageWithTable<? extends ITable>> {

  /**
   * @param name
   */
  public SearchFormExtractor() {
    super(TEXTS.get("org.eclipse.scout.rt.spec.searchform"));
  }

  @Override
  public String getText(IPageWithTable<? extends ITable> page) {
    ISearchForm searchForm = page.getSearchFormInternal();
    if (searchForm == null) {
      return null;
    }
    Class<? extends ISearchForm> searchFormClass = searchForm.getClass();
    return MediawikiUtility.createLink("c_" + ConfigurationUtility.getAnnotatedClassIdWithFallback(searchFormClass), searchFormClass.getSimpleName());
  }

}
