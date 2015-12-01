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
package org.eclipse.scout.rt.platform.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation on a scout form field used to inject the field into another container.
 * <p>
 * The container may be another field inside the same form or a container in the super classes form fields.
 * <p>
 * The example adds a salary field to the existing BaseForm by injection in an extension form (simplified):
 * 
 * <pre>
 * public class BaseForm {
 *   &#064;Order(10)
 *   public class MainBox extends AbstractGroupBox {
 *     &#064;Order(10)
 *     public class FirstGroupBox extends AbstractGroupBox {
 *       &#064;Order(10)
 *       public class NameField extends AbstractStringField {
 *       }
 *     }
 *   }
 * }
 * 
 * public class ExtendedForm extends BaseForm {
 *   &#064;Order(20)
 *   &#064;InjectFieldTo(BaseForm.MainBox.FirstGroupBox.class)
 *   public class SalaryField extends AbstractDoubleField {
 *   }
 * }
 * </pre>
 * 
 * @since 3.9
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InjectFieldTo {
  Class value() default Object.class;
}
