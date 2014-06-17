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
package org.eclipse.scout.rt.spec.client.config.entity;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.spec.client.gen.extract.DescriptionExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.TablePageIconExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.SearchFormExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.TablePageSearchRequiredExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.TypeExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.form.page.TablePageTitleExtractor;

/**
 *
 */
public class DefaultTablePageConfig extends DefaultEntityConfig<IPageWithTable<? extends ITable>> {

  @Override
  public IDocTextExtractor<IPageWithTable<? extends ITable>> getTitleExtractor() {
    return new TablePageTitleExtractor<IPageWithTable<? extends ITable>>();
  }

  @Override
  public List<IDocTextExtractor<IPageWithTable<? extends ITable>>> getPropertyTextExtractors() {
    List<IDocTextExtractor<IPageWithTable<? extends ITable>>> extractors = new ArrayList<IDocTextExtractor<IPageWithTable<? extends ITable>>>();
    extractors.add(new DescriptionExtractor<IPageWithTable<? extends ITable>>());
    extractors.add(new SearchFormExtractor());
    extractors.add(new TablePageIconExtractor());
    extractors.add(new TablePageSearchRequiredExtractor());
    extractors.add(new TypeExtractor<IPageWithTable<? extends ITable>>());
    return extractors;
  }

}
