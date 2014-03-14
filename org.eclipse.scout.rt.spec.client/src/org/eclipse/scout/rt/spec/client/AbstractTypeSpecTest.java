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
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.osgi.BundleInspector;
import org.eclipse.scout.rt.spec.client.config.entity.IDocEntityTableConfig;
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

  protected Set<Class> getAllClasses() throws ProcessingException {
    return SpecUtility.getAllClasses(new BundleInspector.IClassFilter() {
      @Override
      public boolean accept(Class c) {
        return acceptClass(c);
      }
    });
  }

  protected boolean acceptClass(Class c) {
    return isDocType(c, m_supertype, m_listTypesWithoutDoc);
  }

  /**
   * A <code>type</code> is considered a documented type if the following criterias are met:
   * <p>
   * <li>Instances of the type can be assigned to the <code>supertype</code>.
   * <li>Either the type is annotated with a {@link ClassId} annotation for which a doc-text with key
   * <code>[classid]_name</code> is available or <code>listTypesWithoutDoc</code> is set to true.
   * 
   * @param type
   * @param supertype
   * @param listTypesWithoutDoc
   * @return
   */
  public static boolean isDocType(Class type, Class<?> supertype, boolean listTypesWithoutDoc) {
    if (type == null || !supertype.isAssignableFrom(type)) {
      return false;
    }
    if (listTypesWithoutDoc) {
      return !type.isInterface() && !Modifier.isAbstract(type.getModifiers());
    }
    String typeDescription = new SpecialDescriptionExtractor(null, "_name").getText(type);
    return typeDescription != null;
  }

  protected IDocSection generate(Set<Class> fieldTypes) {
    TypeSpecGenerator g = new TypeSpecGenerator(getEntityListConfig(), m_id, m_title, m_introduction);
    return g.getDocSection(fieldTypes);
  }

  protected IDocEntityTableConfig<Class> getEntityListConfig() {
    return getConfiguration().getGenericTypesTableConfig();
  }

}
