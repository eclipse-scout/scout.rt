/*******************************************************************************
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.dataobject.enumeration;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * Values backed by a java enum should implement this interface so that the enum's string representation does not depend
 * on volatile enum properties (i.e. ordinal or enum constant name).
 * <p>
 * <b>Important:</b> The {@link EnumResolver} helps resolving string values by either using a custom <code>static
 * &lt;actual IEnum&gt; resolve(String) </code> method available on implementors of this interface or by using
 * reflection. The custom resolve method allows customizing the handling of <code>null</code>, <i>old</i> as well as
 * <i>unknown</i> values. The reflective method returns <code>null</code> for <code>null</code> and throws an
 * {@link AssertionException} for unknown values.
 * <p>
 * <b>Example:</b>
 *
 * <pre>
 * import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
 *
 * public enum ExampleEnum implements IEnum {
 *
 *   CURRENT("current"),
 *   OTHER("other");
 *
 *   private final String m_stringValue;
 *
 *   private ExampleEnum(String stringValue) {
 *     m_stringValue = stringValue;
 *   }
 *
 *   &#64;Override
 *   public String stringValue() {
 *     return m_stringValue;
 *   }
 *
 *   public static final ExampleEnum resolve(String value) {
 *     // custom null handling
 *     if (value == null) {
 *       return null;
 *     }
 *     switch (value) {
 *       // custom handling of old values (assuming 'old' was used in earlier revisions)
 *       case "old":
 *       case "current":
 *         return CURRENT;
 *       case "other":
 *         return OTHER;
 *       default:
 *         // custom handling of unknown values
 *         throw new AssertionException("unsupported status value '{}'", value);
 *     }
 *   }
 * }
 *
 * </pre>
 */
public interface IEnum {

  /**
   * @return a stable string value that does not depend on the enum's ordinal value or enum constant.
   */
  String stringValue();

  /**
   * @return optional text used for looking up a human readable display text.
   */
  default String text() {
    return null;
  }
}
