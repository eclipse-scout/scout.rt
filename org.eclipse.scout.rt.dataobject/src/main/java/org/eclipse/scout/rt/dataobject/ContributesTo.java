/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for DO entity contributions
 *
 * @see IDoEntityContribution
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ContributesTo {

  /**
   * Concrete DO entity classes as well as interfaces or abstract class are supported as container classes.
   * <p>
   * It's not recommended to defined {@link IDoEntity} or {@link DoEntity} as a container class.
   */
  Class<? extends IDoEntity>[] value();
}
