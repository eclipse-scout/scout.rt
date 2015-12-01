package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TuningUtility;
import org.junit.Test;

public class Base64UtilityTest {

  /**
   * This tests just a small set of byte lengths.
   */
  @Test
  public void testEncodeDecodeMultipleByteLength() throws Exception {
    for (int i = 0; i < 1000; i++) {
      byte[] byteArray = new byte[i];
      for (int j = 0; j < i; j++) {
        byteArray[j] = (byte) ((byte) i & 0xff);
      }
      TuningUtility.startTimer();
      String encode = Base64Utility.encode(byteArray);
      TuningUtility.stopTimer("encode", false, true);
      TuningUtility.startTimer();
      byte[] decode = Base64Utility.decode(encode);
      TuningUtility.stopTimer("decode", false, true);
      assertArrayEquals(byteArray, decode);
    }
    TuningUtility.finishAll();
  }

  @Test
  public void encodeDecodeTestShort() throws Exception {
    String encode = Base64Utility.encode(TEST_STRING_SHORT.getBytes());
    byte[] decode = Base64Utility.decode(encode);
    assertArrayEquals(TEST_STRING_SHORT.getBytes(), decode);
  }

  @Test
  public void encodeDecodeTestMiddle() throws Exception {
    String encode = Base64Utility.encode(TEST_STRING_MIDDLE.getBytes());
    byte[] decode = Base64Utility.decode(encode);
    assertArrayEquals(TEST_STRING_MIDDLE.getBytes(), decode);
  }

  @Test
  public void encodeDecodeTestLong() throws Exception {
    String encode = Base64Utility.encode(TEST_STRING_LONG.getBytes());
    byte[] decode = Base64Utility.decode(encode);
    assertArrayEquals(TEST_STRING_LONG.getBytes(), decode);
  }

  @Test
  public void encodeDecodeTestLongAndStringChangedToBlocks() throws Exception {
    String encode = Base64Utility.encode(TEST_STRING_LONG.getBytes());
    encode = StringUtility.wrapText(encode, 80);
    byte[] decode = Base64Utility.decode(encode);
    assertArrayEquals(TEST_STRING_LONG.getBytes(), decode);
  }

  @Test
  public void encodeTestShortAndCheckAgainstBase64String() throws Exception {
    String encode = Base64Utility.encode(TEST_STRING_SHORT.getBytes());
    assertEquals(TEST_BASE64_DATA_OF_STRING_SHORT, encode);
  }

  @Test
  public void encodeTestMiddleAndCheckAgainstBase64String() throws Exception {
    String encode = Base64Utility.encode(TEST_STRING_MIDDLE.getBytes());
    StringUtility.equalsIgnoreCase(TEST_BASE64_DATA_OF_STRING_MIDDLE, encode);
    assertEquals(TEST_BASE64_DATA_OF_STRING_MIDDLE, encode);
  }

  @Test
  public void encodeTestLongAndCheckAgainstBase64String() throws Exception {
    String encode = Base64Utility.encode(TEST_STRING_LONG.getBytes());
    assertEquals(TEST_BASE64_DATA_STRING_OF_STRING_LONG, encode);
  }

  @Test
  public void encodeTestLongAndStringChangedToBlocks() throws Exception {
    String encode = Base64Utility.encode(TEST_STRING_LONG.getBytes());
    encode = StringUtility.wrapText(encode, 76);
    assertEquals(TEST_BASE64_DATA_BLOCK_OF_STRING_LONG, encode);
  }

  @Test
  public void decodeTestShortBase64String() throws Exception {
    byte[] decode = Base64Utility.decode(TEST_BASE64_DATA_OF_STRING_SHORT);
    assertArrayEquals(TEST_STRING_SHORT.getBytes(), decode);
  }

  @Test
  public void decodeTestMiddleBase64String() throws Exception {
    byte[] decode = Base64Utility.decode(TEST_BASE64_DATA_OF_STRING_MIDDLE);
    assertArrayEquals(TEST_STRING_MIDDLE.getBytes(), decode);
  }

  @Test
  public void decodeTestLongBase64String() throws Exception {
    byte[] decode = Base64Utility.decode(TEST_BASE64_DATA_STRING_OF_STRING_LONG);
    assertArrayEquals(TEST_STRING_LONG.getBytes(), decode);
  }

