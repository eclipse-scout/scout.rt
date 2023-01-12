/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.svg.client.extension.svgfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.svg.client.extension.svgfield.SvgFieldChains.SvgFieldAppLinkActionChain;
import org.eclipse.scout.rt.svg.client.extension.svgfield.SvgFieldChains.SvgFieldClickedChain;
import org.eclipse.scout.rt.svg.client.svgfield.AbstractSvgField;
import org.eclipse.scout.rt.svg.client.svgfield.SvgFieldEvent;

public abstract class AbstractSvgFieldExtension<OWNER extends AbstractSvgField> extends AbstractFormFieldExtension<OWNER> implements ISvgFieldExtension<OWNER> {

  public AbstractSvgFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execClicked(SvgFieldClickedChain chain, SvgFieldEvent e) {
    chain.execClicked(e);
  }

  @Override
  public void execAppLinkAction(SvgFieldAppLinkActionChain chain, String ref) {
    chain.execAppLinkAction(ref);
  }
}
