import React from 'react'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {Editor} from './Editor.jsx'
import {t} from '../i18n.js'
import {suorituksenTyyppi} from './Suoritus'

export class KoulutusmoduuliEditor extends React.Component {
  render() {
    let { model } = this.props
    let overrideEdit = model.context.editAll ? true : false
    let suoritusTyyppi = suorituksenTyyppi(model.context.suoritus)
    let isEsiopetus = suoritusTyyppi === 'esiopetuksensuoritus'
    let propertyFilter  = p => {
      let excludedProperties = ['tunniste', 'perusteenDiaarinumero', 'pakollinen']
      let esiopetusKuvaus = isEsiopetus && p.key === 'kuvaus'
      return !excludedProperties.includes(p.key) && !esiopetusKuvaus
    }
    return (<span className="koulutusmoduuli">
      <span className="tunniste">
        {
          ['aikuistenperusopetuksenoppimaara', 'aikuistenperusopetuksenoppimaaranalkuvaihe'].includes(suoritusTyyppi)
            ? <Editor model={model.context.suoritus} path="tyyppi" edit={false}/>
            : <Editor model={model} path="tunniste" edit={overrideEdit}/>
        }
      </span>
      <span className="diaarinumero"><span className="value">
        <Editor model={model} path="perusteenDiaarinumero" placeholder={t('Perusteen diaarinumero')} edit={model.context.edit && !isEsiopetus} />
      </span></span>
      <PropertiesEditor model={model} propertyFilter={propertyFilter} />
    </span>)
  }
}
