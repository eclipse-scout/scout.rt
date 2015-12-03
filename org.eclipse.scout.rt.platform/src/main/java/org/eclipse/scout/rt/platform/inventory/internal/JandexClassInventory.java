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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.inventory.IClassInventory;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

public class JandexClassInventory implements IClassInventory {

  private final IndexView m_index;

  public JandexClassInventory(IndexView index) {
    m_index = index;
  }

  @Override
  public Set<IClassInfo> getAllKnownSubClasses(Class<?> queryClass) {
    Assertions.assertNotNull(queryClass);
    Collection<ClassInfo> subclasses;
    if (queryClass.isInterface()) {
      subclasses = m_index.getAllKnownImplementors(DotName.createSimple(queryClass.getName()));
    }
    else {
      subclasses = m_index.getAllKnownSubclasses(DotName.createSimple(queryClass.getName()));
    }
    return convertClassInfos(subclasses);
  }

  @Override
  public Set<IClassInfo> getKnownAnnotatedTypes(Class<?> annotation) {
    Assertions.assertNotNull(annotation);
    Assertions.assertTrue(annotation.isAnnotation(), "given class is not an annotation: %s", annotation);
    Collection<AnnotationInstance> annotationInstances = m_index.getAnnotations(DotName.createSimple(annotation.getName()));
    return convertAnnotationInstance(annotationInstances);
  }

  public IClassInfo getClassInfo(Class<?> queryClass) {
    Assertions.assertNotNull(queryClass);
    ClassInfo ci = m_index.getClassByName(DotName.createSimple(queryClass.getName()));
    if (ci == null) {
      return null;
    }
    return new JandexClassInfo(ci);
  }

  protected Set<IClassInfo> convertClassInfos(Collection<ClassInfo> classInfos) {
    Set<IClassInfo> result = new HashSet<IClassInfo>(classInfos.size());
    for (ClassInfo classInfo : classInfos) {
      result.add(new JandexClassInfo(classInfo));
    }
    return result;
  }

  protected Set<IClassInfo> convertAnnotationInstance(Collection<AnnotationInstance> annotationInstances) {
    Set<IClassInfo> result = new HashSet<IClassInfo>(annotationInstances.size());
    for (AnnotationInstance annotationInstance : annotationInstances) {
      AnnotationTarget target = annotationInstance.target();
      if (target instanceof ClassInfo) {
        result.add(new JandexClassInfo((ClassInfo) target));
      }
    }
    return result;
  }
}
