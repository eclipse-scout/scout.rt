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

import java.util.HashSet;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.runtime.BundleBrowser;
import org.eclipse.scout.rt.spec.client.gen.TypeSpecGenerator;
import org.eclipse.scout.rt.spec.client.gen.extract.SpecialDescriptionExtractor;
import org.eclipse.scout.rt.spec.client.internal.Activator;
import org.eclipse.scout.rt.spec.client.out.IDocSection;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * Abstract spec test for creating a spec file with a table describing types (eg. form fields, columns, ...)
 * <p>
 * A type appears on the list if these criteria are met:<br>
 * - The type is a subtype or the same type as the supertype provided in the constructor.<br>
 * - A doc text with the key "[types classid]_name" is defined
 * <p>
 * The resulting table consists of the two columns:<br>
 * - Name: filled with doc text with the key "[types classid]_name"<br>
 * - Description: filled with doc text with the key "[types classid]_description"
 */
public abstract class AbstractTypeSpecTest extends AbstractSpecGen {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractTypeSpecTest.class);
  private Class<?> m_supertype;
  private String m_id;
  private String m_title;

  public AbstractTypeSpecTest(String id, String title, Class<? extends ITypeWithClassId> supertype) {
    m_id = id;
    m_title = title;
    m_supertype = supertype;
  }

  @Test
  public void generateFieldTypeSpec() throws ProcessingException {

    Class[] fieldTypes = getAllClasses();
    IDocSection doc = generate(fieldTypes);
    write(doc, m_id, new String[]{});
  }

  protected Class[] getAllClasses() throws ProcessingException {
    HashSet<Class> discoveredClasses = new HashSet<Class>();
    for (Bundle bundle : Activator.getDefault().getBundle().getBundleContext().getBundles()) {
      // Skip fragments, because classes from fragments are read when browsing the host bundle
      if (Platform.isFragment(bundle)) {
        continue;
      }
      String[] classNames;
      BundleBrowser bundleBrowser = new BundleBrowser(bundle.getSymbolicName(), bundle.getSymbolicName());
      classNames = bundleBrowser.getClasses(false, true);
      Class c = null;
      for (String className : classNames) {
        try {
          c = bundle.loadClass(className);
        }
        catch (Throwable t) {
          // nop: we are only interested in loadable classes
        }
        if (acceptClass(c)) {
          discoveredClasses.add(c);
        }
      }
    }

    Class[] fieldTypes = discoveredClasses.toArray(new Class[discoveredClasses.size()]);
    return fieldTypes;
  }

  protected boolean acceptClass(Class c) {
    if (c == null || !m_supertype.isAssignableFrom(c)) {
      return false;
    }
    String typeDescription = new SpecialDescriptionExtractor(null, "_name").getText(c);
    return typeDescription != null;
  }

  protected IDocSection generate(Class[] fieldTypes) {
    TypeSpecGenerator g = new TypeSpecGenerator(getConfiguration(), m_id, m_title);
    return g.getDocSection(fieldTypes);
  }

}
