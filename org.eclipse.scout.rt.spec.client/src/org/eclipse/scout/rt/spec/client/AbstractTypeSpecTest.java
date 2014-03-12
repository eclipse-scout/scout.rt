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

import java.lang.reflect.Modifier;
import java.util.Set;

import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.osgi.BundleInspector;
import org.eclipse.scout.rt.spec.client.config.entity.IDocEntityListConfig;
import org.eclipse.scout.rt.spec.client.gen.TypeSpecGenerator;
import org.eclipse.scout.rt.spec.client.gen.extract.SpecialDescriptionExtractor;
import org.eclipse.scout.rt.spec.client.out.IDocSection;

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

  public AbstractTypeSpecTest(String id, String title, Class<? extends ITypeWithClassId> supertype, boolean listTypesWithoutDoc) {
    m_id = id;
    m_title = title;
    m_supertype = supertype;
    m_listTypesWithoutDoc = listTypesWithoutDoc;
  }

  public AbstractTypeSpecTest(String id, String title, Class<? extends ITypeWithClassId> supertype) {
    this(id, title, supertype, false);
  }

  @Override
  public void generateSpec() throws ProcessingException {
    IDocSection doc = generate(getAllClasses());
    writeMediawikiFile(doc, m_id, new String[]{});
  }

  protected Set<Class> getAllClasses() throws ProcessingException {
    return SpecUtility.getAllClasses(new BundleInspector.IClassFilter() {
      @Override
      public boolean accept(Class c) {
        return acceptClass(c);
      }
    });
  }

  protected boolean acceptClass(Class c) {
    if (c == null || !m_supertype.isAssignableFrom(c)) {
      return false;
    }
    if (m_listTypesWithoutDoc) {
      return !c.isInterface() && !Modifier.isAbstract(c.getModifiers());
    }
    String typeDescription = new SpecialDescriptionExtractor(null, "_name").getText(c);
    return typeDescription != null;
  }

  protected IDocSection generate(Set<Class> fieldTypes) {
    TypeSpecGenerator g = new TypeSpecGenerator(getEntityListConfig(), m_id, m_title);
    return g.getDocSection(fieldTypes);
  }

  protected IDocEntityListConfig<Class> getEntityListConfig() {
    return getConfiguration().getGenericTypesConfig();
  }

}
