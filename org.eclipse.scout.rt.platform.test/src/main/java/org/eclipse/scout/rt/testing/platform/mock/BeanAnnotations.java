/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform.mock;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

/**
 * Creates, injects and registers mock bean annotations
 */
@ApplicationScoped
public class BeanAnnotations {
  private volatile List<IBean<?>> m_regs;

  /**
   * Initialize fields annotated with {@link BeanMock}
   */
  public void init(Object testObj) {
    List<IBean<?>> regs = new ArrayList<IBean<?>>();
    Field[] fields = testObj.getClass().getDeclaredFields();
    for (Field f : fields) {
      if (isBeanMock(f)) {
        regs.add(registerMock(testObj, f));
      }
    }
    m_regs = Collections.unmodifiableList(regs);
  }

  private IBean<?> registerMock(Object testObj, Field f) {
    Class<?> mockType = f.getType();
    BeanMetaData beanData = BEANS.get(IBeanAnnotationMetaDataProducer.class).produce(mockType);
    IBean<?> reg = Platform.get().getBeanManager().registerBean(beanData);
    Object mock = BEANS.get(mockType);
    trySetMock(f, mock, testObj);
    return reg;
  }

  private boolean isBeanMock(Field f) {
    return f.getAnnotation(BeanMock.class) != null;
  }

  private void trySetMock(Field field, Object mock, Object testObj) {
    try {
      field.setAccessible(true);
      field.set(testObj, mock);
    }
    catch (IllegalArgumentException | IllegalAccessException e) {
      throw new ProcessingException("Failed to set mock to field " + field.getName(), e);
    }
  }

  /**
   * Remove all registrations made in this class
   */
  public void clear() {
    if (m_regs != null) {
      for (IBean<?> bean : m_regs) {
        Platform.get().getBeanManager().unregisterBean(bean);
      }
    }
    m_regs = null;
  }

}
