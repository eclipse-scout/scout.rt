package org.eclipse.scout.commons;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class Base64UtilityTest {
  private static final String TEST_STRING_SHORT = "Lo";

  private static final String TEST_STRING_LONG = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed ut nisi dignissim leo sagittis tincidunt. Curabitur porttitor, magna ut consequat vestibulum, diam nibh faucibus quam, ac ultrices odio ante hendrerit mauris. Praesent id massa malesuada, mollis tortor eu, sagittis nisi. Sed sagittis orci vel dui gravida, nec euismod neque aliquet. Phasellus mattis augue ut dolor eleifend, a lacinia ligula vestibulum. Pellentesque placerat velit accumsan lorem vestibulum dignissim. Etiam eu egestas ante, quis porta erat. Suspendisse tempus a turpis condimentum elementum. Vestibulum iaculis augue quis mi vulputate fermentum ac vel nisl. Phasellus ac pellentesque velit, id dapibus nibh. Integer vitae ipsum ante. Aenean ultricies, velit placerat facilisis malesuada, lorem tortor convallis mauris, at congue felis tellus at tellus." +
      "Etiam interdum turpis non nibh laoreet tincidunt. Aliquam viverra massa ut tristique commodo. Donec quis fringilla nunc. Suspendisse mattis, ante at tempus fringilla, nulla lorem vestibulum est, in pharetra nisl sem vel metus. Donec id facilisis lacus, ac pellentesque lectus. Morbi rhoncus sem ac elit euismod, non cursus risus euismod. Nunc magna dolor, accumsan eget lectus sit amet, dignissim venenatis magna. Nulla facilisi. Cras tristique erat sit amet nunc feugiat, vel placerat tortor iaculis. Donec convallis, eros quis aliquet iaculis, felis ipsum dapibus velit, quis molestie quam mi vitae neque. In scelerisque, erat a iaculis adipiscing, mauris tellus facilisis libero, pharetra semper elit dolor quis velit. Proin odio sapien, rutrum vitae posuere vitae, vulputate porttitor tortor. Nullam molestie leo leo, non mattis sem sollicitudin eu." +
      "Quisque imperdiet sapien at dolor lacinia, ullamcorper tincidunt velit tempor. Integer commodo scelerisque tellus, eu vehicula dui molestie id. Ut et eros ut arcu rutrum semper. Phasellus pretium est tellus, ut sodales felis vehicula eget. Donec magna nisl, viverra eu nibh ac, porta pellentesque eros. In molestie metus a massa tincidunt fringilla. Integer at sem vestibulum, eleifend nibh id, laoreet urna. Aenean malesuada congue mauris dapibus bibendum. Donec tellus felis, vehicula eu consectetur vel, fringilla id nunc. Maecenas ligula erat, tristique sed pellentesque a, fermentum et nisi. Proin ultricies molestie quam, in faucibus urna semper quis. Ut hendrerit leo nec est gravida venenatis. Proin lacus odio, imperdiet quis diam sit amet, tempus tincidunt eros. Nunc in dolor vel eros porta egestas. In dignissim mauris non orci mattis, nec feugiat odio suscipit." +
      "Nunc feugiat est ante, id molestie enim interdum at. Integer vitae dignissim sapien. Nunc ultrices dui vitae ultricies ultrices. Donec cursus nulla nec laoreet vehicula. Donec lectus lorem, vehicula a eleifend lacinia, viverra vel dolor. Cras tincidunt ligula ligula, non lacinia odio varius vel. Pellentesque porta eros eget odio ornare, egestas vulputate metus vestibulum. Curabitur ac vehicula ipsum, vel interdum lectus. Curabitur porttitor nunc nunc, non lobortis purus ultricies at. Donec in nisl a lacus faucibus eleifend eget et ante. Sed sed euismod est. Vivamus semper massa facilisis erat lobortis, vel vulputate elit volutpat." +
      "Sed placerat odio turpis, vitae tristique purus egestas vitae. Etiam eros erat, tristique nec volutpat eu, gravida sit amet lectus. Proin non fringilla elit, et elementum dui. Proin faucibus enim nec bibendum ultrices. Cras facilisis et nunc et hendrerit. Praesent ac dolor sed tellus consequat congue. Maecenas et elit ut lorem cursus ullamcorper.";

  @Test
  public void encodeDecodeTestShort() throws Exception {
    String encode = Base64Utility.encode(TEST_STRING_SHORT.getBytes());
    byte[] decode = Base64Utility.decode(encode);
    assertArrayEquals(TEST_STRING_SHORT.getBytes(), decode);
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
}
