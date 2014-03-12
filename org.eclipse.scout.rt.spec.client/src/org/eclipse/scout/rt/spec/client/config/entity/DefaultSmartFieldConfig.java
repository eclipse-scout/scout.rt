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

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.spec.client.gen.extract.DescriptionExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.SmartFieldTypeExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.form.field.FieldDetailTitleExtractor;

/**
 *
 */
public class DefaultSmartFieldConfig extends DefaultEntityConfig<ISmartField<?>> {

  @Override
  public List<IDocTextExtractor<ISmartField<?>>> getPropertyTextExtractors() {
    ArrayList<IDocTextExtractor<ISmartField<?>>> extrators = new ArrayList<IDocTextExtractor<ISmartField<?>>>();
    extrators.add(new DescriptionExtractor<ISmartField<?>>());
    extrators.add(new SmartFieldTypeExtractor());
    return extrators;
  }

  @Override
  public IDocTextExtractor<ISmartField<?>> getTitleExtractor() {
    return new FieldDetailTitleExtractor<ISmartField<?>>();
  }
}
