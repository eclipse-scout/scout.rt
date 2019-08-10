/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.testenvironment.ui.desktop;

import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * {@link IDesktop} for Client Test Environment
 *
 * @author jbr
 */
@ClassId("3240b80d-d6ef-461e-a625-6d42cc715471")
public class TestEnvironmentDesktop extends AbstractDesktop {

  @Override
  protected String getConfiguredTitle() {
    return "Test Environment Application";
  }
}
