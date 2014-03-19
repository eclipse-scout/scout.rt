/**
 *
 */
package org.eclipse.scout.rt.ui.json;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class JsonDesktopTest {
  private JsonDesktop m_jsonDesktop;

  @Before
  public void beforeTest() {
    IDesktop desktop = Mockito.mock(IDesktop.class);
    IJsonSession session = Mockito.mock(IJsonSession.class);
    m_jsonDesktop = new JsonDesktop(desktop, session);
  }

  @Test
  public void testJsonObject() throws ProcessingException {
    IForm form = Mockito.mock(IForm.class);
    Mockito.when(form.getFormId()).thenReturn("JsonForm");
    Mockito.when(form.getTitle()).thenReturn("JsonForm");
    Mockito.when(form.getIconId()).thenReturn("JsonForm");

    JSONObject json = m_jsonDesktop.formToJson(form);
    assertEquals(
        "{\"title\":\"JsonForm\",\"formId\":\"JsonForm\",\"iconId\":\"JsonForm\"}",
        json.toString());
  }

  @Test
  public void testJsonArray() throws Exception {
    JSONArray jsonArray = new JSONArray();
    jsonArray.put("hello");
    jsonArray.put("world");
    assertEquals("[\"hello\",\"world\"]", jsonArray.toString());
  }

}
