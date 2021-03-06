import React from 'baret'
import {modelData, modelLookup, modelTitle} from '../editor/EditorModel.js'
import {Editor} from '../editor/Editor'
import {PropertiesEditor, shouldShowProperty} from '../editor/PropertiesEditor'
import {
  modelErrorMessages,
  modelItems,
  modelProperties,
  modelProperty,
  optionalPrototypeModel,
  pushRemoval
} from '../editor/EditorModel'
import R from 'ramda'
import {buildClassNames} from '../components/classnames'
import {accumulateExpandedState} from '../editor/ExpandableItems'
import {fixArviointi, hasArvosana, suoritusValmis, tilaText} from './Suoritus'
import {t} from '../i18n/i18n'
import Text from '../i18n/Text'
import {ammatillisentutkinnonosanryhmaKoodisto} from '../koodisto/koodistot'
import {fetchLaajuudet, YhteensäSuoritettu} from './YhteensaSuoritettu'
import UusiTutkinnonOsa from '../ammatillinen/UusiTutkinnonOsa'
import {createTutkinnonOsanSuoritusPrototype, placeholderForNonGrouped} from '../ammatillinen/TutkinnonOsa'
import {sortGradesF} from '../util/sorting'


export class Suoritustaulukko extends React.Component {
  render() {
    let {suorituksetModel, parentSuoritus, nested} = this.props
    let context = suorituksetModel.context
    parentSuoritus = parentSuoritus || context.suoritus
    let suoritukset = modelItems(suorituksetModel) || []

    let suoritusProto = context.edit ? createTutkinnonOsanSuoritusPrototype(suorituksetModel) : suoritukset[0]
    let koulutustyyppi = modelData(parentSuoritus, 'koulutusmoduuli.koulutustyyppi.koodiarvo')
    let suoritustapa = modelData(parentSuoritus, 'suoritustapa')
    let isAmmatillinenTutkinto = parentSuoritus.value.classes.includes('ammatillisentutkinnonsuoritus')
    if (suoritukset.length === 0 && !context.edit) return null
    let isAmmatillinenPerustutkinto = koulutustyyppi === '1'

    const {isExpandedP, allExpandedP, toggleExpandAll, setExpanded} = accumulateExpandedState({
      suoritukset,
      filter: s => suoritusProperties(s).length > 0,
      component: this
    })

    let grouped, groupIds, groupTitles

    if (isAmmatillinenTutkinto && isAmmatillinenPerustutkinto && suoritustapa && suoritustapa.koodiarvo === 'ops') {
      grouped = R.groupBy(s => modelData(s, 'tutkinnonOsanRyhmä.koodiarvo') || placeholderForNonGrouped)(suoritukset)
      groupTitles = R.merge(ammatillisentutkinnonosanryhmaKoodisto, { [placeholderForNonGrouped] : t('Muut suoritukset')})
      groupIds = R.keys(grouped).sort()
      if (context.edit) {
        // Show the empty groups too
        groupIds = R.uniq(R.keys(ammatillisentutkinnonosanryhmaKoodisto).concat(groupIds))
      }
    } else {
      grouped = { [placeholderForNonGrouped] : suoritukset }
      groupTitles = { [placeholderForNonGrouped] : t(modelProperty(suoritukset[0] || suoritusProto, 'koulutusmoduuli').title) }
      groupIds = [placeholderForNonGrouped]
    }

    const laajuudetP = fetchLaajuudet(parentSuoritus, groupIds)

    let samaLaajuusYksikkö = suoritukset.every((s, i, xs) => modelData(s, 'koulutusmoduuli.laajuus.yksikkö.koodiarvo') === modelData(xs[0], 'koulutusmoduuli.laajuus.yksikkö.koodiarvo'))
    let laajuusModel = modelLookup(suoritusProto, 'koulutusmoduuli.laajuus')
    if (laajuusModel && laajuusModel.optional && !modelData(laajuusModel)) laajuusModel = optionalPrototypeModel(laajuusModel)
    let laajuusYksikkö = t(modelData(laajuusModel, 'yksikkö.lyhytNimi'))
    let showTila = !näyttötutkintoonValmistava(parentSuoritus)
    let showExpandAll = suoritukset.some(s => suoritusProperties(s).length > 0)
    let columns = [TutkintokertaColumn, SuoritusColumn, PakollisuusColumn, LaajuusColumn, ArvosanaColumn].filter(column => column.shouldShow({parentSuoritus, suorituksetModel, suoritukset, suoritusProto, context}))

    return !suoritustapa && context.edit && isAmmatillinenTutkinto
        ? <Text name="Valitse ensin tutkinnon suoritustapa" />
        : (suoritukset.length > 0 || context.edit) && (
          <div className="suoritus-taulukko">
            <table>
              <thead>
              <tr>
                <th className="suoritus">
                  {showExpandAll &&
                  <div>
                    {allExpandedP.map(allExpanded => (
                      <a className={'expand-all button' + (allExpanded ? ' expanded' : '')} onClick={toggleExpandAll}>
                        <Text name={allExpanded ? 'Sulje kaikki' : 'Avaa kaikki'}/>
                      </a>)
                    )}
                  </div>
                  }
                </th>
              </tr>
              </thead>
              {
                groupIds.flatMap((groupId, i) => suoritusGroup(groupId, i))
              }
            </table>
          </div>)

    function suoritusGroup(groupId, i) {
      const items = (grouped[groupId] || [])

      return [
        <tbody key={'group-' + i} className={`group-header ${groupId}`}>
          <tr>
            { columns.map(column => column.renderHeader({suoritusProto, laajuusYksikkö, groupTitles, groupId})) }
          </tr>
        </tbody>,
        items.map((suoritus, j) => suoritusEditor(suoritus, i * 100 + j, groupId)),
        context.edit && <tbody key={'group-' + i + '-new'} className={'uusi-tutkinnon-osa ' + groupId}>
          <tr>
            <td colSpan="4">
              <UusiTutkinnonOsa suoritus={parentSuoritus}
                                suoritusPrototype={createTutkinnonOsanSuoritusPrototype(suorituksetModel, groupId)}
                                suorituksetModel={suorituksetModel}
                                suoritukset={items}
                                groupId={groupId}
                                setExpanded={setExpanded}
                                groupTitles={groupTitles}
              />
            </td>
          </tr>
        </tbody>,
        !nested && !näyttötutkintoonValmistava(parentSuoritus) && !ylioppilastutkinto(parentSuoritus) && <tbody key={'group- '+ i + '-footer'} className="yhteensä">
          <tr><td>
            <YhteensäSuoritettu osasuoritukset={items} laajuusP={laajuudetP.map(l => l[groupId])} laajuusYksikkö={laajuusYksikkö}/>
          </td></tr>
        </tbody>
      ]
    }

    function suoritusEditor(suoritus, key, groupId) {
      return (<TutkinnonOsanSuoritusEditor baret-lift
                                           model={suoritus} showScope={!samaLaajuusYksikkö} showTila={showTila}
                                           expanded={isExpandedP(suoritus)} onExpand={setExpanded(suoritus)} key={key}
                                           groupId={groupId} columns={columns}/>)
    }
  }
}

