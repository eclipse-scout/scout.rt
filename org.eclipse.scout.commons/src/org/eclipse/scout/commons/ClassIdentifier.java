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
package org.eclipse.scout.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A class identifier holds the static information of how a class is embedded within other classes. This information is
 * especially required to identify extended template classes.
 * <p>
 * <b>Example</b>:
 * 
 * <pre>
 * public abstract class Template {
 *   public class InnerClass {
 *   }
 * }
 * 
 * public class Foo {
 *   public class A extends Template {
 *   }
 * 
 *   public class B extends Template {
 *   }
 * }
 * </pre>
 * 
 * A class identifier distinguishes between the <code>InnerClass</code> used within <code>Foo.A</code> and the one used
 * within <code>Foo.B</code>. I.e <code>new ClassIdentifier(Foo.A.class, Foo.A.InnerClass.class)</code> is not the same
 * as <code>new ClassIdentifier(Foo.B.class, Foo.B.InnerClass.class)</code>, whereas
 * <code>Foo.A.InnerClass.class == Foo.B.InnerClass.class</code>. Therefore,
 * <code>new InnerClass(Foo.A.class, Foo.A.InnerClass.class)</code> identifies the same class as
 * <code>new InnerClass(Foo.A.class, Template.InnerClass.class)</code>.
 */
public class ClassIdentifier implements Serializable {
  private static final long serialVersionUID = 1L;

  private final Class<?>[] m_segments;

  public ClassIdentifier(Class<?>... segments) throws IllegalArgumentException {
    if (segments == null || segments.length == 0) {
      throw new IllegalArgumentException("The given classes array must not be null or empty");
    }
    m_segments = segments;
  }

  /**
   * @return Returns the array of segments represented by this class identifier.
   */
  public Class<?>[] getClasses() {
    return m_segments;
  }

  /**
   * @return Returns the last segment of this class identifier.
   */
  public Class<?> getLastSegment() {
    return m_segments[m_segments.length - 1];
  }

  /**
   * Converts the given array of classes into an array of {@link ClassIdentifier}s. The method returns always a non-null
   * result. Null entries in the given class array are not transformed into a class identifier.
   * 
   * @param classes
   * @return
   */
  public static ClassIdentifier[] convertClassArrayToClassIdentifierArray(Class<?>... classes) {
    if (classes == null || classes.length == 0) {
      return new ClassIdentifier[0];
    }
    ArrayList<ClassIdentifier> result = new ArrayList<ClassIdentifier>();
    for (Class<?> c : classes) {
      if (c != null) {
        result.add(new ClassIdentifier(c));
      }
    }
    return result.toArray(new ClassIdentifier[result.size()]);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(m_segments);
    return result;
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
    ClassIdentifier other = (ClassIdentifier) obj;
    if (!Arrays.equals(m_segments, other.m_segments)) {
      return false;
    }
    return true;
  }
}
