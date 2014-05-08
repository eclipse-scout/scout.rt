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
package org.eclipse.scout.rt.spec.client;

import java.util.Collection;
import java.util.Set;

import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.osgi.BundleInspector;
import org.eclipse.scout.rt.spec.client.config.entity.IDocEntityTableConfig;
import org.eclipse.scout.rt.spec.client.filter.FilterUtility;
import org.eclipse.scout.rt.spec.client.gen.TypeSpecGenerator;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.eclipse.scout.rt.spec.client.utility.SpecUtility;

/**
 * Abstract spec test for creating a spec file with a table describing types (eg. form fields, columns, ...)
 * <p>
 * A type appears on the list if these criteria are met:<br>
 * <li>The type is a subtype or the same type as the supertype provided in the constructor.<br>
 * <li>Either a doc text with the key "[types classid]_name" is defined or the property {@link #m_listTypesWithoutDoc}
 * is set to true and the type is neither an interface nore an abstract class.
 * <p>
 * The resulting table consists of the two columns:<br>
 * <li>Name: filled with doc text with the key "[types classid]_name"<br>
 * <li>Description: filled with doc text with the key "[types classid]_description"
 */
public abstract class AbstractTypeSpecTest extends AbstractSpecGenTest {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractTypeSpecTest.class);
  protected Class<?> m_supertype;
  protected String m_id;
  protected String m_title;
  protected boolean m_listTypesWithoutDoc;
  private String m_introduction;

  public AbstractTypeSpecTest(String id, String title, String introduction, Class<? extends ITypeWithClassId> supertype, boolean listTypesWithoutDoc) {
    m_id = id;
    m_title = title;
    m_introduction = introduction;
    m_supertype = supertype;
    m_listTypesWithoutDoc = listTypesWithoutDoc;
  }

  public AbstractTypeSpecTest(String id, String title, String introduction, Class<? extends ITypeWithClassId> supertype) {
    this(id, title, introduction, supertype, false);
  }

  @Override
  public void generateSpec() throws ProcessingException {
    IDocSection doc = generate(getAllClasses());
    writeMediawikiFile(doc, m_id, new String[]{});
  }

  protected Set<Class<?>> getAllClasses() throws ProcessingException {
    return SpecUtility.getAllClasses(new BundleInspector.IClassFilter() {
      @Override
      public boolean accept(Class c) {
        return acceptClass(c);
      }
    });
  }

  protected boolean acceptClass(Class c) {
    return FilterUtility.isAccepted(c, getEntityListConfig().getFilters()) && SpecUtility.isDocType(c, m_supertype, m_listTypesWithoutDoc);
  }

  protected IDocSection generate(Collection<Class<?>> fieldTypes) {
    TypeSpecGenerator g = new TypeSpecGenerator(getEntityListConfig(), m_id, m_title, m_introduction);
    return g.getDocSection(fieldTypes);
  }

  protected IDocEntityTableConfig<Class<?>> getEntityListConfig() {
    return getConfiguration().getGenericTypesTableConfig();
  }

}
