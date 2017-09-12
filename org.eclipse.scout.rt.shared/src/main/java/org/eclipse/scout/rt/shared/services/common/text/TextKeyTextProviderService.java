package org.eclipse.scout.rt.shared.services.common.text;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.SharedConfigProperties.TextProvidersShowKeysProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This TextProvider simply returns the given text key passed to the getText() method. This provider can be used to
 * display text keys in the UI of a running application, which is useful when you want to see which text key belongs to
 * a certain label, or when you want to export a form as JSON.
 * <p>
 * Use the register method to install this provider as high priority text provider which will be called before any other
 * text provider.
 */
@IgnoreBean
public class TextKeyTextProviderService implements ITextProviderService {

  private static final Logger LOG = LoggerFactory.getLogger(TextKeyTextProviderService.class);

  @Override
  public String getText(Locale locale, String key, String... messageArguments) {
    return "${textKey:" + key + "}";
  }

  @Override
  public Map<String, String> getTextMap(Locale locale) {
    return Collections.emptyMap();
  }

  public static void register() {
    LOG.info("Register TextKeyTextProviderService with high priority. ScoutTexts will return text keys instead of localized texts");
    BeanMetaData beanData = new BeanMetaData(TextKeyTextProviderService.class).withOrder(Double.MIN_VALUE + 1);
    Platform.get().getBeanManager().registerBean(beanData);
    BEANS.get(ScoutTexts.class).reloadTextProviders();
  }

  public static void unregister() {
    LOG.info("Unregistered TextKeyTextProviderService. ScoutTexts will work as usual");
    Platform.get().getBeanManager().unregisterClass(TextKeyTextProviderService.class);
    BEANS.get(ScoutTexts.class).reloadTextProviders();
  }

  /**
   * Must be public to be found by BeanManager.
   */
  public static class P_Initializer implements IPlatformListener {

    @Override
    public void stateChanged(PlatformEvent event) {
      if (State.PlatformStarted == event.getState() && CONFIG.getPropertyValue(TextProvidersShowKeysProperty.class)) {
        register();
      }
    }
  }

}
