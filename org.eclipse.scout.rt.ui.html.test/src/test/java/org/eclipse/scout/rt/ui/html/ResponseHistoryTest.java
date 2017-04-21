package org.eclipse.scout.rt.ui.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.json.JSONObject;
import org.junit.Test;

public class ResponseHistoryTest {

  @Test
  public void testRegister() {
    ResponseHistory history = new ResponseHistory();

    // Check that no exceptions are thrown on an empty history
    history.toString();
    history.confirmResponseProcessed(1L);
    history.toSyncResponse();
    assertNull(history.getResponse(1L));
    assertNull(history.getResponse(null));

    // Insert two requests
    JSONObject resp1 = new JSONObject();
    JSONObject resp2 = new JSONObject();
    history.registerResponse(resp1, 7L);
    history.registerResponse(resp2, 8L);

    // Test getters
    assertSame(resp1, history.getResponse(7L));
    assertSame(resp2, history.getResponse(8L));

    // TODO: Test combined response
  }

  @Test
  public void testCleanup() {
    ResponseHistory history = new ResponseHistory();

    // Insert two requests
    JSONObject resp1 = new JSONObject();
    JSONObject resp2 = new JSONObject();
    history.registerResponse(resp1, 7L);
    history.registerResponse(resp2, 8L);

    // Test confirm
    assertEquals(2, history.size());
    history.confirmResponseProcessed(7L);
    assertEquals(1, history.size());
    history.confirmResponseProcessed(5L);
    assertEquals(1, history.size());
    history.confirmResponseProcessed(8L);
    assertEquals(0, history.size());

    // Test confirm with higher sequence no.
    history.registerResponse(resp1, 7L);
    history.registerResponse(resp2, 8L);
    assertEquals(2, history.size());
    history.confirmResponseProcessed(9L); // auto-confirms #7 and #8
    assertEquals(0, history.size());
  }

  @Test
  public void testMaxSize() {
    ResponseHistory history = new ResponseHistory();

    List<JSONObject> all = new ArrayList<>();
    // Insert many requests
    for (int i = 0; i < 20; i++) {
      JSONObject resp = new JSONObject();
      all.add(resp);
      history.registerResponse(resp, Long.valueOf(i));
      assertEquals(Math.min(i + 1, 10), history.size());
    }

    assertNull(history.getResponse(0L));
    assertNull(history.getResponse(1L));
    assertNull(history.getResponse(2L));
    assertNull(history.getResponse(9L));
    assertSame(all.get(10), history.getResponse(10L));
    assertSame(all.get(11), history.getResponse(11L));
    assertSame(all.get(12), history.getResponse(12L));
    assertSame(all.get(19), history.getResponse(19L));
    assertNull(history.getResponse(20L));
  }

  @Test(expected = AssertionException.class)
  public void testFail1() {
    ResponseHistory history = new ResponseHistory();
    history.registerResponse(null, null); // null args
  }

  @Test(expected = AssertionException.class)
  public void testFail2() {
    ResponseHistory history = new ResponseHistory();
    history.registerResponse(new JSONObject(), null); // null arg
  }

  @Test(expected = AssertionException.class)
  public void testFail3() {
    ResponseHistory history = new ResponseHistory();
    history.registerResponse(null, 1L); // null arg
  }

  @Test(expected = AssertionException.class)
  public void testFail4() {
    ResponseHistory history = new ResponseHistory();
    history.confirmResponseProcessed(null); // null arg
  }

  @Test(expected = AssertionException.class)
  public void testFail5() {
    ResponseHistory history = new ResponseHistory();
    history.registerResponse(new JSONObject(), 2L);
    history.registerResponse(new JSONObject(), 2L); // response sequence no. already registered
  }
}
