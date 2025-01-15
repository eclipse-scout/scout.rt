/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.code;

import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.filter.BeanClassFilter;
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

  public CodeTypeClassInventory(Predicate<IClassInfo> filter) {
    super(filter, ICodeType.class);
  }
}
