/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.script;

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
    assertFalse(ScriptRequest.tryParse("basename.less").isPresent()); // path is mandatory
  }

  @Test
  public void testTryParseWithValidInput() {
    String[] tests = {"path/basename.js", "path/basename.min.js", "path/basename-34fce3bc.min.js", "path/subpath/basename-34fce3bc.min.css", "/res/lib-1.10.88/lib.js"};
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

  @Test
  public void testToMinimized() {
    ScriptRequest request = ScriptRequest.tryParse("chartjs/Chart-2.8.0.js").get();
    ScriptRequest minRequest = request.toMinimized(true);
    assertEquals("chartjs/Chart-2.8.0.min.js", minRequest.toString());

    // already minified
    request = ScriptRequest.tryParse("chartjs/Chart-2.8.0.min.js").get();
    minRequest = request.toMinimized(true);
    assertEquals("chartjs/Chart-2.8.0.min.js", minRequest.toString());

    // Should remove the .min suffix from the filename
    ScriptRequest nonMinRequest = request.toMinimized(false);
    assertEquals("chartjs/Chart-2.8.0.js", nonMinRequest.toString());

    // already non-minified
    request = ScriptRequest.tryParse("chartjs/Chart-2.8.0.js").get();
    nonMinRequest = request.toMinimized(false);
    assertEquals("chartjs/Chart-2.8.0.js", nonMinRequest.toString());
  }
}