let näyttötutkintoonValmistava = suoritus => suoritus.value.classes.includes('nayttotutkintoonvalmistavankoulutuksensuoritus')
let ylioppilastutkinto = suoritus => suoritus.value.classes.includes('ylioppilastutkinnonsuoritus')

export class TutkinnonOsanSuoritusEditor extends React.Component {
  render() {
    let {model, showScope, showTila, onExpand, expanded, groupId, columns} = this.props
    let properties = suoritusProperties(model)
    let displayProperties = properties.filter(p => p.key !== 'osasuoritukset')
    let hasProperties = displayProperties.length > 0
    let osasuoritukset = modelLookup(model, 'osasuoritukset')

    return (<tbody className={buildClassNames(['tutkinnon-osa', (expanded && 'expanded'), (groupId)])}>
    <tr>
      {columns.map(column => column.renderData({model, showScope, showTila, onExpand, hasProperties, expanded}))}
      {
        model.context.edit && (
          <td className="remove">
            <a className="remove-value" onClick={() => pushRemoval(model)}/>
          </td>
        )
      }
    </tr>
    {
      modelErrorMessages(model).map((error, i) => <tr key={'error-' + i} className="error"><td colSpan="42" className="error">{error}</td></tr>)
    }
    {
      expanded && hasProperties && (<tr className="details" key="details">
        <td colSpan="4">
          <PropertiesEditor model={model} properties={displayProperties}/>
        </td>
      </tr>)
    }
    {
      expanded && osasuoritukset && osasuoritukset.value && (<tr className="osasuoritukset" key="osasuoritukset">
        <td colSpan="4">
          <Suoritustaulukko parentSuoritus={model} nested={true} suorituksetModel={ osasuoritukset }/>
        </td>
      </tr>)
    }
    </tbody>)
  }
}

