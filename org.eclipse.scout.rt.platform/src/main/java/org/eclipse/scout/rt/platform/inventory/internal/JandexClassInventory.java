/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.inventory.internal;

import java.lang.reflect.Modifier;
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
    Collection<ClassInfo> subclasses1;
    Set<ClassInfo> subclasses2;
    if (queryClass.isInterface()) {
      //'getAllKnownImplementors' returns all subclasses but not all subinterfaces. It ignores subinterfaces that have no implementor class at all.
      subclasses1 = m_index.getAllKnownImplementors(DotName.createSimple(queryClass.getName()));
      subclasses2 = new HashSet<>();
      collectAllKnownSubinterfacesRecursive(DotName.createSimple(queryClass.getName()), subclasses2);
    }
    else {
      subclasses1 = m_index.getAllKnownSubclasses(DotName.createSimple(queryClass.getName()));
      subclasses2 = null;
    }
    return convertClassInfos(subclasses1, subclasses2);
  }

  @Override
  public Set<IClassInfo> getAllKnownSubClasses(IClassInfo queryClassInfo) {
    Assertions.assertNotNull(queryClassInfo);
    Collection<ClassInfo> subclasses1;
    Set<ClassInfo> subclasses2;
    if (queryClassInfo.isInterface()) {
      //'getAllKnownImplementors' returns all subclasses but not all subinterfaces. It ignores subinterfaces that have no implementor class at all.
      subclasses1 = m_index.getAllKnownImplementors(DotName.createSimple(queryClassInfo.name()));
      subclasses2 = new HashSet<>();
      collectAllKnownSubinterfacesRecursive(DotName.createSimple(queryClassInfo.name()), subclasses2);
    }
    else {
      subclasses1 = m_index.getAllKnownSubclasses(DotName.createSimple(queryClassInfo.name()));
      subclasses2 = null;
    }
    return convertClassInfos(subclasses1, subclasses2);
  }

  protected void collectAllKnownSubinterfacesRecursive(DotName queryName, Set<ClassInfo> collector) {
    Collection<ClassInfo> subinterfaces = m_index.getKnownDirectImplementors(queryName);
    if (!subinterfaces.isEmpty()) {
      for (ClassInfo ci : subinterfaces) {
        if (Modifier.isInterface(ci.flags()) && collector.add(ci)) {
          collectAllKnownSubinterfacesRecursive(ci.name(), collector);
        }
      }
    }
  }

  @Override
  public Set<IClassInfo> getKnownAnnotatedTypes(Class<?> annotation) {
    Assertions.assertNotNull(annotation);
    Assertions.assertTrue(annotation.isAnnotation(), "given class is not an annotation: {}", annotation);
    Collection<AnnotationInstance> annotationInstances = m_index.getAnnotations(DotName.createSimple(annotation.getName()));
    return convertAnnotationInstance(annotationInstances);
  }

  @Override
  public Set<IClassInfo> getKnownAnnotatedTypes(IClassInfo annotationInfo) {
    Assertions.assertNotNull(annotationInfo);
    Assertions.assertTrue(annotationInfo.isAnnotation(), "given class is not an annotation: {}", annotationInfo.name());
    Collection<AnnotationInstance> annotationInstances = m_index.getAnnotations(DotName.createSimple(annotationInfo.name()));
    return convertAnnotationInstance(annotationInstances);
  }

  public IClassInfo getClassInfo(String queryClassName) {
    Assertions.assertNotNull(queryClassName);
    ClassInfo ci = m_index.getClassByName(DotName.createSimple(queryClassName));
    if (ci == null) {
      return null;
    }
    return new JandexClassInfo(ci);
  }

  public IClassInfo getClassInfo(Class<?> queryClass) {
    Assertions.assertNotNull(queryClass);
    ClassInfo ci = m_index.getClassByName(DotName.createSimple(queryClass.getName()));
    if (ci == null) {
      return null;
    }
    return new JandexClassInfo(ci);
  }

  protected Set<IClassInfo> convertClassInfos(Collection<ClassInfo> classInfos1, Collection<ClassInfo> optionalClassInfos2) {
    Set<IClassInfo> result = new HashSet<>(classInfos1.size() + (optionalClassInfos2 != null ? optionalClassInfos2.size() : 0));
    for (ClassInfo classInfo : classInfos1) {
      result.add(new JandexClassInfo(classInfo));
    }
    if (optionalClassInfos2 != null) {
      for (ClassInfo classInfo : optionalClassInfos2) {
        result.add(new JandexClassInfo(classInfo));
      }
    }
    return result;
  }

  protected Set<IClassInfo> convertAnnotationInstance(Collection<AnnotationInstance> annotationInstances) {
    Set<IClassInfo> result = new HashSet<>(annotationInstances.size());
    for (AnnotationInstance annotationInstance : annotationInstances) {
      AnnotationTarget target = annotationInstance.target();
      if (target instanceof ClassInfo) {
        result.add(new JandexClassInfo((ClassInfo) target));
      }
    }
    return result;
  }
}
