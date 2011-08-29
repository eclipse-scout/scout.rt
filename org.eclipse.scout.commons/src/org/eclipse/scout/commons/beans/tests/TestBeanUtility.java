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
package org.eclipse.scout.commons.beans.tests;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.TreeSet;

import org.eclipse.scout.commons.BeanUtility;
import org.eclipse.scout.commons.TuningUtility;
import org.eclipse.scout.commons.beans.FastBeanInfo;
import org.eclipse.scout.commons.beans.FastPropertyDescriptor;

/**
 *
 */
public class TestBeanUtility {

  public static void main(String[] args) {
    for (int i = 1; i <= 5; i++) {
      System.out.println("pass " + i);
      new TestBeanUtility().testBeanInfo();
      TuningUtility.finishAll();
    }
  }

  public void testBeanInfo() {
    /*
    testClassTree(CompanyForm.class, null);
    testClassTree(CompanyForm.class, AbstractForm.class);
    testClassTree(CompanyForm.class, AbstractFormField.class);
    testClassTree(CompanyFormData.class, null);
    testClassTree(CompanyFormData.class, AbstractFormData.class);
    testClassTree(CompanyFormData.class, AbstractFormFieldData.class);
    //
    testClassTree(PersonForm.class, null);
    testClassTree(PersonForm.class, AbstractForm.class);
    testClassTree(PersonForm.class, AbstractFormField.class);
    testClassTree(PersonFormData.class, null);
    testClassTree(PersonFormData.class, AbstractFormData.class);
    testClassTree(PersonFormData.class, AbstractFormFieldData.class);
    //
    testClassTree(ProjectForm.class, null);
    testClassTree(ProjectForm.class, AbstractForm.class);
    testClassTree(ProjectForm.class, AbstractFormField.class);
    testClassTree(ProjectFormData.class, null);
    testClassTree(ProjectFormData.class, AbstractFormData.class);
    testClassTree(ProjectFormData.class, AbstractFormFieldData.class);
    */
  }

  protected void testClassTree(Class<?> c, Class<?> stopClass) {
    testClass(c, stopClass);
    for (Class<?> sub : c.getClasses()) {
      testClassTree(sub, stopClass);
    }
  }

  protected void testClass(Class<?> c, Class<?> stopClass) {
    if (stopClass != null && !stopClass.isAssignableFrom(c)) {
      return;
    }
    BeanInfo info1;
    try {
      TuningUtility.startTimer();
      info1 = Introspector.getBeanInfo(c, stopClass);
      TuningUtility.stopTimer("sun", false, true);
    }
    catch (IntrospectionException e) {
      System.out.println("ignoring " + c.getName() + " with stop " + stopClass + ": " + e);
      return;
    }
    TuningUtility.startTimer();
    FastBeanInfo info2 = BeanUtility.getFastBeanInfo(c, stopClass);
    TuningUtility.stopTimer("bsi", false, true);
    //
    StringBuilder b1 = new StringBuilder();
    b1.append("Class: " + info1.getBeanDescriptor().getBeanClass() + "\n");
    int count1 = 0;
    TreeSet<String> set1 = new TreeSet<String>();
    for (PropertyDescriptor d : info1.getPropertyDescriptors()) {
      TuningUtility.startTimer();
      d.getName();
      d.getPropertyType();
      d.getReadMethod();
      d.getWriteMethod();
      TuningUtility.stopTimer("sun", false, true);
      //ignore indexed properties
      if (d.getReadMethod() != null || d.getWriteMethod() != null) {
        count1++;
        String s = d.getName() + ";" + d.getPropertyType() + ";" + dumpMethod(d.getReadMethod()) + ";" + dumpMethod(d.getWriteMethod());
        set1.add(s);
      }
    }
    b1.append("Count: " + count1 + "\n");
    for (String s : set1) {
      b1.append("Property: " + s + "\n");
    }
    //
    StringBuilder b2 = new StringBuilder();
    b2.append("Class: " + info2.getBeanClass() + "\n");
    int count2 = 0;
    TreeSet<String> set2 = new TreeSet<String>();
    for (FastPropertyDescriptor d : info2.getPropertyDescriptors()) {
      TuningUtility.startTimer();
      d.getName();
      d.getPropertyType();
      d.getReadMethod();
      d.getWriteMethod();
      TuningUtility.stopTimer("bsi", false, true);
      count2++;
      String s = d.getName() + ";" + d.getPropertyType() + ";" + dumpMethod(d.getReadMethod()) + ";" + dumpMethod(d.getWriteMethod());
      set2.add(s);
    }
    b2.append("Count: " + count2 + "\n");
    for (String s : set2) {
      b2.append("Property: " + s + "\n");
    }
    //
    if (!b1.toString().equals(b2.toString())) {
      System.out.println("************ ERROR *************");
      System.out.println(info1.getBeanDescriptor().getBeanClass());
      System.out.println(info2.getBeanClass());
      System.out.println(count1 + "==" + count2);
      TreeSet<String> tmp = new TreeSet<String>(set1);
      set1.removeAll(set2);
      set2.removeAll(tmp);
      for (String s : set1) {
        System.out.println("Set1: " + s);
      }
      for (String s : set2) {
        System.out.println("Set2: " + s);
      }
    }
  }

  private static String dumpMethod(Method m) {
    if (m == null) {
      return null;
    }
    return m.getReturnType() + " " + m.getName() + Arrays.asList(m.getParameterTypes());
  }
}
