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
package org.eclipse.scout.rt.platform.classid;

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
  private final int m_hash;

  public ClassIdentifier(Class<?>... segments) {
    this(null, segments);
  }

  public ClassIdentifier(ClassIdentifier context, Class<?>... segments) {
    if (context == null && (segments == null || segments.length == 0)) {
      throw new IllegalArgumentException("The given context and classes array must not be null or empty");
    }
    final int contextLength = context == null ? 0 : context.m_segments.length;
    final int segmentsLength = segments == null ? 0 : segments.length;
    m_segments = new Class<?>[contextLength + segmentsLength];
    if (context != null) {
      System.arraycopy(context.m_segments, 0, m_segments, 0, contextLength);
    }

    if (segments != null) {
      for (Class<?> segment : segments) {
        if (segment == null) {
          throw new IllegalArgumentException("null segments are not allowed.");
        }
      }
      System.arraycopy(segments, 0, m_segments, contextLength, segmentsLength);
    }

    m_hash = Arrays.hashCode(m_segments);
  }

  /**
   * @return Returns the array of segments represented by this class identifier.
   */
  public Class<?>[] getClasses() {
    return Arrays.copyOf(m_segments, m_segments.length);
  }

  /**
   * @return Returns the last segment of this class identifier.
   */
  public Class<?> getLastSegment() {
    return m_segments[m_segments.length - 1];
  }

  public Class<?> getSegment(int i) {
    if (i < 0 || i >= m_segments.length) {
      throw new IllegalArgumentException("index out of bounds");
    }
    return m_segments[i];
  }

  public int size() {
    return m_segments.length;
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
    ArrayList<ClassIdentifier> result = new ArrayList<>();
    for (Class<?> c : classes) {
      if (c != null) {
        result.add(new ClassIdentifier(c));
      }
    }
    return result.toArray(new ClassIdentifier[result.size()]);
  }

  @Override
  public int hashCode() {
    return m_hash;
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
    return Arrays.equals(m_segments, other.m_segments);
  }

  @Override
  public String toString() {
    return "ClassIdentifier [" + Arrays.toString(m_segments) + "]";
  }
}
