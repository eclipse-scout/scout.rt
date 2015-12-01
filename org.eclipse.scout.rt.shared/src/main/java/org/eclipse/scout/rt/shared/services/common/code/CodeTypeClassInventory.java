/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.code;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.filter.BeanClassFilter;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.inventory.FilteredClassInventory;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;

/**
 * Inventory service for code types classes for registration. Uses jandex class inventory to find classes. *
 * <p>
 * By default all direct subclasses of {@link ICodeType} are scanned. Make sure your code type classes are available in
 * the {@link ClassInventory}.
 * </p>
 */
@ApplicationScoped
public class CodeTypeClassInventory extends FilteredClassInventory<ICodeType<?, ?>> {

  public CodeTypeClassInventory() {
    this(new BeanClassFilter());
  }

  public CodeTypeClassInventory(IFilter<IClassInfo> filter) {
    super(filter, ICodeType.class);
  }

}
