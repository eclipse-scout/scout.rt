/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.context;

/**
 * <h3>{@link AbstractRunContextChainInterceptor}</h3>
 *
 * @author Andreas Hoegger
 */
public abstract class AbstractRunContextChainInterceptor<RESULT> implements IRunContextChainInterceptor<RESULT> {

  @Override
  public void fillCurrent() {
  }

  @Override
  public void fillEmtpy() {
  }
}
