/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.migration.ecma6.model.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.platform.util.StringUtility;

import com.fasterxml.jackson.databind.util.StdConverter;

public class ChildrenJsonSerializer extends StdConverter<List<INamedElement>, List<INamedElement>> {
  @Override
  public List<INamedElement> convert(List<INamedElement> value) {
    List<INamedElement> writeableValue = new ArrayList<>(value);
     Collections.sort(writeableValue, (o1, o2) -> StringUtility.compare(o1.getFullyQualifiedName(), o2.getFullyQualifiedName()));
     return writeableValue;
  }
}
