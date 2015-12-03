/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.data.model.fixture;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;

public abstract class AbstractPersonEntity extends AbstractDataModelEntity {
  private static final long serialVersionUID = 1L;

  @Override
  protected String getConfiguredText() {
    return "Person";
  }

  @Override
  protected void injectAttributesInternal(OrderedCollection<IDataModelAttribute> attributes) {
    super.injectAttributesInternal(attributes);
    CustomDataModelExtension.injectAttributes(this, attributes);
  }

  @Override
  protected void injectEntitiesInternal(OrderedCollection<IDataModelEntity> entities) {
    super.injectEntitiesInternal(entities);
    CustomDataModelExtension.injectEntities(this, entities);
  }

  @Order(10)
  public class LastNameAttribute extends AbstractDataModelAttribute {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getConfiguredText() {
      return "LastName";
    }

    @Override
    protected int getConfiguredType() {
      return IDataModelAttribute.TYPE_STRING;
    }
  }

  @Order(10)
  public class FirstNameAttribute extends AbstractDataModelAttribute {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getConfiguredText() {
      return "FirstName";
    }

    @Override
    protected int getConfiguredType() {
      return IDataModelAttribute.TYPE_STRING;
    }
  }

  @Order(10)
  public class BirthdateAttribute extends AbstractDataModelAttribute {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getConfiguredText() {
      return "Birthdate";
    }

    @Override
    protected int getConfiguredType() {
      return IDataModelAttribute.TYPE_DATE;
    }
  }

  @Order(10)
  public class PrimaryAddressEntity extends AbstractAddressEntity {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getConfiguredText() {
      return "PrimaryAddress";
    }

    @Override
    protected boolean getConfiguredOneToMany() {
      return false;
    }
  }

  @Order(10)
  public class LegalAddressEntity extends AbstractAddressEntity {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getConfiguredText() {
      return "LegalAddress";
    }
  }

  @Order(10)
  public class AccountManagerEntity extends AbstractPersonEntity {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getConfiguredText() {
      return "AccountManager";
    }

    @Override
    protected boolean getConfiguredOneToMany() {
      return false;
    }
  }

  @Order(10)
  public class CompanyEntity extends AbstractCompanyEntity {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getConfiguredText() {
      return "Company";
    }

    @Override
    protected boolean getConfiguredOneToMany() {
      return false;
    }
  }
}
