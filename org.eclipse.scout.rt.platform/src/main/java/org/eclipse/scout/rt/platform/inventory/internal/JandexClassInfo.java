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
package org.eclipse.scout.rt.platform.inventory.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.List;

import org.eclipse.scout.rt.platform.InjectBean;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JandexClassInfo implements IClassInfo {
  private static final Logger LOG = LoggerFactory.getLogger(JandexClassInfo.class);
  private static final String CONSTRUCTOR_NAME = "<init>";//NOSONAR

  private final ClassInfo m_classInfo;
  private volatile Class<?> m_class;

  public JandexClassInfo(ClassInfo classInfo) {
    Assertions.assertNotNull(classInfo);
    m_classInfo = classInfo;
  }

  @Override
  public String name() {
    return m_classInfo.name().toString();
  }

  @Override
  public int flags() {
    return m_classInfo.flags();
  }

  protected void ensureClassLoaded() {
    if (m_class == null) {
      synchronized (this) {
        if (m_class == null) {
          try {
            m_class = Class.forName(name());
          }
          catch (ClassNotFoundException | NoClassDefFoundError ex) {
            throw new PlatformException("Error loading class '" + name() + "' with flags 0x" + Integer.toHexString(flags()), ex);
          }
        }
      }
    }
  }

  @Override
  public boolean hasNoArgsConstructor() {
    return m_classInfo.hasNoArgsConstructor();
  }

  @Override
  public boolean hasInjectableConstructor() {
    List<AnnotationInstance> list = m_classInfo.annotations().get(DotName.createSimple(InjectBean.class.getName()));
    if (list == null || list.isEmpty()) {
      return false;
    }
    for (AnnotationInstance inst : list) {
      AnnotationTarget target = inst.target();
      if (AnnotationTarget.Kind.METHOD != target.kind()) {
        continue;
      }
      if (!CONSTRUCTOR_NAME.equals(target.asMethod().name())) {
        continue;
      }
      //target is annotated with @InjectBean, is a method and is a constructor
      return true;
    }
    return false;
  }

  @Override
  public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
    return m_classInfo.annotations().containsKey(DotName.createSimple(annotationType.getName()));
  }

  @Override
  public Object getAnnotationValue(Class<? extends Annotation> annotationType, String annotationParameterName) {
    List<AnnotationInstance> annotationInstances = m_classInfo.annotations().get(DotName.createSimple(annotationType.getName()));
    if (CollectionUtility.isEmpty(annotationInstances)) {
      return null;
    }
    for (AnnotationInstance annotationInstance : annotationInstances) {
      if (annotationInstance.target().kind() == AnnotationTarget.Kind.CLASS) {
        AnnotationValue annotationValue = annotationInstance.value(annotationParameterName);
        if (annotationValue != null) {
          return annotationValue.value();
        }
      }
    }
    return null;
  }

  @Override
  public Class<?> resolveClass() {
    ensureClassLoaded();
    return m_class;
  }

  @Override
  public boolean isInstanciable() {
    if (isAbstract() || isInterface() || !isPublic()) {
      return false;
    }

    try {
      // top level or static inner
      if (m_classInfo.enclosingClass() != null) {
        if (!Modifier.isStatic(flags())) {
          return false;
        }
        if (!Modifier.isPublic(flags())) {
          return false;
        }
      }
    }
    catch (Exception ex) {
      LOG.warn("Could not resolve class [{}]", name(), ex);
      return false;
    }
    return true;
  }

  @Override
  public boolean isPublic() {
    return Modifier.isPublic(flags());
  }

  @Override
  public boolean isFinal() {
    return Modifier.isFinal(flags());
  }

  @Override
  public boolean isInterface() {
    return Modifier.isInterface(flags());
  }

  @Override
  public boolean isAbstract() {
    return Modifier.isAbstract(flags());
  }

  @Override
  public boolean isSynthetic() {
    return (flags() & ACC_SYNTHETIC) != 0;
  }

  @Override
  public boolean isAnnotation() {
    return (flags() & ACC_ANNOTATION) != 0;
  }

  @Override
  public boolean isEnum() {
    return (flags() & ACC_ENUM) != 0;
  }

  @Override
  public String toString() {
    return m_classInfo.toString();
  }

  @Override
  public int hashCode() {
    return m_classInfo.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    JandexClassInfo other = (JandexClassInfo) obj;
    if (m_classInfo == null) {
      if (other.m_classInfo != null) {
        return false;
      }
    }
    else if (!m_classInfo.equals(other.m_classInfo)) {
      return false;
    }
    return true;
  }
}