  @Test
  public void decodeTestLongBlockBase64String() throws Exception {
    byte[] decode = Base64Utility.decode(TEST_BASE64_DATA_BLOCK_OF_STRING_LONG);
    assertArrayEquals(TEST_STRING_LONG.getBytes(), decode);
  }

  private static final String TEST_STRING_SHORT = "Lo";

  private static final String TEST_BASE64_DATA_OF_STRING_SHORT = "TG8=";

  private static final String TEST_STRING_MIDDLE = "Next is a line ending\n"
      + "This is the new line";

  private static final String TEST_BASE64_DATA_OF_STRING_MIDDLE = "TmV4dCBpcyBhIGxpbmUgZW5kaW5nClRoaXMgaXMgdGhlIG5ldyBsaW5l";

  private static final String TEST_STRING_LONG =
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed ut nisi dignissim leo sagittis tincidunt. Curabitur porttitor, magna ut consequat vestibulum, diam nibh faucibus quam, ac ultrices odio ante hendrerit mauris. Praesent id massa malesuada, mollis tortor eu, sagittis nisi. Sed sagittis orci vel dui gravida, nec euismod neque aliquet. Phasellus mattis augue ut dolor eleifend, a lacinia ligula vestibulum. Pellentesque placerat velit accumsan lorem vestibulum dignissim. Etiam eu egestas ante, quis porta erat. Suspendisse tempus a turpis condimentum elementum. Vestibulum iaculis augue quis mi vulputate fermentum ac vel nisl. Phasellus ac pellentesque velit, id dapibus nibh. Integer vitae ipsum ante. Aenean ultricies, velit placerat facilisis malesuada, lorem tortor convallis mauris, at congue felis tellus at tellus.\n"
          + "\n"
          + "Etiam interdum turpis non nibh laoreet tincidunt. Aliquam viverra massa ut tristique commodo. Donec quis fringilla nunc. Suspendisse mattis, ante at tempus fringilla, nulla lorem vestibulum est, in pharetra nisl sem vel metus. Donec id facilisis lacus, ac pellentesque lectus. Morbi rhoncus sem ac elit euismod, non cursus risus euismod. Nunc magna dolor, accumsan eget lectus sit amet, dignissim venenatis magna. Nulla facilisi. Cras tristique erat sit amet nunc feugiat, vel placerat tortor iaculis. Donec convallis, eros quis aliquet iaculis, felis ipsum dapibus velit, quis molestie quam mi vitae neque. In scelerisque, erat a iaculis adipiscing, mauris tellus facilisis libero, pharetra semper elit dolor quis velit. Proin odio sapien, rutrum vitae posuere vitae, vulputate porttitor tortor. Nullam molestie leo leo, non mattis sem sollicitudin eu.\n"
          + "\n"
          + "Quisque imperdiet sapien at dolor lacinia, ullamcorper tincidunt velit tempor. Integer commodo scelerisque tellus, eu vehicula dui molestie id. Ut et eros ut arcu rutrum semper. Phasellus pretium est tellus, ut sodales felis vehicula eget. Donec magna nisl, viverra eu nibh ac, porta pellentesque eros. In molestie metus a massa tincidunt fringilla. Integer at sem vestibulum, eleifend nibh id, laoreet urna. Aenean malesuada congue mauris dapibus bibendum. Donec tellus felis, vehicula eu consectetur vel, fringilla id nunc. Maecenas ligula erat, tristique sed pellentesque a, fermentum et nisi. Proin ultricies molestie quam, in faucibus urna semper quis. Ut hendrerit leo nec est gravida venenatis. Proin lacus odio, imperdiet quis diam sit amet, tempus tincidunt eros. Nunc in dolor vel eros porta egestas. In dignissim mauris non orci mattis, nec feugiat odio suscipit.\n"
          + "\n"
          + "Nunc feugiat est ante, id molestie enim interdum at. Integer vitae dignissim sapien. Nunc ultrices dui vitae ultricies ultrices. Donec cursus nulla nec laoreet vehicula. Donec lectus lorem, vehicula a eleifend lacinia, viverra vel dolor. Cras tincidunt ligula ligula, non lacinia odio varius vel. Pellentesque porta eros eget odio ornare, egestas vulputate metus vestibulum. Curabitur ac vehicula ipsum, vel interdum lectus. Curabitur porttitor nunc nunc, non lobortis purus ultricies at. Donec in nisl a lacus faucibus eleifend eget et ante. Sed sed euismod est. Vivamus semper massa facilisis erat lobortis, vel vulputate elit volutpat.\n"
          + "\n"
          + "Sed placerat odio turpis, vitae tristique purus egestas vitae. Etiam eros erat, tristique nec volutpat eu, gravida sit amet lectus. Proin non fringilla elit, et elementum dui. Proin faucibus enim nec bibendum ultrices. Cras facilisis et nunc et hendrerit. Praesent ac dolor sed tellus consequat congue. Maecenas et elit ut lorem cursus ullamcorper.";

