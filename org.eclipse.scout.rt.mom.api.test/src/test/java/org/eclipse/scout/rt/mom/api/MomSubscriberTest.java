/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.api;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.junit.Test;

public class MomSubscriberTest {

  @Test
  public void testDispose() {
    ISubscription subscriptionMock = mock(ISubscription.class);
    AbstractMomSubscriber subscriber = new AbstractMomSubscriber() {
      @Override
      public void subscribe() {
        registerSubscription(subscriptionMock);
      }
    };
    subscriber.subscribe();
    subscriber.dispose();
    verify(subscriptionMock, times(1)).dispose();
  }

  @Test
  public void testConcurrentSubscriptions() {
    List<ISubscription> subscriptionMocks = Stream.generate(() -> mock(ISubscription.class))
        .limit(10000)
        .collect(Collectors.toList());

    AbstractMomSubscriber subscriber = new AbstractMomSubscriber() {
      @Override
      public void subscribe() {
        subscriptionMocks.stream().parallel().forEach(this::registerSubscription);
      }
    };

    subscriber.subscribe();
    subscriber.dispose();
    for (ISubscription subscriptionMock : subscriptionMocks) {
      verify(subscriptionMock, times(1)).dispose();
    }
  }

  @Test
  public void testConcurrentModification() {
    List<ISubscription> subscriptionMocks = Stream.generate(() -> mock(ISubscription.class))
        .limit(100_000)
        .collect(Collectors.toList());

    AbstractMomSubscriber subscriber = new AbstractMomSubscriber() {
      @Override
      public void subscribe() {
        for (ISubscription subscriptionMock : subscriptionMocks) {
          registerSubscription(subscriptionMock);
        }
      }
    };

    subscriber.subscribe(); // insert initial subscriptions
    IFuture<Void> subscriptionFuture = Jobs.schedule(subscriber::subscribe, Jobs.newInput()); // insert more subscriptions async
    subscriber.dispose(); // start disposing while new subscriptions are being added
    subscriptionFuture.awaitDone();
  }
}
