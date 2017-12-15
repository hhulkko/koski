import React from 'baret'
import Text from '../i18n/Text'
import {KurssitEditor} from './KurssitEditor'
import {modelItems} from './EditorModel'
import {arvioituTaiVahvistettu} from './Suoritus'

export default ({model}) => {
  let suorituksiaTehty = modelItems(model, 'osasuoritukset').filter(arvioituTaiVahvistettu).length > 0
  return (<div className="kurssit">
    {(model.context.edit || suorituksiaTehty) && <h5><Text name="Kurssit"/></h5>}
    <KurssitEditor model={model}/>
  </div>)
}