/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.basic.planner;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.planner.IPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.PlannerEvent;
import org.eclipse.scout.rt.client.ui.basic.planner.Resource;
import org.eclipse.scout.rt.ui.html.json.AbstractEventFilter;

public class PlannerEventFilter extends AbstractEventFilter<PlannerEvent, PlannerEventFilterCondition> {

  private final JsonPlanner<? extends IPlanner> m_jsonPlanner;

  public PlannerEventFilter(JsonPlanner<? extends IPlanner> jsonPlanner) {
    m_jsonPlanner = jsonPlanner;
  }

  @Override
  public PlannerEvent filter(PlannerEvent event) {
    for (PlannerEventFilterCondition condition : getConditions()) {
      if (condition.getType() == event.getType()) {
        if (condition.checkResources()) {
          List<Resource> resources = new ArrayList<>(event.getResources());
          resources.removeAll(condition.getResources());
          if (resources.isEmpty()) {
            // Ignore event if no resources remain or if the event contained no resources at all
            return null;
          }
          PlannerEvent newEvent = new PlannerEvent(m_jsonPlanner.getModel(), event.getType(), resources);
          return newEvent;
        }

        // Ignore event if only type should be checked
        return null;
      }
    }
    return event;
  }
}
