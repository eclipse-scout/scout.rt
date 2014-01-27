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
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.SpecialDescriptionExtractor;
import org.eclipse.scout.rt.spec.client.gen.filter.IDocFilter;

/**
 * Default configuration for {@link Class}
 */
public class DefaultTypesConfig implements IDocEntityListConfig<Class> {

  @Override
  public List<IDocTextExtractor<Class>> getTextExtractors() {
    ArrayList<IDocTextExtractor<Class>> extractors = new ArrayList<IDocTextExtractor<Class>>();

    extractors.add(new SpecialDescriptionExtractor(TEXTS.get("org.eclipse.scout.rt.spec.type"), "_name"));
    extractors.add(new SpecialDescriptionExtractor(TEXTS.get("org.eclipse.scout.rt.spec.doc"), "_description"));
    return extractors;
  }

  @Override
  public List<IDocFilter<Class>> getFilters() {
    return Collections.emptyList();
  }

  @Override
  public String getTitle() {
    return null;
  }

}
