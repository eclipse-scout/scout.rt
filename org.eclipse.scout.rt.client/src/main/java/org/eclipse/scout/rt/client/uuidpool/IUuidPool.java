/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.uuidpool;

import java.util.List;
import java.util.UUID;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Representation of a pool of UUIDs for the UI.
 */
@Bean
public interface IUuidPool {

  String PROP_SIZE = "size";
  String PROP_REFILL_THRESHOLD = "refillThreshold";
  String PROP_FAIL_ON_STARVATION = "failOnStarvation";

  int getSize();

  void setSize(int size);

  int getRefillThreshold();

  void setRefillThreshold(int refillThreshold);

  boolean isFailOnStarvation();

  void setFailOnStarvation(boolean failOnStarvation);

  List<UUID> generateUuids(int count);
}
