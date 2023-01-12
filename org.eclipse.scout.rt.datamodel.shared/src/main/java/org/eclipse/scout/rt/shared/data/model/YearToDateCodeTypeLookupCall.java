/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.model;

import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ICodeLookupCallVisitor;

@ClassId("23001b0f-e866-42c8-9ef6-a7065408f441")
public class YearToDateCodeTypeLookupCall extends CodeLookupCall<Integer> {
  private static final long serialVersionUID = 1L;

  /**
   * Visitor class that filters out inactive codes.
   */
  class LookupVisitor implements ICodeLookupCallVisitor<Integer> {

    @Override
    public boolean visit(CodeLookupCall<Integer> call, ICode<Integer> code, int treeLevel) {
      return code.isActive();
    }
  }

  /**
   * Default constructor that wires this lookup call to the event type code type.
   */
  public YearToDateCodeTypeLookupCall() {
    super(YearToDateCodeType.class);
    setFilter(new LookupVisitor());
  }
}
