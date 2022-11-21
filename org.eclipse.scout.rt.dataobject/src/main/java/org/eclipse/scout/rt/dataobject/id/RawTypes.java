/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.id;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to mark the static {@code of()} method used to create instances of an {@link IId} out of its raw
 * types.
 * <p>
 * Annotating the corresponding {@code of()} method is optional for {@link IRootId} implementations since the
 * corresponding '{@code of()} method is automatically determined according to the generic type of the concrete id
 * implementation. This annotation is mandatory for {@link ICompositeId} implementations since the correct {@code of()}
 * method may not be determined automatically.
 * <p>
 * Example:
 *
 * <pre>
 * public class ExampleId extends AbstractCompositeId  {
 *
 * &#64;RawTypes
 * public static ExampleId of(UUID uuid, String id) {
 *   if (uuid == null && StringUtility.isNullOrEmpty(null)) {
 *     return null;
 *   }
 *   return new ExampleId(uuid, id);
 * }
 *   ...
 * }
 * </pre>
 *
 * @see IId
 * @see IRootId
 * @see ICompositeId
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD})
public @interface RawTypes {
}
