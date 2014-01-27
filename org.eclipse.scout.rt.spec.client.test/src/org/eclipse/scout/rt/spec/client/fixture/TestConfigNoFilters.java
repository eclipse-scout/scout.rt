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
package org.eclipse.scout.rt.spec.client.fixture;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.spec.client.config.entity.DefaultEntityConfig;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.TypeExtractor;
import org.eclipse.scout.rt.spec.client.gen.filter.IDocFilter;

/**
 *
 */
public class TestConfigNoFilters extends DefaultEntityConfig<IFormField> {
  @Override
  public List<IDocFilter<IFormField>> getFilters() {
    return null;
  }

  @Override
  public List<IDocTextExtractor<IFormField>> getPropertyTextExtractors() {
    ArrayList<IDocTextExtractor<IFormField>> p = new ArrayList<IDocTextExtractor<IFormField>>();
    p.add(new TypeExtractor<IFormField>());
    return p;
  }

}
