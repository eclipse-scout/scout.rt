/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.mapping;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;

/**
 * {@link DoEntityMappings} container class allowing to map from a DO entity to a peer and vice versa.
 * <p>
 * Implementation are usually application scoped so that the mappings are only collected once.
 * <p>
 * Definition example:
 *
 * <pre>
 * &#64;ApplicationScoped
 * public static class LoremFormMapper extends AbstractDoEntityMapper<LoremDo, LoremForm> {
 *
 *   &#64;Override
 *   protected void initMappings(DoEntityMappings<LoremDo, LoremForm> mappings) {
 *     mappings
 *         .with(LoremDo::key, LoremForm::getKey, LoremForm::setKey)
 *         .withHolder(LoremDo::name, LoremForm::getNameField);
 *   }
 * }
 * </pre>
 * <p>
 * Usage Example within an {@link org.eclipse.scout.rt.client.ui.form.AbstractFormHandler}
 * <ul>
 * <li>execLoad: <code>BEANS.get(LoremFormMapper.class).fromDo(data, (LoremForm) form);</code>
 * <li>execStore: <code>BEANS.get(LoremFormMapper.class).&#116;oDo((LoremForm)form, data);</code>
 * </ul>
 */
// Not marked @ApplicationScoped on purpose because subclass might decide for itself if this is desired
public abstract class AbstractDoEntityMapper<DO_ENTITY extends IDoEntity, PEER> {

  protected DoEntityMappings<DO_ENTITY, PEER> m_mappings;

  public AbstractDoEntityMapper() {
    reset();
  }

  /**
   * Extends the initialized mappings by mappings provided via {@link IDoEntityMapperExtension}.
   */
  @SuppressWarnings("unchecked")
  protected void extendMappings(DoEntityMappings<DO_ENTITY, PEER> mappings) {
    BEANS.all(IDoEntityMapperExtension.class).stream()
        .filter(extension -> extension.getMapperClass().isInstance(AbstractDoEntityMapper.this)) // filter for those extensions that reference this mapper class
        .forEach(extension -> extension.extendMappings(mappings));
  }

  /**
   * Method is overridden by subclasses to initialize the mappings.
   */
  protected abstract void initMappings(DoEntityMappings<DO_ENTITY, PEER> mappings);

  /**
   * Applies the mapping from the DO entity to the target.
   */
  public void fromDo(DO_ENTITY doEntity, PEER target) {
    assertNotNull(doEntity, "doEntity is required");
    assertNotNull(target, "target is required");
    if (Platform.get().inDevelopmentMode()) {
      // Allows adding new mappings during development without the need to restart because instances are usually application scoped.
      reset();
    }
    m_mappings.fromDo(doEntity, target);
  }

  /**
   * Applies the mapping from the source to the DO entity.
   */
  public void toDo(PEER source, DO_ENTITY doEntity) {
    assertNotNull(source, "source is required");
    assertNotNull(doEntity, "doEntity is required");
    if (Platform.get().inDevelopmentMode()) {
      // Allows adding new mappings during development without the need to restart because instances are usually application scoped.
      reset();
    }
    m_mappings.toDo(source, doEntity);
  }

  /**
   * Resets the mappings, i.e. recreating all mapping entries.
   */
  protected void reset() {
    DoEntityMappings<DO_ENTITY, PEER> mappings = new DoEntityMappings<>();
    initMappings(mappings);
    extendMappings(mappings);
    m_mappings = mappings; // reassign in order to prevent ConcurrentModificationException when used in multiple threads concurrently
  }
}
