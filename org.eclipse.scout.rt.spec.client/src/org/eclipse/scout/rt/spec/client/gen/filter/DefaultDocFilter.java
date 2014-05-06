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
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.spec.client.gen.extract.DescriptionExtractor;

/**
 * A default doc filter that implements the following rules:
 * <p>
 * <li>If a filter annotation ({@link Doc#filter()}) is present for the element filtering is applied according the
 * annotation.
 * <li>For GroupBoxes with no filter annotation and neither label nor description defined: {@link Filtering#TRANSPARENT}
 * is applied.
 * <li>For all other elements: {@link Filtering#ACCEPT} is applied.
 */
public class DefaultDocFilter<T> implements IDocFilter<T> {

  @Override
  public Filtering accept(T o) {
    Class<? extends Object> clazz = (o instanceof Class) ? (Class<?>) o : o.getClass();
    Doc docAnnotation = clazz.getAnnotation(Doc.class);
    if (docAnnotation != null) {
      return docAnnotation.filter();
    }
    if (o instanceof IGroupBox) {
      IGroupBox groupBox = (IGroupBox) o;
      DescriptionExtractor<IGroupBox> descExtractor = new DescriptionExtractor<IGroupBox>();
      if (groupBox.isMainBox() || !(StringUtility.hasText(groupBox.getLabel()) || StringUtility.hasText(descExtractor.getText(groupBox)))) {
        return Filtering.TRANSPARENT;
      }
    }
    return Filtering.ACCEPT;
  }

}