  private static final String TEST_BASE64_DATA_STRING_OF_STRING_LONG =
      "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdC4gU2VkIHV0IG5pc2kgZGlnbmlzc2ltIGxlbyBzYWdpdHRpcyB0aW5jaWR1bnQuIEN1cmFiaXR1ciBwb3J0dGl0b3IsIG1hZ25hIHV0IGNvbnNlcXVhdCB2ZXN0aWJ1bHVtLCBkaWFtIG5pYmggZmF1Y2lidXMgcXVhbSwgYWMgdWx0cmljZXMgb2RpbyBhbnRlIGhlbmRyZXJpdCBtYXVyaXMuIFByYWVzZW50IGlkIG1hc3NhIG1hbGVzdWFkYSwgbW9sbGlzIHRvcnRvciBldSwgc2FnaXR0aXMgbmlzaS4gU2VkIHNhZ2l0dGlzIG9yY2kgdmVsIGR1aSBncmF2aWRhLCBuZWMgZXVpc21vZCBuZXF1ZSBhbGlxdWV0LiBQaGFzZWxsdXMgbWF0dGlzIGF1Z3VlIHV0IGRvbG9yIGVsZWlmZW5kLCBhIGxhY2luaWEgbGlndWxhIHZlc3RpYnVsdW0uIFBlbGxlbnRlc3F1ZSBwbGFjZXJhdCB2ZWxpdCBhY2N1bXNhbiBsb3JlbSB2ZXN0aWJ1bHVtIGRpZ25pc3NpbS4gRXRpYW0gZXUgZWdlc3RhcyBhbnRlLCBxdWlzIHBvcnRhIGVyYXQuIFN1c3BlbmRpc3NlIHRlbXB1cyBhIHR1cnBpcyBjb25kaW1lbnR1bSBlbGVtZW50dW0uIFZlc3RpYnVsdW0gaWFjdWxpcyBhdWd1ZSBxdWlzIG1pIHZ1bHB1dGF0ZSBmZXJtZW50dW0gYWMgdmVsIG5pc2wuIFBoYXNlbGx1cyBhYyBwZWxsZW50ZXNxdWUgdmVsaXQsIGlkIGRhcGlidXMgbmliaC4gSW50ZWdlciB2aXRhZSBpcHN1bSBhbnRlLiBBZW5lYW4gdWx0cmljaWVzLCB2ZWxpdCBwbGFjZXJhdCBmYWNpbGlzaXMgbWFsZXN1YWRhLCBsb3JlbSB0b3J0b3IgY29udmFsbGlzIG1hdXJpcywgYXQgY29uZ3VlIGZlbGlzIHRlbGx1cyBhdCB0ZWxsdXMuCgpFdGlhbSBpbnRlcmR1bSB0dXJwaXMgbm9uIG5pYmggbGFvcmVldCB0aW5jaWR1bnQuIEFsaXF1YW0gdml2ZXJyYSBtYXNzYSB1dCB0cmlzdGlxdWUgY29tbW9kby4gRG9uZWMgcXVpcyBmcmluZ2lsbGEgbnVuYy4gU3VzcGVuZGlzc2UgbWF0dGlzLCBhbnRlIGF0IHRlbXB1cyBmcmluZ2lsbGEsIG51bGxhIGxvcmVtIHZlc3RpYnVsdW0gZXN0LCBpbiBwaGFyZXRyYSBuaXNsIHNlbSB2ZWwgbWV0dXMuIERvbmVjIGlkIGZhY2lsaXNpcyBsYWN1cywgYWMgcGVsbGVudGVzcXVlIGxlY3R1cy4gTW9yYmkgcmhvbmN1cyBzZW0gYWMgZWxpdCBldWlzbW9kLCBub24gY3Vyc3VzIHJpc3VzIGV1aXNtb2QuIE51bmMgbWFnbmEgZG9sb3IsIGFjY3Vtc2FuIGVnZXQgbGVjdHVzIHNpdCBhbWV0LCBkaWduaXNzaW0gdmVuZW5hdGlzIG1hZ25hLiBOdWxsYSBmYWNpbGlzaS4gQ3JhcyB0cmlzdGlxdWUgZXJhdCBzaXQgYW1ldCBudW5jIGZldWdpYXQsIHZlbCBwbGFjZXJhdCB0b3J0b3IgaWFjdWxpcy4gRG9uZWMgY29udmFsbGlzLCBlcm9zIHF1aXMgYWxpcXVldCBpYWN1bGlzLCBmZWxpcyBpcHN1bSBkYXBpYnVzIHZlbGl0LCBxdWlzIG1vbGVzdGllIHF1YW0gbWkgdml0YWUgbmVxdWUuIEluIHNjZWxlcmlzcXVlLCBlcmF0IGEgaWFjdWxpcyBhZGlwaXNjaW5nLCBtYXVyaXMgdGVsbHVzIGZhY2lsaXNpcyBsaWJlcm8sIHBoYXJldHJhIHNlbXBlciBlbGl0IGRvbG9yIHF1aXMgdmVsaXQuIFByb2luIG9kaW8gc2FwaWVuLCBydXRydW0gdml0YWUgcG9zdWVyZSB2aXRhZSwgdnVscHV0YXRlIHBvcnR0aXRvciB0b3J0b3IuIE51bGxhbSBtb2xlc3RpZSBsZW8gbGVvLCBub24gbWF0dGlzIHNlbSBzb2xsaWNpdHVkaW4gZXUuCgpRdWlzcXVlIGltcGVyZGlldCBzYXBpZW4gYXQgZG9sb3IgbGFjaW5pYSwgdWxsYW1jb3JwZXIgdGluY2lkdW50IHZlbGl0IHRlbXBvci4gSW50ZWdlciBjb21tb2RvIHNjZWxlcmlzcXVlIHRlbGx1cywgZXUgdmVoaWN1bGEgZHVpIG1vbGVzdGllIGlkLiBVdCBldCBlcm9zIHV0IGFyY3UgcnV0cnVtIHNlbXBlci4gUGhhc2VsbHVzIHByZXRpdW0gZXN0IHRlbGx1cywgdXQgc29kYWxlcyBmZWxpcyB2ZWhpY3VsYSBlZ2V0LiBEb25lYyBtYWduYSBuaXNsLCB2aXZlcnJhIGV1IG5pYmggYWMsIHBvcnRhIHBlbGxlbnRlc3F1ZSBlcm9zLiBJbiBtb2xlc3RpZSBtZXR1cyBhIG1hc3NhIHRpbmNpZHVudCBmcmluZ2lsbGEuIEludGVnZXIgYXQgc2VtIHZlc3RpYnVsdW0sIGVsZWlmZW5kIG5pYmggaWQsIGxhb3JlZXQgdXJuYS4gQWVuZWFuIG1hbGVzdWFkYSBjb25ndWUgbWF1cmlzIGRhcGlidXMgYmliZW5kdW0uIERvbmVjIHRlbGx1cyBmZWxpcywgdmVoaWN1bGEgZXUgY29uc2VjdGV0dXIgdmVsLCBmcmluZ2lsbGEgaWQgbnVuYy4gTWFlY2VuYXMgbGlndWxhIGVyYXQsIHRyaXN0aXF1ZSBzZWQgcGVsbGVudGVzcXVlIGEsIGZlcm1lbnR1bSBldCBuaXNpLiBQcm9pbiB1bHRyaWNpZXMgbW9sZXN0aWUgcXVhbSwgaW4gZmF1Y2lidXMgdXJuYSBzZW1wZXIgcXVpcy4gVXQgaGVuZHJlcml0IGxlbyBuZWMgZXN0IGdyYXZpZGEgdmVuZW5hdGlzLiBQcm9pbiBsYWN1cyBvZGlvLCBpbXBlcmRpZXQgcXVpcyBkaWFtIHNpdCBhbWV0LCB0ZW1wdXMgdGluY2lkdW50IGVyb3MuIE51bmMgaW4gZG9sb3IgdmVsIGVyb3MgcG9ydGEgZWdlc3Rhcy4gSW4gZGlnbmlzc2ltIG1hdXJpcyBub24gb3JjaSBtYXR0aXMsIG5lYyBmZXVnaWF0IG9kaW8gc3VzY2lwaXQuCgpOdW5jIGZldWdpYXQgZXN0IGFudGUsIGlkIG1vbGVzdGllIGVuaW0gaW50ZXJkdW0gYXQuIEludGVnZXIgdml0YWUgZGlnbmlzc2ltIHNhcGllbi4gTnVuYyB1bHRyaWNlcyBkdWkgdml0YWUgdWx0cmljaWVzIHVsdHJpY2VzLiBEb25lYyBjdXJzdXMgbnVsbGEgbmVjIGxhb3JlZXQgdmVoaWN1bGEuIERvbmVjIGxlY3R1cyBsb3JlbSwgdmVoaWN1bGEgYSBlbGVpZmVuZCBsYWNpbmlhLCB2aXZlcnJhIHZlbCBkb2xvci4gQ3JhcyB0aW5jaWR1bnQgbGlndWxhIGxpZ3VsYSwgbm9uIGxhY2luaWEgb2RpbyB2YXJpdXMgdmVsLiBQZWxsZW50ZXNxdWUgcG9ydGEgZXJvcyBlZ2V0IG9kaW8gb3JuYXJlLCBlZ2VzdGFzIHZ1bHB1dGF0ZSBtZXR1cyB2ZXN0aWJ1bHVtLiBDdXJhYml0dXIgYWMgdmVoaWN1bGEgaXBzdW0sIHZlbCBpbnRlcmR1bSBsZWN0dXMuIEN1cmFiaXR1ciBwb3J0dGl0b3IgbnVuYyBudW5jLCBub24gbG9ib3J0aXMgcHVydXMgdWx0cmljaWVzIGF0LiBEb25lYyBpbiBuaXNsIGEgbGFjdXMgZmF1Y2lidXMgZWxlaWZlbmQgZWdldCBldCBhbnRlLiBTZWQgc2VkIGV1aXNtb2QgZXN0LiBWaXZhbXVzIHNlbXBlciBtYXNzYSBmYWNpbGlzaXMgZXJhdCBsb2JvcnRpcywgdmVsIHZ1bHB1dGF0ZSBlbGl0IHZvbHV0cGF0LgoKU2VkIHBsYWNlcmF0IG9kaW8gdHVycGlzLCB2aXRhZSB0cmlzdGlxdWUgcHVydXMgZWdlc3RhcyB2aXRhZS4gRXRpYW0gZXJvcyBlcmF0LCB0cmlzdGlxdWUgbmVjIHZvbHV0cGF0IGV1LCBncmF2aWRhIHNpdCBhbWV0IGxlY3R1cy4gUHJvaW4gbm9uIGZyaW5naWxsYSBlbGl0LCBldCBlbGVtZW50dW0gZHVpLiBQcm9pbiBmYXVjaWJ1cyBlbmltIG5lYyBiaWJlbmR1bSB1bHRyaWNlcy4gQ3JhcyBmYWNpbGlzaXMgZXQgbnVuYyBldCBoZW5kcmVyaXQuIFByYWVzZW50IGFjIGRvbG9yIHNlZCB0ZWxsdXMgY29uc2VxdWF0IGNvbmd1ZS4gTWFlY2VuYXMgZXQgZWxpdCB1dCBsb3JlbSBjdXJzdXMgdWxsYW1jb3JwZXIu";

