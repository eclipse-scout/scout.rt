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

import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.rt.spec.client.gen.extract.DescriptionExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.SimpleTypeTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.TypeExtractor;
import org.eclipse.scout.rt.spec.client.gen.filter.IDocFilter;
import org.eclipse.scout.rt.spec.client.gen.filter.IgnoreDocFilter;

/**
 *
 */
public class DefaultEntityConfig<T extends ITypeWithClassId> implements IDocEntityConfig<T> {

  /**
   * Default filters: Ignores Types annotated with {@link org.eclipse.scout.commons.annotations.Doc Doc#ignore()}==false
   */
  @Override
  public List<IDocFilter<T>> getFilters() {
    List<IDocFilter<T>> columnFilters = new ArrayList<IDocFilter<T>>();
    columnFilters.add(new IgnoreDocFilter<T>());
    return columnFilters;
  }

  @Override
  public List<IDocTextExtractor<T>> getTexts() {
    ArrayList<IDocTextExtractor<T>> p = new ArrayList<IDocTextExtractor<T>>();
    p.add(new TypeExtractor<T>());
    p.add(new DescriptionExtractor<T>());
    return p;
  }

  @Override
  public IDocTextExtractor<T> getId() {
    return new TypeExtractor<T>();
  }

  @Override
  public IDocTextExtractor<T> getTitle() {
    return new SimpleTypeTextExtractor<T>();
  }

}
