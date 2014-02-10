package org.eclipse.scout.rt.ui.swt.util;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.junit.Test;

/**
 * JUnit tests for {@link SwtUtility}
 * 
 * @since 3.10.0-M4
 */
public class SwtUtilityTest {

  @Test
  public void testKeyStrokeRepresentation() {
    KeyStroke ks = new KeyStroke("f11");
    assertEquals("KeyStroke should be f11", "f11", ks.getKeyStroke());
    assertEquals("KeyStroke pretty printed should be F11", "F11", SwtUtility.getKeyStrokePrettyPrinted(ks));

    ks = new KeyStroke("a");
    assertEquals("KeyStroke should be 'a'", "a", ks.getKeyStroke());
    assertEquals("KeyStroke pretty printed should be a", "a", SwtUtility.getKeyStrokePrettyPrinted(ks));

    ks = new KeyStroke("alternate-f1");
    assertEquals("KeyStroke should be alternate-f1", "alternate-f1", ks.getKeyStroke());
    assertEquals("KeyStroke pretty printed should be Alt+F1", "Alt+F1", SwtUtility.getKeyStrokePrettyPrinted(ks));

    ks = new KeyStroke("control-alternate-1");
    assertEquals("KeyStroke should be control-alternate-1", "control-alternate-1", ks.getKeyStroke());
    assertEquals("KeyStroke pretty printed should be Ctrl+Alt+1", "Ctrl+Alt+1", SwtUtility.getKeyStrokePrettyPrinted(ks));

    ks = new KeyStroke("");
    assertEquals("KeyStroke should be empty", "", ks.getKeyStroke());
    assertEquals("KeyStroke pretty printed should be empty", "", SwtUtility.getKeyStrokePrettyPrinted(ks));
  }
}
