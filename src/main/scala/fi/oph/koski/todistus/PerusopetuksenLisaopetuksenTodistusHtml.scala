package fi.oph.koski.todistus

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.schema._

import scala.xml.Elem

class PerusopetuksenLisaopetuksenTodistusHtml(val koulutustoimija: Option[OrganisaatioWithOid], val oppilaitos: Oppilaitos, val oppijaHenkilö: Henkilötiedot, val todistus: PerusopetuksenLisäopetuksenSuoritus)(implicit val user: KoskiSession, val localizationRepository: LocalizationRepository)
      extends PeruskoulunTodistusHtml[PerusopetuksenLisäopetuksenOppiaineenSuoritus] {
  def oppiaineet = todistus.osasuoritukset.toList.flatten.collect { case s: PerusopetuksenLisäopetuksenOppiaineenSuoritus => s }
  def title = "Todistus lisäopetuksen suorittamisesta"

  override def oppiaineetHeaderHtml: Elem =
    <tr>
      <th class="oppiaine">Yhteiset ja niihin liittyvät valinnaiset oppiaineet</th>
      <th class="laajuus">Vuosiviikko- tuntimäärä</th>
      <th class="arvosana">Arvosana</th>
      <th></th>
      <th class="arvosana-korotus">Perusopetuksen päättöarvosanojen korottaminen</th>
    </tr>

  override def oppiainerivitHtml(oppiaine: Aine, nimi: String, rowClass: String): Elem = {
    val korotus = oppiaine.suoritus.korotus
    <tr class={rowClass}>
      <td class="oppiaine">
        {nimi}
      </td>
      <td class="laajuus">
        {oppiaine.suoritus.koulutusmoduuli.laajuus.map(_.arvo).getOrElse("")}
      </td>
      <td class="arvosana-kirjaimin">
        {i(oppiaine.suoritus.arvosanaKirjaimin).capitalize}
      </td>
      <td class="arvosana-numeroin">
        {if (!korotus) numeerinenArvosanaHtml(oppiaine.suoritus)}
      </td>
      <td class="arvosana-korotus">
        {if (korotus) numeerinenArvosanaHtml(oppiaine.suoritus)}
      </td>
    </tr>
  }
}
