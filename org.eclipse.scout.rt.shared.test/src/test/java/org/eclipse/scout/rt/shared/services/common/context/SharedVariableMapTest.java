/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.context;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.junit.Test;

public class SharedVariableMapTest {

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateInternal() {
    List<Map<String, Object>> protocol = new ArrayList<>();
    SharedVariableMap m = new SharedVariableMap();
    m.put("1", 1L);
    m.put("2", 2L);
    assertEquals(2, m.size());

    PropertyChangeListener propertyChangeListener = e -> protocol.add((Map<String, Object>) e.getNewValue());
    m.addPropertyChangeListener(propertyChangeListener);
    m.put("3", 3L);
    m.remove("1");
    m.updateInternal(Map.of("11", 11L, "22", 22L, "33", 33L));
    m.updateInternal(Map.of("33", 33L, "11", 11L, "22", 22L)); // this update is ignored (no changes)

    assertEquals(3, m.size());
    assertEquals(11L, m.get("11"));
    assertEquals(22L, m.get("22"));
    assertEquals(33L, m.get("33"));

    assertEquals(3, protocol.size());
    assertEquals(Map.of("1", 1L, "2", 2L, "3", 3L), protocol.get(0));
    assertEquals(Map.of("2", 2L, "3", 3L), protocol.get(1));
    assertEquals(Map.of("11", 11L, "22", 22L, "33", 33L), protocol.get(2));
    m.removePropertyChangeListener(propertyChangeListener);
  }

  @Test
  public void testConcurrentAccess() {
    final SharedVariableMap m = new SharedVariableMap();
    final List<IFuture<Void>> jobs = new ArrayList<>();
    final int numberOfValues = 1000;
    final int numberOfJobs = 10;

    // Phase 1: Concurrent put and get
    for (int i = 0; i < numberOfJobs; i++) {
      jobs.add(schedulePutAndGetJob(m, i, numberOfValues));
    }
    jobs.forEach(IFuture::awaitDoneAndGet);
    assertEquals(numberOfJobs * numberOfValues, m.size());

    // Phase 2: Concurrent remove and get
    jobs.clear();
    for (int i = 0; i < numberOfJobs; i++) {
      jobs.add(scheduleRemoveAndGetJob(m, i, numberOfValues));
    }
    jobs.forEach(IFuture::awaitDoneAndGet);
    assertEquals(0, m.size());
  }

  protected IFuture<Void> schedulePutAndGetJob(SharedVariableMap m, int jobNumber, int numberOfValues) {
    return Jobs.schedule(() -> {
      for (int i = jobNumber * numberOfValues; i < (jobNumber + 1) * numberOfValues; i++) {
        m.put(String.valueOf(i), i);
        if (i % 5 == 0) {
          m.get(String.valueOf(i)); // Occasionally read a key to test concurrent gets
        }
      }
    }, Jobs.newInput().withName("put-get-job-" + jobNumber).withRunContext(RunContexts.empty()));
  }

  protected IFuture<Void> scheduleRemoveAndGetJob(SharedVariableMap m, int jobNumber, int numberOfValues) {
    return Jobs.schedule(() -> {
      for (int i = jobNumber * numberOfValues; i < (jobNumber + 1) * numberOfValues; i++) {
        if (i % 5 == 0) {
          m.get(String.valueOf(i)); // Occasionally read a key to test concurrent gets
        }
        m.remove(String.valueOf(i));
      }
    }, Jobs.newInput().withName("remove-get-job-" + jobNumber).withRunContext(RunContexts.empty()));
  }
}
