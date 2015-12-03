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
package org.eclipse.scout.rt.svg.client;

import org.apache.batik.swing.svg.SVGUserAgentAdapter;

public class SilentSVGUserAgentAdapter extends SVGUserAgentAdapter {
  @Override
  public void displayMessage(String message) {
  }

  @Override
  public void displayError(String message) {
  }

  @Override
  public void displayError(Exception ex) {
  }

  @Override
  public void showAlert(String message) {
  }

  @Override
  public String showPrompt(String message) {
    return null;
  }

  @Override
  public String showPrompt(String message, String defaultValue) {
    return null;
  }
}
