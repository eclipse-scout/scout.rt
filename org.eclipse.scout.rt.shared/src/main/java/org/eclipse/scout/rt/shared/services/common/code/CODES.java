/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Convenience accessor for service ICodeService
 */
public final class CODES {

  private CODES() {
  }

  /**
   * @param id
   * @return Note that this method does not load code types, but only searches code types already loaded into the code
   *         service using {@link #getAllCodeTypes(String)}, {@link #getCodeType(Class)} etc.
   */
  public static <T> ICodeType<T, ?> findCodeTypeById(T id) {
    return BEANS.get(ICodeService.class).findCodeTypeById(id);
  }

  public static List<ICodeType<?, ?>> getCodeTypes(Class<?>... types) {
    List<Class<? extends ICodeType<?, ?>>> typeList = varargToList(types);
    return BEANS.get(ICodeService.class).getCodeTypes(typeList);
  }

  public static List<ICodeType<?, ?>> getCodeTypes(List<Class<? extends ICodeType<?, ?>>> types) {
    return BEANS.get(ICodeService.class).getCodeTypes(types);
  }

  public static <CODE_ID_TYPE, CODE extends ICode<CODE_ID_TYPE>> CODE getCode(Class<CODE> type) {
    return BEANS.get(ICodeService.class).getCode(type);
  }

  public static <T extends ICodeType> T reloadCodeType(Class<T> type) {
    return BEANS.get(ICodeService.class).reloadCodeType(type);
  }

  public static List<ICodeType<?, ?>> reloadCodeTypes(Class<?>... types) {
    List<Class<? extends ICodeType<?, ?>>> typeList = varargToList(types);
    return BEANS.get(ICodeService.class).reloadCodeTypes(typeList);
  }

  public static List<ICodeType<?, ?>> reloadCodeTypes(List<Class<? extends ICodeType<?, ?>>> types) {
    return BEANS.get(ICodeService.class).reloadCodeTypes(types);
  }

  public static void invalidateCodeType(Class<? extends ICodeType> type) {
    BEANS.get(ICodeService.class).invalidateCodeType(type);
  }

  public static void invalidateCodeTypes(Class<?>... types) {
    List<Class<? extends ICodeType<?, ?>>> typeList = varargToList(types);
    BEANS.get(ICodeService.class).invalidateCodeTypes(typeList);
  }

  public static void invalidateCodeTypes(List<Class<? extends ICodeType<?, ?>>> types) {
    BEANS.get(ICodeService.class).invalidateCodeTypes(types);
  }

  /**
   * Converts the given classes vararg into a {@link List}.
   */
  @SuppressWarnings("unchecked")
  private static List<Class<? extends ICodeType<?, ?>>> varargToList(Class<?>... types) {
    if (types == null) {
      return CollectionUtility.emptyArrayList();
    }
    List<Class<? extends ICodeType<?, ?>>> typeList = new ArrayList<>(types.length);
    for (Class<?> t : types) {
      if (ICodeType.class.isAssignableFrom(t)) {
        typeList.add((Class<? extends ICodeType<?, ?>>) t);
      }
    }
    return typeList;
  }

  public static Collection<ICodeType<?, ?>> getAllCodeTypes(String classPrefix) {
    Set<Class<? extends ICodeType<?, ?>>> allCodeTypeClasses = getAllCodeTypeClasses(classPrefix);
    List<Class<? extends ICodeType<?, ?>>> list = CollectionUtility.arrayList(allCodeTypeClasses);
    return getCodeTypes(list);
  }

  public static Set<Class<? extends ICodeType<?, ?>>> getAllCodeTypeClasses(String classPrefix) {
    final Set<Class<? extends ICodeType<?, ?>>> filteredClasses = new LinkedHashSet<>();
    final Collection<Class<? extends ICodeType<?, ?>>> classes = BEANS.get(ICodeService.class).getAllCodeTypeClasses();
    for (Class<? extends ICodeType<?, ?>> c : classes) {
      if (c.getName().startsWith(classPrefix)) {
        filteredClasses.add(c);
      }
    }
    return filteredClasses;
  }
}
