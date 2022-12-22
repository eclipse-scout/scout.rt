import {Outline, OutlineModel} from '@eclipse-scout/core';
import {PersonTablePage} from '../index';

export default (): OutlineModel => ({
  id: '${simpleArtifactName}.DataOutline',
  title: '${symbol_dollar}{textKey:MyDataOutlineTitle}',
  objectType: Outline,
  nodes: [
    {
      objectType: PersonTablePage
    }
  ]
});

export type DataOutlineWidgetMap = {
  '${simpleArtifactName}.DataOutline': Outline;
};
