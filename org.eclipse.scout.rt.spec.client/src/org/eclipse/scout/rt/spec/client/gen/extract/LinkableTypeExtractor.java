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
package org.eclipse.scout.rt.spec.client.gen.extract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.commons.ListUtility;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.link.DocLink;

/**
 * An XML text with link templates that can later be converted to links, once the generated resources are known.
 * <p>
 * Example:
 * </p>
 */
public class LinkableTypeExtractor<T> extends AbstractNamedTextExtractor<T> implements IDocTextExtractor<T> {
  public static final String LINKS_TAG_NAME = "links";

  private final List<Class<?>> m_ignoredClasses;

  public LinkableTypeExtractor() {
    this(TEXTS.get("org.eclipse.scout.rt.spec.type"), Object.class, AbstractPropertyObserver.class, AbstractFormField.class);
  }

  public LinkableTypeExtractor(String typeName, Class<?>... ignoredClass) {
    super(TEXTS.get("org.eclipse.scout.rt.spec.type"));
    m_ignoredClasses = Arrays.asList(ignoredClass);
  }

  @Override
  public String getText(T o) {
    Class<?> c = o.getClass();
    List<String> clazzNames = getTypesAsLinks(c, m_ignoredClasses);
    String formattedClasses = ListUtility.format(clazzNames, "");
    String links = DocLink.encloseInTags(formattedClasses, LINKS_TAG_NAME);
    return DocLink.encloseInTags(links, DocLink.REPLACE_TAG_NAME);
  }

  /**
   * The type hierarchy of a class ignoring the once in the ignore list
   * 
   * @param c
   * @param ignoreList
   */
  private List<String> getTypesAsLinks(Class<?> c, List<Class<?>> ignoreList) {
    List<String> clazzes = new ArrayList<String>();
    while (c != null) {
      if (!ignoreList.contains(c)) {
        DocLink link = new DocLink(c.getName(), c.getSimpleName());
        clazzes.add(link.toXML());
      }
      c = c.getSuperclass();
    }
    return clazzes;
  }

}
