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

import org.eclipse.scout.commons.ArrayComparator;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.SimpleTypeTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.SpecialDescriptionExtractor;
import org.eclipse.scout.rt.spec.client.gen.filter.DefaultDocFilter;
import org.eclipse.scout.rt.spec.client.gen.filter.IDocFilter;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiUtility;

/**
 * Default configuration for {@link Class}
 */
public class DefaultTypesTableConfig implements IDocEntityTableConfig<Class<?>> {

  @Override
  public List<IDocTextExtractor<Class<?>>> getTextExtractors() {
    ArrayList<IDocTextExtractor<Class<?>>> extractors = new ArrayList<IDocTextExtractor<Class<?>>>();
    extractors.add(new SpecialDescriptionExtractor(TEXTS.get("org.eclipse.scout.rt.spec.type"), "_name", true, new SimpleTypeTextExtractor<Class>()));
    extractors.add(new SpecialDescriptionExtractor(TEXTS.get("org.eclipse.scout.rt.spec.doc"), "_description"));
    return extractors;
  }

  @Override
  public List<IDocFilter<Class<?>>> getFilters() {
    List<IDocFilter<Class<?>>> filters = new ArrayList<IDocFilter<Class<?>>>();
    filters.add(new DefaultDocFilter<Class<?>>());
    return filters;
  }

  @Override
  public String getTitle() {
    return null;
  }

  @Override
  public ArrayComparator.ColumnComparator[] getSortColumns() {
    return new ArrayComparator.ColumnComparator[]{new ArrayComparator.ColumnComparator(0, new ArrayComparator.DefaultObjectComparator(LocaleThreadLocal.get()) {
      @Override
      public int compare(Object o1, Object o2) {
        o1 = o1 instanceof String ? MediawikiUtility.removeAnchorsAndLinks((String) o1) : o1;
        o2 = o2 instanceof String ? MediawikiUtility.removeAnchorsAndLinks((String) o2) : o2;
        return super.compare(o1, o2);
      }
    })};
  }

}
