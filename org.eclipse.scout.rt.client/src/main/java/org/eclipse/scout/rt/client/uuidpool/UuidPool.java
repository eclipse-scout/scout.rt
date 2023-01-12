/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.uuidpool;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;

public class UuidPool extends AbstractPropertyObserver implements IUuidPool {

  public UuidPool() {
    this(true);
  }

  public UuidPool(boolean callInitializer) {
    if (callInitializer) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    initConfig();
  }

  protected void initConfig() {
    setSize(getConfiguredSize());
    setRefillThreshold(getConfiguredRefillThreshold());
    setFailOnStarvation(getConfiguredFailOnStarvation());
  }

  protected int getConfiguredSize() {
    return 100;
  }

  protected int getConfiguredRefillThreshold() {
    return 25;
  }

  protected boolean getConfiguredFailOnStarvation() {
    return false;
  }

  @Override
  public int getSize() {
    return propertySupport.getPropertyInt(PROP_SIZE);
  }

  @Override
  public void setSize(int size) {
    propertySupport.setPropertyInt(PROP_SIZE, size);
  }

  @Override
  public int getRefillThreshold() {
    return propertySupport.getPropertyInt(PROP_REFILL_THRESHOLD);
  }

  @Override
  public void setRefillThreshold(int refillThreshold) {
    propertySupport.setPropertyInt(PROP_REFILL_THRESHOLD, refillThreshold);
  }

  @Override
  public boolean isFailOnStarvation() {
    return propertySupport.getPropertyBool(PROP_FAIL_ON_STARVATION);
  }

  @Override
  public void setFailOnStarvation(boolean failOnStarvation) {
    propertySupport.setPropertyBool(PROP_FAIL_ON_STARVATION, failOnStarvation);
  }

  @Override
  public List<UUID> generateUuids(int count) {
    return IntStream.range(0, count)
        .mapToObj(i -> UUID.randomUUID())
        .collect(Collectors.toList());
  }
}
