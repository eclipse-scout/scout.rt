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
package org.eclipse.scout.rt.platform;

/**
 * Exception thrown in case of any error during the construction of a bean.
 */
public class BeanCreationException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public BeanCreationException(String beanName, String message) {
    this(beanName, message, null);
  }

  public BeanCreationException(String beanName, Throwable t) {
    this(beanName, null, t);
  }

  public BeanCreationException(String beanName, String message, Throwable t) {
    super(String.format("Error creating bean '%s'. %s", beanName, message), t);
  }
}
