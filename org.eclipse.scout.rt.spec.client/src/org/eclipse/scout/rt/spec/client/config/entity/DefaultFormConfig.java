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

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.spec.client.gen.extract.DescriptionExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.TypeExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.form.FormTitleExtractor;

/**
 * The default configuration for {@link IForm}
 */
public class DefaultFormConfig extends DefaultEntityConfig<IForm> {

  @Override
  public List<IDocTextExtractor<IForm>> getTexts() {
    List<IDocTextExtractor<IForm>> propertyTemplate = new ArrayList<IDocTextExtractor<IForm>>();
    propertyTemplate.add(new FormTitleExtractor());
    propertyTemplate.add(new DescriptionExtractor<IForm>());
    propertyTemplate.add(new TypeExtractor<IForm>());
    return propertyTemplate;
  }

}
