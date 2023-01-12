/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.ui.html.json.AbstractEventFilter;

@Bean
public class DesktopEventFilter extends AbstractEventFilter<DesktopEvent, DesktopEventFilterCondition> {

  @Override
  public DesktopEvent filter(DesktopEvent event) {
    for (DesktopEventFilterCondition condition : getConditions()) {
      if (condition.getType() == event.getType()) {
        if (ObjectUtility.equals(event.getForm(), condition.getForm())) {
          return null; // Ignore event
        }
        if (condition.isCheckDisplayParents() && checkIfEventOnParent(condition.getForm(), event)) {
          return null; // Ignore event
        }
      }
    }
    return event;
  }

  protected boolean checkIfEventOnParent(IForm form, DesktopEvent event) {
    if (ObjectUtility.equals(event.getForm(), form)) {
      return true;
    }
    else if (form.getDisplayParent() instanceof IForm) {
      return checkIfEventOnParent((IForm) form.getDisplayParent(), event);
    }
    return false;
  }
}
