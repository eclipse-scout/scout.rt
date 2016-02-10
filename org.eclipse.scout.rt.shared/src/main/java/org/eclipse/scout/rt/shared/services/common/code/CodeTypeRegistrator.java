package org.eclipse.scout.rt.shared.services.common.code;

import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * registers all {@link ICodeType} beans using the {@link ICodeService} for instance caching between client and server
 */
public class CodeTypeRegistrator implements IPlatformListener {

  private static final Logger LOG = LoggerFactory.getLogger(CodeTypeRegistrator.class);

  @Override
  public void stateChanged(PlatformEvent e) throws PlatformException {
    if (e.getState() == IPlatform.State.BeanManagerPrepared) {
      IBeanManager beanManager = e.getSource().getBeanManager();
      Set<Class<? extends ICodeType<?, ?>>> classes = BEANS.get(CodeTypeClassInventory.class).getClasses();
      for (Class<? extends ICodeType<?, ?>> c : classes) {
        LOG.debug("Register {}", c.getName());
        beanManager.registerBean(
            new BeanMetaData(c)
                .withProducer(new CodeTypeProducer()));
      }
      LOG.info("{} code type classes registered.", classes.size());
    }
  }

}
