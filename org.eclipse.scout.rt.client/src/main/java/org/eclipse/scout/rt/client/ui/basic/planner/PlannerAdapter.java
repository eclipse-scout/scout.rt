/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.planner;

import java.util.List;

/**
 * inside gui handling or in non-model threads don't use this adapter because it might reduce performance when batch
 * events are handled as single events
 */
public class PlannerAdapter implements PlannerListener {

  @Override
  public void plannerChangedBatch(List<? extends PlannerEvent> batch) {
    for (PlannerEvent event : batch) {
      plannerChanged(event);
    }
  }

  @Override
  public void plannerChanged(PlannerEvent e) {
    // NOP
  }
}
