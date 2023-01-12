/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.ui.webresource;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Test;

public class ScriptRequestTest {

  @Test
  public void testTryParseWithWrongInput() {
    assertFalse(ScriptRequest.tryParse(null).isPresent());
    assertFalse(ScriptRequest.tryParse("").isPresent());
    assertFalse(ScriptRequest.tryParse(" ").isPresent());
    assertFalse(ScriptRequest.tryParse(" \t ").isPresent());
    assertFalse(ScriptRequest.tryParse("path/basename-34fce3bc.min.jpg").isPresent());
    assertFalse(ScriptRequest.tryParse("path/basename-34fce3bc.jpg").isPresent());
    assertFalse(ScriptRequest.tryParse("path/basename.jpg").isPresent());
    assertFalse(ScriptRequest.tryParse("path/basename.jpg").isPresent());
    assertFalse(ScriptRequest.tryParse("basename.less").isPresent()); // less is not supported
    assertTrue(ScriptRequest.tryParse("basename.css").isPresent());
  }

  @Test
  public void testTryParseWithValidInput() {
    String[] tests = {"path/basename.js", "path/basename.min.js", "path/basename-34fce3bc.min.js", "path/subpath/basename-34fce3bc.min.css", "path/subpath/entry1~entry2-34fce3bc.min.css", "/res/lib-1.10.88/lib.js", "base.css"};
    for (String test : tests) {
      assertEquals("invalid input: " + test, test, ScriptRequest.tryParse(test).orElseThrow(() -> new IllegalArgumentException("invalid input: " + test)).toString());
    }
  }

  @Test
  public void testToString() {
    ScriptRequest request = ScriptRequest.tryParse("path/subpath/basename-34fce3bc.min.css").get();
    assertEquals("basename-34fce3bc", request.toString(false, true, true, false, false));
    assertEquals("-34fce3bc", request.toString(false, false, true, false, false));
    assertEquals("basename.css", request.toString(false, true, false, false, true));
  }

  @Test
  public void testTryParseWithElements() {
    String path = "path/";
    String baseName = "basename";
    String fingerprint = "fce";
    String extension = "js";
    Optional<ScriptRequest> opt = ScriptRequest.tryParse(path, baseName, fingerprint, true, extension);

    assertEquals(path, opt.get().path());
    assertEquals(baseName, opt.get().baseName());
    assertEquals(fingerprint, opt.get().fingerprint());
    assertTrue(opt.get().minimized());
    assertEquals(extension, opt.get().fileExtension());
  }
}
