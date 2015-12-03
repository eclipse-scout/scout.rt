/*******************************************************************************
 * Copyright (C) 2005-2010 The Android Open Source Project
 * Copyright (c) 2015 BSI Business Systems Integration AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     The Android Open Source Project - initial implementation
 *     BSI Business Systems Integration AG - changes and improvements
 ******************************************************************************/
package org.json;

// Note: this class was written without inspecting the non-free org.json sourcecode.

/*
 * Changes to the original code:
 * -----------------------------
 * - Applied Scout code formatting rules
 * - JSONException extends "RuntimeException" instead of "Exception". JavaDoc was
 *   updated accordingly (removed obsolete paragraph).
 *
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 */

/**
 * Thrown to indicate a problem with the JSON API. Such problems include:
 * <ul>
 * <li>Attempts to parse or construct malformed documents
 * <li>Use of null as a name
 * <li>Use of numeric types not available to JSON, such as {@link Double#isNaN() NaNs} or {@link Double#isInfinite()
 * infinities}.
 * <li>Lookups using an out of range index or nonexistent name
 * <li>Type mismatches on lookups
 * </ul>
 */
public class JSONException extends RuntimeException { // BSI
  private static final long serialVersionUID = 1L;

  public JSONException(String s) {
    super(s);
  }
}
