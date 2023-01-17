/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.nls;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.junit.Test;

public class NaturalCollatorProviderConcurrencyTest {

  @Test
  public void testParallel() {
    List<IFuture<Void>> futures = IntStream.range(0, 10)
        .mapToObj(i -> Jobs.schedule(NaturalCollatorProviderConcurrencyTest::run, Jobs.newInput().withName("Job " + i)))
        .collect(Collectors.toList());
    futures.forEach(IFuture::awaitDone);
  }

  private static void run() {
    List<String> names = IntStream.range(0, 1000).unordered().mapToObj(i -> "Name" + i).collect(Collectors.toCollection(ArrayList::new));
    for (int k = 0; k < 10; k++) {
      Collator collator = BEANS.get(CollatorProvider.class).getInstance();
      collator.setStrength(Collator.SECONDARY);
      names.sort(collator);
    }
    System.out.println("Done: " + IFuture.CURRENT.get().getJobInput().getName());
  }
}