  private static final String TEST_BASE64_DATA_BLOCK_OF_STRING_LONG =
      "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdC4g\n"
          + "U2VkIHV0IG5pc2kgZGlnbmlzc2ltIGxlbyBzYWdpdHRpcyB0aW5jaWR1bnQuIEN1cmFiaXR1ciBw\n"
          + "b3J0dGl0b3IsIG1hZ25hIHV0IGNvbnNlcXVhdCB2ZXN0aWJ1bHVtLCBkaWFtIG5pYmggZmF1Y2li\n"
          + "dXMgcXVhbSwgYWMgdWx0cmljZXMgb2RpbyBhbnRlIGhlbmRyZXJpdCBtYXVyaXMuIFByYWVzZW50\n"
          + "IGlkIG1hc3NhIG1hbGVzdWFkYSwgbW9sbGlzIHRvcnRvciBldSwgc2FnaXR0aXMgbmlzaS4gU2Vk\n"
          + "IHNhZ2l0dGlzIG9yY2kgdmVsIGR1aSBncmF2aWRhLCBuZWMgZXVpc21vZCBuZXF1ZSBhbGlxdWV0\n"
          + "LiBQaGFzZWxsdXMgbWF0dGlzIGF1Z3VlIHV0IGRvbG9yIGVsZWlmZW5kLCBhIGxhY2luaWEgbGln\n"
          + "dWxhIHZlc3RpYnVsdW0uIFBlbGxlbnRlc3F1ZSBwbGFjZXJhdCB2ZWxpdCBhY2N1bXNhbiBsb3Jl\n"
          + "bSB2ZXN0aWJ1bHVtIGRpZ25pc3NpbS4gRXRpYW0gZXUgZWdlc3RhcyBhbnRlLCBxdWlzIHBvcnRh\n"
          + "IGVyYXQuIFN1c3BlbmRpc3NlIHRlbXB1cyBhIHR1cnBpcyBjb25kaW1lbnR1bSBlbGVtZW50dW0u\n"
          + "IFZlc3RpYnVsdW0gaWFjdWxpcyBhdWd1ZSBxdWlzIG1pIHZ1bHB1dGF0ZSBmZXJtZW50dW0gYWMg\n"
          + "dmVsIG5pc2wuIFBoYXNlbGx1cyBhYyBwZWxsZW50ZXNxdWUgdmVsaXQsIGlkIGRhcGlidXMgbmli\n"
          + "aC4gSW50ZWdlciB2aXRhZSBpcHN1bSBhbnRlLiBBZW5lYW4gdWx0cmljaWVzLCB2ZWxpdCBwbGFj\n"
          + "ZXJhdCBmYWNpbGlzaXMgbWFsZXN1YWRhLCBsb3JlbSB0b3J0b3IgY29udmFsbGlzIG1hdXJpcywg\n"
          + "YXQgY29uZ3VlIGZlbGlzIHRlbGx1cyBhdCB0ZWxsdXMuCgpFdGlhbSBpbnRlcmR1bSB0dXJwaXMg\n"
          + "bm9uIG5pYmggbGFvcmVldCB0aW5jaWR1bnQuIEFsaXF1YW0gdml2ZXJyYSBtYXNzYSB1dCB0cmlz\n"
          + "dGlxdWUgY29tbW9kby4gRG9uZWMgcXVpcyBmcmluZ2lsbGEgbnVuYy4gU3VzcGVuZGlzc2UgbWF0\n"
          + "dGlzLCBhbnRlIGF0IHRlbXB1cyBmcmluZ2lsbGEsIG51bGxhIGxvcmVtIHZlc3RpYnVsdW0gZXN0\n"
          + "LCBpbiBwaGFyZXRyYSBuaXNsIHNlbSB2ZWwgbWV0dXMuIERvbmVjIGlkIGZhY2lsaXNpcyBsYWN1\n"
          + "cywgYWMgcGVsbGVudGVzcXVlIGxlY3R1cy4gTW9yYmkgcmhvbmN1cyBzZW0gYWMgZWxpdCBldWlz\n"
          + "bW9kLCBub24gY3Vyc3VzIHJpc3VzIGV1aXNtb2QuIE51bmMgbWFnbmEgZG9sb3IsIGFjY3Vtc2Fu\n"
          + "IGVnZXQgbGVjdHVzIHNpdCBhbWV0LCBkaWduaXNzaW0gdmVuZW5hdGlzIG1hZ25hLiBOdWxsYSBm\n"
          + "YWNpbGlzaS4gQ3JhcyB0cmlzdGlxdWUgZXJhdCBzaXQgYW1ldCBudW5jIGZldWdpYXQsIHZlbCBw\n"
          + "bGFjZXJhdCB0b3J0b3IgaWFjdWxpcy4gRG9uZWMgY29udmFsbGlzLCBlcm9zIHF1aXMgYWxpcXVl\n"
          + "dCBpYWN1bGlzLCBmZWxpcyBpcHN1bSBkYXBpYnVzIHZlbGl0LCBxdWlzIG1vbGVzdGllIHF1YW0g\n"
          + "bWkgdml0YWUgbmVxdWUuIEluIHNjZWxlcmlzcXVlLCBlcmF0IGEgaWFjdWxpcyBhZGlwaXNjaW5n\n"
          + "LCBtYXVyaXMgdGVsbHVzIGZhY2lsaXNpcyBsaWJlcm8sIHBoYXJldHJhIHNlbXBlciBlbGl0IGRv\n"
          + "bG9yIHF1aXMgdmVsaXQuIFByb2luIG9kaW8gc2FwaWVuLCBydXRydW0gdml0YWUgcG9zdWVyZSB2\n"
          + "aXRhZSwgdnVscHV0YXRlIHBvcnR0aXRvciB0b3J0b3IuIE51bGxhbSBtb2xlc3RpZSBsZW8gbGVv\n"
          + "LCBub24gbWF0dGlzIHNlbSBzb2xsaWNpdHVkaW4gZXUuCgpRdWlzcXVlIGltcGVyZGlldCBzYXBp\n"
          + "ZW4gYXQgZG9sb3IgbGFjaW5pYSwgdWxsYW1jb3JwZXIgdGluY2lkdW50IHZlbGl0IHRlbXBvci4g\n"
          + "SW50ZWdlciBjb21tb2RvIHNjZWxlcmlzcXVlIHRlbGx1cywgZXUgdmVoaWN1bGEgZHVpIG1vbGVz\n"
          + "dGllIGlkLiBVdCBldCBlcm9zIHV0IGFyY3UgcnV0cnVtIHNlbXBlci4gUGhhc2VsbHVzIHByZXRp\n"
          + "dW0gZXN0IHRlbGx1cywgdXQgc29kYWxlcyBmZWxpcyB2ZWhpY3VsYSBlZ2V0LiBEb25lYyBtYWdu\n"
          + "YSBuaXNsLCB2aXZlcnJhIGV1IG5pYmggYWMsIHBvcnRhIHBlbGxlbnRlc3F1ZSBlcm9zLiBJbiBt\n"
          + "b2xlc3RpZSBtZXR1cyBhIG1hc3NhIHRpbmNpZHVudCBmcmluZ2lsbGEuIEludGVnZXIgYXQgc2Vt\n"
          + "IHZlc3RpYnVsdW0sIGVsZWlmZW5kIG5pYmggaWQsIGxhb3JlZXQgdXJuYS4gQWVuZWFuIG1hbGVz\n"
          + "dWFkYSBjb25ndWUgbWF1cmlzIGRhcGlidXMgYmliZW5kdW0uIERvbmVjIHRlbGx1cyBmZWxpcywg\n"
          + "dmVoaWN1bGEgZXUgY29uc2VjdGV0dXIgdmVsLCBmcmluZ2lsbGEgaWQgbnVuYy4gTWFlY2VuYXMg\n"
          + "bGlndWxhIGVyYXQsIHRyaXN0aXF1ZSBzZWQgcGVsbGVudGVzcXVlIGEsIGZlcm1lbnR1bSBldCBu\n"
          + "aXNpLiBQcm9pbiB1bHRyaWNpZXMgbW9sZXN0aWUgcXVhbSwgaW4gZmF1Y2lidXMgdXJuYSBzZW1w\n"
          + "ZXIgcXVpcy4gVXQgaGVuZHJlcml0IGxlbyBuZWMgZXN0IGdyYXZpZGEgdmVuZW5hdGlzLiBQcm9p\n"
          + "biBsYWN1cyBvZGlvLCBpbXBlcmRpZXQgcXVpcyBkaWFtIHNpdCBhbWV0LCB0ZW1wdXMgdGluY2lk\n"
          + "dW50IGVyb3MuIE51bmMgaW4gZG9sb3IgdmVsIGVyb3MgcG9ydGEgZWdlc3Rhcy4gSW4gZGlnbmlz\n"
          + "c2ltIG1hdXJpcyBub24gb3JjaSBtYXR0aXMsIG5lYyBmZXVnaWF0IG9kaW8gc3VzY2lwaXQuCgpO\n"
          + "dW5jIGZldWdpYXQgZXN0IGFudGUsIGlkIG1vbGVzdGllIGVuaW0gaW50ZXJkdW0gYXQuIEludGVn\n"
          + "ZXIgdml0YWUgZGlnbmlzc2ltIHNhcGllbi4gTnVuYyB1bHRyaWNlcyBkdWkgdml0YWUgdWx0cmlj\n"
          + "aWVzIHVsdHJpY2VzLiBEb25lYyBjdXJzdXMgbnVsbGEgbmVjIGxhb3JlZXQgdmVoaWN1bGEuIERv\n"
          + "bmVjIGxlY3R1cyBsb3JlbSwgdmVoaWN1bGEgYSBlbGVpZmVuZCBsYWNpbmlhLCB2aXZlcnJhIHZl\n"
          + "bCBkb2xvci4gQ3JhcyB0aW5jaWR1bnQgbGlndWxhIGxpZ3VsYSwgbm9uIGxhY2luaWEgb2RpbyB2\n"
          + "YXJpdXMgdmVsLiBQZWxsZW50ZXNxdWUgcG9ydGEgZXJvcyBlZ2V0IG9kaW8gb3JuYXJlLCBlZ2Vz\n"
          + "dGFzIHZ1bHB1dGF0ZSBtZXR1cyB2ZXN0aWJ1bHVtLiBDdXJhYml0dXIgYWMgdmVoaWN1bGEgaXBz\n"
          + "dW0sIHZlbCBpbnRlcmR1bSBsZWN0dXMuIEN1cmFiaXR1ciBwb3J0dGl0b3IgbnVuYyBudW5jLCBu\n"
          + "b24gbG9ib3J0aXMgcHVydXMgdWx0cmljaWVzIGF0LiBEb25lYyBpbiBuaXNsIGEgbGFjdXMgZmF1\n"
          + "Y2lidXMgZWxlaWZlbmQgZWdldCBldCBhbnRlLiBTZWQgc2VkIGV1aXNtb2QgZXN0LiBWaXZhbXVz\n"
          + "IHNlbXBlciBtYXNzYSBmYWNpbGlzaXMgZXJhdCBsb2JvcnRpcywgdmVsIHZ1bHB1dGF0ZSBlbGl0\n"
          + "IHZvbHV0cGF0LgoKU2VkIHBsYWNlcmF0IG9kaW8gdHVycGlzLCB2aXRhZSB0cmlzdGlxdWUgcHVy\n"
          + "dXMgZWdlc3RhcyB2aXRhZS4gRXRpYW0gZXJvcyBlcmF0LCB0cmlzdGlxdWUgbmVjIHZvbHV0cGF0\n"
          + "IGV1LCBncmF2aWRhIHNpdCBhbWV0IGxlY3R1cy4gUHJvaW4gbm9uIGZyaW5naWxsYSBlbGl0LCBl\n"
          + "dCBlbGVtZW50dW0gZHVpLiBQcm9pbiBmYXVjaWJ1cyBlbmltIG5lYyBiaWJlbmR1bSB1bHRyaWNl\n"
          + "cy4gQ3JhcyBmYWNpbGlzaXMgZXQgbnVuYyBldCBoZW5kcmVyaXQuIFByYWVzZW50IGFjIGRvbG9y\n"
          + "IHNlZCB0ZWxsdXMgY29uc2VxdWF0IGNvbmd1ZS4gTWFlY2VuYXMgZXQgZWxpdCB1dCBsb3JlbSBj\n"
          + "dXJzdXMgdWxsYW1jb3JwZXIu";
}
