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
package org.eclipse.scout.rt.client.testenvironment.ui.desktop;

import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;

/**
 * {@link IDesktop} for Client Test Environment
 *
 * @author jbr
 */
public class TestEnvironmentDesktop extends AbstractDesktop {

  @Override
  protected String getConfiguredTitle() {
    return "Test Environment Application";
  }
}
