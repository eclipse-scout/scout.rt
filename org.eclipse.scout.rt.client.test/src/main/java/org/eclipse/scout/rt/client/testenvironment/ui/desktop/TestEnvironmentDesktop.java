/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
