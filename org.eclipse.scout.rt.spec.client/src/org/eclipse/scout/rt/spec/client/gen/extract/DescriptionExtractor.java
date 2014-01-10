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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.rt.shared.TEXTS;

/**
 * A {@link IDocTextExtractor} for the documentation text defined by getConfiguredDoc (to be replaced by classid).
 */
public class DescriptionExtractor<T extends ITypeWithClassId> extends AbstractNamedTextExtractor<T> implements IDocTextExtractor<T> {

  public DescriptionExtractor() {
    super(TEXTS.get("org.eclipse.scout.rt.spec.doc"));
  }

  /**
   * The documentation text of a scout model entity without enclosing html tags.
   */
  @Override
  public String getText(T o) {
    String doc = tryReadingGetConfiguredDoc(o);
    if (doc != null) {
      doc = doc.replaceAll("</html>", "");
      doc = doc.replaceAll("<html>", "");
    }
    return doc;
  }

  //deprecated will be replaced with doc service reading doc by classid
  private String tryReadingGetConfiguredDoc(ITypeWithClassId o) {
    Method configuredDocMethod;
    Class<?> clazz = o.getClass();
    while (clazz != null) {
      try {
        configuredDocMethod = clazz.getDeclaredMethod("getConfiguredDoc");
        configuredDocMethod.setAccessible(true);
        Object res = configuredDocMethod.invoke(o);
        if (res instanceof String) {
          return (String) res;
        }
      }
      catch (NoSuchMethodException e) {
        // ignore
      }
      catch (IllegalAccessException e) {
        // ignore
      }
      catch (InvocationTargetException e) {
        // ignore
      }
      clazz = clazz.getSuperclass();
    }
    return "";
  }
}
