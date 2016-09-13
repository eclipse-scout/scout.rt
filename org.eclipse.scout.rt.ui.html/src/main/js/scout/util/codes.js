/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.codes = {

  registry: {},

  // FIXME [awe] 6.1 - move this to a *.json file, export json in BSI CRM
  masterData: {
    // CommunicationTypeCodeType
    129790: {
      id: 129790,
      modelClass: 'com.bsiag.crm.shared.core.communication.code.CommunicationTypeCodeType',
      codes: [
        {
          id: 129752,
          modelClass: 'com.bsiag.crm.shared.core.communication.code.CommunicationTypeCodeType.ConfigurationCode',
          text: {
            'de': 'Konfiguration',
            'default': 'Configure'
          }
        },
        {
          id: 129755,
          modelClass: 'com.bsiag.crm.shared.core.communication.code.CommunicationTypeCodeType.ContactCenterCode',
          text: {
            'de': 'Contact Center',
            'default': 'Contact Center'
          }
        },
        {
          id: 129754,
          modelClass: 'com.bsiag.crm.shared.core.communication.code.CommunicationTypeCodeType.MarketingCode',
          text: {
            'de': 'Marketing',
            'default': 'Marketing'
          }
        },
        {
          id: 129753,
          modelClass: 'com.bsiag.crm.shared.core.communication.code.CommunicationTypeCodeType.SalesCode',
          text: {
            'de': 'Verkauf',
            'default': 'Sales'
          }
        }
      ]
    },
    // CommunicationStatusCodeType
    75955: {
      id: 75955,
      modelClass: 'com.bsiag.crm.shared.core.communication.CommunicationStatusCodeType',
      codes: [
        {
          id: 3513,
          modelClass: 'com.bsiag.crm.shared.core.communication.CommunicationStatusCodeType.DoneCode',
          text: {
            'de': 'Abgeschlossen',
            'default': 'Done'
          }
        },
        {
          id: 75958,
          modelClass: 'com.bsiag.crm.shared.core.communication.CommunicationStatusCodeType.InProgressCode',
          text: {
            'de': 'In Arbeit',
            'default': 'In Progress'
          }
        },
        {
          id: 3512,
          modelClass: 'com.bsiag.crm.shared.core.communication.CommunicationStatusCodeType.PlannedCode',
          text: {
            'de': 'Geplant',
            'default': 'Planned'
          }
        },
        {
          id: 75960,
          modelClass: 'com.bsiag.crm.shared.core.communication.CommunicationStatusCodeType.PendingCode',
          text: {
            'de': 'Pendent',
            'default': 'Pending'
          }
        }
      ]
    },
    // MessageChannelCodeType
    71074: {
      id: 71074,
      modelClass: 'com.bsiag.crm.shared.core.configuration.code.MessageChannelCodeType',
      codes: [
        {
          id: 100035,
          modelClass: 'com.bsiag.crm.shared.core.configuration.code.MessageChannelCodeType.ContactCode',
          text: {
            'de': 'Besuch',
            'default': 'Appointment'
          }
        },
        {
          id: 100030,
          modelClass: 'com.bsiag.crm.shared.core.configuration.code.MessageChannelCodeType.EmailCode',
          text: {
            'de': 'E-Mail',
            'default': 'E-Mail'
          }
        },
        {
          id: 104685,
          modelClass: 'com.bsiag.crm.shared.core.configuration.code.MessageChannelCodeType.EventCode',
          text: {
            'de': 'Event',
            'default': 'Event'
          }
        },
        {
          id: 104860,
          modelClass: 'com.bsiag.crm.shared.core.configuration.code.MessageChannelCodeType.PhoneCode',
          text: {
            'de': 'Anruf',
            'default': 'Phone'
          }
        },
        {
          id: 105316,
          modelClass: 'com.bsiag.crm.shared.core.configuration.code.MessageChannelCodeType.WebCode',
          text: {
            'de': 'Web',
            'default': 'Web'
          }
        }
      ]
    },
    // SynchronizationStatusCodeType, FIXME [awe tpu] 6.1 impl. this code type in BSI CRM
    12345: {
      id: 12345,
      modelClass: 'com.bsiag.crm.shared.core.configuration.code.SynchronizationStatusCodeType',
      codes: [
        {
          id: 12346,
          modelClass: 'com.bsiag.crm.shared.core.configuration.code.SynchronizationStatusCodeType.UpToDateCode',
          text: {
            'de': 'Aktuell',
            'default': 'Up to date'
          }
        },
        {
          id: 12347,
          modelClass: 'com.bsiag.crm.shared.core.configuration.code.SynchronizationStatusCodeType.ChangedCode',
          text: {
            'de': 'Geändert',
            'default': 'Changed'
          }
        },
        {
          id: 12348,
          modelClass: 'com.bsiag.crm.shared.core.configuration.code.SynchronizationStatusCodeType.ClosedCode',
          text: {
            'de': 'Abgeschlossen',
            'default': 'Closed'
          }
        },
        {
          id: 12349,
          modelClass: 'com.bsiag.crm.shared.core.configuration.code.SynchronizationStatusCodeType.OutOfDateCode',
          text: {
            'de': 'Nicht aktuell',
            'default': 'Out of date'
          }
        }
      ]
    }
  },

  _convert: function() { // FIXME [awe] 6.1 move this to bootstrap
    if (this.converted) {
      return;
    }
    var codeTypeId, codeType;
    for (codeTypeId in this.masterData) {
      codeType = new scout.CodeType();
      codeType.init(this.masterData[codeTypeId]);
      this.registry[codeType.id] = codeType;
    }
    this.converted = true;
  },

  /**
   * Returns a code for the given codeId. The codeId is a string in the following format:
   *
   * "[CodeType.id] [Code.id]"
   *
   * Examples:
   * "71074 104860"
   * "MessageChannel Phone"
   *
   * CodeType.id and Code.id are separated by a space.
   * The Code.id alone is not unique, that's why the CodeType.id must be always provided.
   *
   * You can also call this function with two arguments. In that case the first argument
   * is the codeTypeId and the second is the codeId.
   */
  get: function(vararg, codeId) {
    this._convert();
    var codeTypeId;
    if (arguments.length === 2) {
      codeTypeId = vararg;
    } else {
      var tmp = vararg.split(' ');
      if (tmp.length !== 2) {
        throw new Error('Invalid string. Must have format "[CodeType.id] [Code.id]"');
      }
      codeTypeId = tmp[0];
      codeId = tmp[1];
    }
    scout.assertParameter('codeTypeId', codeTypeId);
    scout.assertParameter('codeId', codeId);

    var codeType = this.getCodeType(codeTypeId);
    return codeType.get(codeId);
  },

  getCodeType: function(codeTypeId) {
    this._convert();
    var codeType = this.registry[codeTypeId];
    if (!codeType) {
      throw new Error('No CodeType found for id=' + codeTypeId);
    }
    return codeType;
  }

};
