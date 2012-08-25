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
package org.eclipse.scout.service;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Rule used by default for immediate service creation of registered osgi service.
 */
public class CreateServiceImmediatelySchedulingRule implements ISchedulingRule {

  @Override
  public boolean contains(ISchedulingRule rule) {
    return rule == this;
  }

  @Override
  public boolean isConflicting(ISchedulingRule rule) {
    return this.equals(rule);
  }

  @Override
  public int hashCode() {
    return getClass().getName().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    return obj.getClass() == getClass();
  }
}
