/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.logger.internal.slf4j;

import java.io.File;

import org.eclipse.scout.commons.logger.IScoutLogManager;
import org.eclipse.scout.commons.logger.IScoutLogger;

public class Slf4jScoutLogManager implements IScoutLogManager {

  @Override
  public void initialize() {
  }

  @Override
  public IScoutLogger getLogger(String name) {
    return new Slf4jLogWrapper(name);
  }

  @Override
  public IScoutLogger getLogger(Class clazz) {
    return getLogger(clazz.getName());
  }

  @Override
  public void setGlobalLogLevel(Integer globalLogLevel) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Integer getGlobalLogLevel() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean startRecording() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public File stopRecording() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
}
