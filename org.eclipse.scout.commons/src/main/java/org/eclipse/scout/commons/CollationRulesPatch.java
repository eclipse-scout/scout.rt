/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.Collator;

/**
 * Patch for Collator to consider space and hyphen characters while sorting strings.
 * 
 * @see Bugzilla 390097
 */
public class CollationRulesPatch {

  private CollationRulesPatch() {
  }

  public static void patchDefaultCollationRules() {
    try {
      changeDefaultCollationRules();
      clearCollatorCache();
    }
    catch (Throwable t) {
      // nop
    }
  }

  public static void revertPatchDefaultCollationRules() {
    try {
      revertDefaultCollationRules();
      clearCollatorCache();
    }
    catch (Throwable t) {
      // nop
    }
  }

  private static void changeDefaultCollationRules() throws Exception {
    Field defaultRulesField = getDefaultCollationRulesField();
    replaceDefaultCollationRules(defaultRulesField, "<'\u005f'", "<'\u0020','\u002D'<'\u005f'");
    ReflectionUtility.setFinalFlagOnField(defaultRulesField);
  }

  private static void revertDefaultCollationRules() throws Exception {
    Field defaultRulesField = getDefaultCollationRulesField();
    replaceDefaultCollationRules(defaultRulesField, "<'\u0020','\u002D'<'\u005f'", "<'\u005f'");
    ReflectionUtility.setFinalFlagOnField(defaultRulesField);
  }

  private static Field getDefaultCollationRulesField() throws Exception {
    Class collationRulesClass = Class.forName("java.text.CollationRules");
    Field defaultRulesField = collationRulesClass.getDeclaredField("DEFAULTRULES");
    ReflectionUtility.removeFinalFlagOnField(defaultRulesField);
    return defaultRulesField;
  }

  private static void replaceDefaultCollationRules(Field defaultRulesField, String oldRule, String newRule) throws Exception {
    defaultRulesField.setAccessible(true);
    String defaultRules = (String) defaultRulesField.get(null);
    if (defaultRules.contains(newRule) && !defaultRules.contains(oldRule)) {
      return;
    }
    String newRules = defaultRules.replaceAll(oldRule, newRule);
    defaultRulesField.set(null, newRules);
    defaultRulesField.setAccessible(false);
  }

  private static void clearCollatorCache() throws Exception {
    Field cacheField = getAccessibleCollatorCacheField();
    clearCollatorCacheField(cacheField);
    cacheField.setAccessible(false);
  }

  private static Field getAccessibleCollatorCacheField() throws Exception {
    Field cacheField = Collator.class.getDeclaredField("cache");
    cacheField.setAccessible(true);
    return cacheField;
  }

  private static void clearCollatorCacheField(Field cacheField) throws Exception {
    Object softCache = cacheField.get(null);
    if (softCache != null) {
      Class<?> softcacheClass = cacheField.getType();
      Method cacheClearMethod = softcacheClass.getDeclaredMethod("clear");
      cacheClearMethod.invoke(softCache);
    }
  }

}
