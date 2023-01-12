/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.svg.client;

import org.apache.batik.swing.svg.SVGUserAgentAdapter;

@SuppressWarnings("squid:S1186")
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
