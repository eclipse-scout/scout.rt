/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.jms;

import java.util.Map;

import org.eclipse.scout.rt.mom.api.IMomImplementor;
import org.eclipse.scout.rt.testing.platform.runner.parameterized.AbstractScoutTestParameter;

public abstract class AbstractJmsMomTestParameter extends AbstractScoutTestParameter {

  public AbstractJmsMomTestParameter(String name) {
    super(name);
  }

  public abstract Class<? extends IMomImplementor> getImplementor();

  public abstract Map<String, String> getEnvironment();
}
