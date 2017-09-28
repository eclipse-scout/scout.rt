/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controls whether the annotated extension is registered only within the outer extension's owner class. Default is
 * <code>true</code> (also in cases the annotation is missing). <br/>
 * <b>Example:</b>
 *
 * <pre>
 * public class MyFormExtension extends AbstractFormExtension&lt;OrigForm&gt; {
 *
 *   public MyFormExtension(OrigForm owner) {
 *     super(owner);
 *   }
 *
 *   &#64;InheritOuterExtensionScope(false) // -> extension is applied to ALL AbstractStringFields (class must be declared static)
 *   &#64;InheritOuterExtensionScope(true) // -> extension is applied only to AbstractStringFields within OrigForm (same if annotation is missing)
 *   public static class MyStringFieldExtension extends AbstractStringFieldExtension&lt;AbstractStringField&gt; {
 *
 *     public MyStringFieldExtension(AbstractStringField owner) {
 *       super(owner);
 *     }
 *   }
 * }
 * </pre>
 *
 * @since 6.0
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InheritOuterExtensionScope {

  boolean value() default true;
}
