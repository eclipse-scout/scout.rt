/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.extension;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.extension.AbstractContainerValidationService;

@Order(5200)
public class DataModelClientContainerValidationService extends AbstractContainerValidationService {

  @PostConstruct
  protected void initializeContributions() {
    // contributions
    addPossibleContributionForContainer(ITree.class, AbstractComposerField.class);
  }
}
