import React from 'baret'
import {modelData, modelTitle} from '../editor/EditorModel.js'
import Link from '../components/Link'
import {yearFromIsoDateString} from '../date/date'
import {näytettävätPäätasonSuoritukset} from '../opiskeluoikeus/OpiskeluoikeusEditor'
import {modelItems} from '../editor/EditorModel'

export default ({ opiskeluoikeudet, selectedIndex }) => {
  return (<ul className="oppilaitokset-nav">
    {
      opiskeluoikeudet.map((oppilaitos, oppilaitosIndex) => {
        let selected = oppilaitosIndex == selectedIndex
        let className = selected ? 'selected' : ''
        let content = (<div>
          <div className="oppilaitos-nav">{modelTitle(oppilaitos, 'oppilaitos')}</div>
          <ul className="oppilaitokset">
            {
              modelItems(oppilaitos, 'opiskeluoikeudet').map((opiskeluoikeus, opiskeluoikeusIndex) =>
                (<li key={opiskeluoikeusIndex}>
                  <ul className="opiskeluoikeudet">
                    {
                      näytettävätPäätasonSuoritukset(opiskeluoikeus).map((suoritusRyhmä, suoritusIndex) =>
                        (<li className="opiskeluoikeus" key={opiskeluoikeusIndex + '-' + suoritusIndex}>
                          <span className="koulutus inline-text">{ modelTitle(suoritusRyhmä.suoritukset[0], 'tyyppi') }</span>
                          { modelData(opiskeluoikeus, 'alkamispäivä')
                            ? <span className="inline-text">
                                      <span className="alku pvm">{yearFromIsoDateString(modelTitle(opiskeluoikeus, 'alkamispäivä'))}</span>{'-'}
                              <span className="loppu pvm">{yearFromIsoDateString(modelTitle(opiskeluoikeus, 'päättymispäivä'))}{','}</span>
                                    </span>
                            : null
                          }
                          <span className="tila">{ modelTitle(opiskeluoikeus, 'tila.opiskeluoikeusjaksot.-1.tila') }</span>
                        </li>)
                      )
                    }
                  </ul>
                </li>)
              )
            }
          </ul>
        </div>)
        return (
          <li className={className} key={oppilaitosIndex}>
            { selected ? content : <Link href={'?oppilaitos=' + modelData(oppilaitos, 'oppilaitos.oid')}>{content}</Link> }
          </li>)
      })}
  </ul>)
}