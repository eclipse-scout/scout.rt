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

import org.eclipse.scout.rt.spec.client.gen.extract.CodeTypeCodesExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;

/**
 * Default configuration for {@link Class}
 */
public class DefaultCodeTypeTypesTableConfig extends DefaultTypesTableConfig {

  @Override
  public List<IDocTextExtractor<Class>> getTextExtractors() {
    ArrayList<IDocTextExtractor<Class>> extractors = new ArrayList<IDocTextExtractor<Class>>();
    extractors.addAll(super.getTextExtractors());
    extractors.add(new CodeTypeCodesExtractor());
    return extractors;
  }

}
