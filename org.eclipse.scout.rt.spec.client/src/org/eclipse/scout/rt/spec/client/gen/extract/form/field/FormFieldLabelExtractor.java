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
package org.eclipse.scout.rt.spec.client.gen.extract.form.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.commons.ListUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.gen.DocGenUtility;
import org.eclipse.scout.rt.spec.client.gen.extract.AbstractNamedTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.LinkableTypeExtractor;
import org.eclipse.scout.rt.spec.client.gen.filter.IDocFilter;
import org.eclipse.scout.rt.spec.client.link.DocLink;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiUtility;

/**
 * Extracts the label of a field
 */
public class FormFieldLabelExtractor extends AbstractNamedTextExtractor<IFormField> implements IDocTextExtractor<IFormField> {
  protected static final String INDENT = "&nbsp;&nbsp;&nbsp;";
  private boolean m_hierarchicLabels;
  private List<IDocFilter<IFormField>> m_docFilters;

  // TODO ASA really needed: m_ignoredClasses?
  private final List<Class<?>> m_ignoredClasses;

  /**
   * @param hierarchicLabels
   *          If true, an indentation is applied reflecting the fields hierarchic position (= nesting in GroupBoxes)
   * @param docFilters
   *          Only relevant when <code>hierarchicLabels</code> is <code>true</code>: Used when evaluating the super
   *          hierarchy of a field for calculation the indentation.
   * @param ignoredClasses
   *          Ignored classes for linking.
   */
  public FormFieldLabelExtractor(boolean hierarchicLabels, List<IDocFilter<IFormField>> docFilters, Class<?>... ignoredClasses) {
    super(TEXTS.get("org.eclipse.scout.rt.spec.label"));
    m_hierarchicLabels = hierarchicLabels;
    m_docFilters = docFilters;
    m_ignoredClasses = Arrays.asList(ignoredClasses);
  }

  /**
   * Convenience overload for {@link #FormFieldLabelExtractor(boolean, List, Class...)} which sets a default
   * set of ignored classes.
   * 
   * @param hierarchicLabels
   * @param docFilters
   */
  public FormFieldLabelExtractor(boolean hierarchicLabels, List<IDocFilter<IFormField>> docFilters) {
    this(hierarchicLabels, docFilters, Object.class, AbstractPropertyObserver.class, AbstractFormField.class);
  }

  @Override
  public String getText(IFormField field) {
    List<String> linkList = getDisplaynameWithTypesAsLinks(field, m_ignoredClasses);
    String links = ListUtility.format(linkList, "");
    // TODO ASA move constant LINKS_TAG_NAME
    String taggedLinks = DocLink.encloseInTags(links, LinkableTypeExtractor.LINKS_TAG_NAME);
    return decorateText(field, taggedLinks);
  }

  /**
   * <li>Add indentation <li>Style italic if display text is a substitution for an empty label.
   * 
   * @param field
   * @param taggedLinks
   * @return decoratedText
   */
  protected String decorateText(IFormField field, String taggedLinks) {
    StringBuilder sb = new StringBuilder();
    sb.append(getIndentation(field));
    boolean emptyLabel = StringUtility.isNullOrEmpty(getLable(field));
    if (emptyLabel) {
      sb.append("''");
    }
    sb.append(DocLink.encloseInTags(taggedLinks, DocLink.REPLACE_TAG_NAME));
    if (emptyLabel) {
      sb.append("''");
    }
    return sb.toString();
  }

  /**
   * Creates a list with the type hierarchy of a class as links with the fields label as display text
   * 
   * @param field
   * @param ignoreList
   */
  // TODO ASA unittest
  protected List<String> getDisplaynameWithTypesAsLinks(IFormField field, List<Class<?>> ignoreList) {
    List<String> linkes = new ArrayList<String>();
    Class<?> c = field.getClass();
    while (c != null) {
      if (!ignoreList.contains(c)) {
        DocLink link = new DocLink(c.getName(), getDisplayText(field));
        linkes.add(link.toXML());
      }
      c = c.getSuperclass();
    }
    return linkes;
  }

  /**
   * Reads label-property returns its value with correct indentation
   */
  protected String getDisplayText(IFormField field) {
    StringBuilder sb = new StringBuilder();
    String label = getLable(field);
    sb.append(StringUtility.substituteWhenEmpty(label, TEXTS.get("org.eclipse.scout.rt.spec.action.withoutLabel")));
    // TODO ASA replace mnemonics: how are ampersands escaped when they are not used for mnemonics?
    return sb.toString().replaceAll("&", "");
  }

  protected String getLable(IFormField field) {
    return MediawikiUtility.transformToWiki((String) field.getProperty(IFormField.PROP_LABEL));
  }

  protected String getIndentation(IFormField field) {
    StringBuilder sb = new StringBuilder();
    if (m_hierarchicLabels) {
      int level = getLevel(field);
      sb.append(StringUtility.repeat(INDENT, level));
    }
    return sb.toString();
  }

  /**
   * calculate the level for indentation
   * 
   * @param field
   * @return
   */
  protected int getLevel(IFormField field) {
    IFormField parentField = field.getParentField();
    int level = 0;
    while (parentField != null) {
      if (DocGenUtility.isAccepted(parentField, m_docFilters)) {
        ++level;
      }
      parentField = parentField.getParentField();
    }
    return level;
  }
}
