import {Outline} from '@eclipse-scout/core';
import {PersonTablePage} from '../index';

export default () => ({
  id: '${simpleArtifactName}.DataOutline',
  title: '${symbol_dollar}{textKey:MyDataOutlineTitle}',
  objectType: Outline,
  nodes: [
    {
      objectType: PersonTablePage
    }
  ]
});
