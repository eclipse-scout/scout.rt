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
  }
}