const suoritusProperties = suoritus => {
  let properties = modelProperties(modelLookup(suoritus, 'koulutusmoduuli'), p => p.key === 'kuvaus').concat(
    suoritus.context.edit
      ? modelProperties(suoritus, p => ['näyttö', 'tunnustettu', 'lisätiedot'].includes(p.key))
      : modelProperties(suoritus, p => !(['koulutusmoduuli', 'arviointi', 'tutkinnonOsanRyhmä', 'tutkintokerta'].includes(p.key)))
      .concat(modelProperties(modelLookup(suoritus, 'arviointi.-1'), p => !(['arvosana', 'päivä', 'arvioitsijat']).includes(p.key)))
  )
  return properties.filter(shouldShowProperty(suoritus.context))
}

const TutkintokertaColumn = {
  shouldShow: ({parentSuoritus}) => ylioppilastutkinto(parentSuoritus),
  renderHeader: () => {
    return <td key="tutkintokerta" className="tutkintokerta"><Text name="Tutkintokerta"/></td>
  },
  renderData: ({model}) => (<td key="tutkintokerta" className="tutkintokerta">
    <Editor model={model} path="tutkintokerta.vuosi" compact="true"/>
    {' '}
    <Editor model={model} path="tutkintokerta.vuodenaika" compact="true"/>
  </td>)
}

const SuoritusColumn = {
  shouldShow : () => true,
  renderHeader: ({groupTitles, groupId}) => <td key="suoritus">{groupTitles[groupId]}</td>,
  renderData: ({model, showTila, onExpand, hasProperties, expanded}) => {
    let nimi = modelTitle(model, 'koulutusmoduuli')

    return (<td key="suoritus" className="suoritus">
      <a className={ hasProperties ? 'toggle-expand' : 'toggle-expand disabled'}
         onClick={() => onExpand(!expanded)}>{ expanded ? '' : ''}</a>
      {showTila && <span className="tila" title={tilaText(model)}>{suorituksenTilaSymbol(model)}</span>}
      {
        hasProperties
          ? <a className="nimi" onClick={() => onExpand(!expanded)}>{nimi}</a>
          : <span className="nimi">{nimi}</span>
      }

    </td>)
  }
}

const PakollisuusColumn = {
  shouldShow: ({parentSuoritus, suoritukset, suoritusProto}) => !näyttötutkintoonValmistava(parentSuoritus) && (modelData(suoritusProto, 'koulutusmoduuli.pakollinen') !== undefined || suoritukset.find(s => modelData(s, 'koulutusmoduuli.pakollinen') !== undefined) !== undefined),
  renderHeader: () => <td key="pakollisuus" className="pakollisuus"><Text name="Pakollisuus"/></td>,
  renderData: ({model}) => <td key="pakollisuus" className="pakollisuus"><Editor model={model} path="koulutusmoduuli.pakollinen"/></td>
}

const LaajuusColumn = {
  shouldShow: ({parentSuoritus, suoritukset, suorituksetModel, context}) => (!näyttötutkintoonValmistava(parentSuoritus) && (context.edit
    ? modelProperty(createTutkinnonOsanSuoritusPrototype(suorituksetModel), 'koulutusmoduuli.laajuus') !== null
    : suoritukset.find(s => modelData(s, 'koulutusmoduuli.laajuus.arvo') !== undefined) !== undefined)),
  renderHeader: ({laajuusYksikkö}) => {
    return <td key="laajuus" className="laajuus"><Text name="Laajuus"/>{((laajuusYksikkö && ' (' + laajuusYksikkö + ')') || '')}</td>
  },
  renderData: ({model, showScope}) => <td key="laajuus" className="laajuus"><Editor model={model} path="koulutusmoduuli.laajuus" compact="true" showReadonlyScope={showScope}/></td>
}

const ArvosanaColumn = {
  shouldShow: ({parentSuoritus, suoritukset, context}) => !näyttötutkintoonValmistava(parentSuoritus) && (context.edit || suoritukset.find(hasArvosana) !== undefined),
  renderHeader: () => <td key="arvosana" className="arvosana"><Text name="Arvosana"/></td>,
  renderData: ({model}) => {
    let arvosanaModel = modelLookup(fixArviointi(model), 'arviointi.-1.arvosana')
    // TODO, Parempi tapa erotella eri koodistoista tulevat arvot
    let sortByKoodistoUriAndGrade = (grades) => {
      let sort = (x, y) => {
        if (x.data.koodistoUri < y.data.koodistoUri) {
          return -1
        }
        if (x.data.koodistoUri > y.data.koodistoUri) {
          return 1
        }
        return sortGradesF(x, y)
      }
      return grades.sort(sort)
    }
    return arvosanaModel && <td key="arvosana" className="arvosana">
        <Editor model={arvosanaModel} showEmptyOption="true" sortBy={sortByKoodistoUriAndGrade} />
      </td>
  }
}

export const suorituksenTilaSymbol = (suoritus) => suoritusValmis(suoritus) ? '' : ''