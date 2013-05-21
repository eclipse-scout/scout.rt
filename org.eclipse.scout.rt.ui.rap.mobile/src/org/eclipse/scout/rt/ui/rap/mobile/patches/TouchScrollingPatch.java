package org.eclipse.scout.rt.ui.rap.mobile.patches;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptLoader;


public class TouchScrollingPatch {

  private static final String SCRIPT_FILE = "TouchScrollingPatch.js";
  private static final String SCRIPT_URL = "touchscroll/TouchScrollingPatch.js";

  public static void enable() {
    ensureRegistered();
    ensureLoaded();
  }

  private static void ensureRegistered() {
    if( !RWT.getResourceManager().isRegistered( SCRIPT_URL ) ) {
      try {
        register();
      } catch( IOException exception ) {
        throw new RuntimeException( "Failed to register resource", exception );
      }
    }
  }

  private static void ensureLoaded() {
    JavaScriptLoader loader = RWT.getClient().getService( JavaScriptLoader.class );
    loader.require( RWT.getResourceManager().getLocation( SCRIPT_URL ) );
  }

  private static void register() throws IOException {
    InputStream inputStream = TouchScrollingPatch.class.getResourceAsStream( SCRIPT_FILE );
    try {
      RWT.getResourceManager().register( SCRIPT_URL, inputStream );
    } finally {
      inputStream.close();
    }
  }

}
