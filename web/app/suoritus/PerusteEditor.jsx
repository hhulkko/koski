import React from 'baret'
import Atom from 'bacon.atom'
import Bacon from 'baconjs'
import R from 'ramda'
import {modelData} from '../editor/EditorModel.js'
import {pushModelValue} from '../editor/EditorModel'
import {wrapOptional} from '../editor/EditorModel'
import {StringEditor} from '../editor/StringEditor'
import {PerusteDropdown} from './PerusteDropdown'

export const PerusteEditor = ({model}) => {
  if (!model.context.edit) return <StringEditor model={model}/>
  model = wrapOptional(model)
  let perusteAtom = Atom(modelData(model))
  perusteAtom.filter(R.identity).changes().onValue(diaarinumero => pushModelValue(model, { data: diaarinumero }))
  return <PerusteDropdown {...{perusteAtom, suoritusTyyppiP: Bacon.constant(modelData(model.context.suoritus, 'tyyppi'))}}/>
}
PerusteEditor.handlesOptional=() => true