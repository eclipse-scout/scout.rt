/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
