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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;

/**
 * Inventory service for code types classes for registration. Uses jandex class inventory to find classes.
 * *
 * <p>
 * By default all direct subclasses of {@link ICodeType} are scanned. Make sure your code type classes are available in
 * the {@link ClassInventory}.
 * </p>
 */
@ApplicationScoped
public class CodeTypeClassInventory {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSharedCodeService.class);
  private final IFilter<IClassInfo> m_filter;

  public CodeTypeClassInventory() {
    this(BEANS.get(CodeTypeClassFilter.class));
  }

  public CodeTypeClassInventory(IFilter<IClassInfo> filter) {
    m_filter = filter;
  }

  /**
   * All code type classes in the jandex {@link ClassInventory} that are instanciable and not ignored with
   * {@link IgnoreBean}.
   */
  public Set<Class<? extends ICodeType<?, ?>>> getClasses() {
    Set<IClassInfo> allKnownCodeTypes = findClasses();
    Set<Class<? extends ICodeType<?, ?>>> discoveredCodeTypes = new HashSet<>(allKnownCodeTypes.size());
    for (IClassInfo codeTypeInfo : allKnownCodeTypes) {
      if (m_filter.accept(codeTypeInfo)) {
        try {
          @SuppressWarnings("unchecked")
          Class<? extends ICodeType<?, ?>> codeTypeClass = (Class<? extends ICodeType<?, ?>>) codeTypeInfo.resolveClass();
          discoveredCodeTypes.add(codeTypeClass);
        }
        catch (Exception e) {
          LOG.error("Error loading code type class", e);
        }
      }
    }
    return CollectionUtility.hashSet(discoveredCodeTypes);
  }

  /**
   * @return classes in inventory
   */
  protected Set<IClassInfo> findClasses() {
    return ClassInventory.get().getAllKnownSubClasses(ICodeType.class);
  }

}
