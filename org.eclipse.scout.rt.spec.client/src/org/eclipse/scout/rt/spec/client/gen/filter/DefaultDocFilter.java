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
package org.eclipse.scout.rt.spec.client.gen.filter;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Doc;
import org.eclipse.scout.commons.annotations.Doc.Filtering;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.spec.client.gen.extract.DescriptionExtractor;

/**
 * A default doc filter that implements the following rules which can optionally be overridden with the annotation
 * {@link Doc#filter()}:<br>
 * TODO ASA docu: default filtering rules
 */
public class DefaultDocFilter<T> implements IDocFilter<T> {

  @Override
  public Filtering accept(T o) {
    Doc docAnnotation = o.getClass().getAnnotation(Doc.class);
    if (docAnnotation != null) {
      return docAnnotation.filter();
    }
    if (o instanceof AbstractGroupBox) {
      AbstractGroupBox groupBox = (AbstractGroupBox) o;
      DescriptionExtractor<AbstractGroupBox> descExtractor = new DescriptionExtractor<AbstractGroupBox>();
      if (groupBox.isMainBox() || !(StringUtility.hasText(groupBox.getLabel()) || StringUtility.hasText(descExtractor.getText(groupBox)))) {
        return Filtering.TRANSPARENT;
      }
    }
    return Filtering.ACCEPT;
  }

}
