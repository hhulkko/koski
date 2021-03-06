package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation._


@Description("Aikuisten perusopetuksen opiskeluoikeus")
case class AikuistenPerusopetuksenOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  @Hidden
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None,
  @Description("Oppijan oppimäärän päättymispäivä")
  päättymispäivä: Option[LocalDate] = None,
  tila: AikuistenPerusopetuksenOpiskeluoikeudenTila,
  lisätiedot: Option[AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot] = None,
  suoritukset: List[AikuistenPerusopetuksenPäätasonSuoritus],
  @KoodistoKoodiarvo("aikuistenperusopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("aikuistenperusopetus", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
}

case class AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot(
  @KoodistoUri("perusopetuksentukimuoto")
  @Description("""Oppilaan saamat laissa säädetyt tukimuodot""")
  tukimuodot: Option[List[Koodistokoodiviite]] = None,
  @Description("""Tehostetun tuen päätös alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, että päätöstä ei ole tehty. Rahoituksen laskennassa käytettävä tieto.""")
  @SensitiveData
  @OksaUri("tmpOKSAID511", "tehostettu tuki")
  tehostetunTuenPäätös: Option[Päätösjakso] = None,
  @Description("""Opiskelu ulkomailla huoltajan ilmoituksesta, alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole ulkomailla.""")
  ulkomailla: Option[Päätösjakso] = None,
  @Description("""Oppilas on vuosiluokkiin sitomattomassa opetuksessa (true/false)""")
  @DefaultValue(false)
  @Title("Vuosiluokkiin sitomaton opetus")
  @SensitiveData
  vuosiluokkiinSitoutumatonOpetus: Boolean = false,
  @Description("""Onko oppija vammainen. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.""")
  @SensitiveData
  vammainen: Option[List[Aikajakso]] = None,
  @Description("Onko oppija vaikeasti vammainen. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData
  vaikeastiVammainen: Option[List[Aikajakso]] = None,
  @Description("""Oppilaalla on majoitusetu. Rahoituksen laskennassa käytettävä tieto.""")
  majoitusetu: Option[Päätösjakso] = None,
  @Description("""Oppilaalla on oikeus maksuttomaan asuntolapaikkaan. Rahoituksen laskennassa käytettävä tieto.""")
  oikeusMaksuttomaanAsuntolapaikkaan: Option[Päätösjakso] = None,
  @Description("Sisäoppilaitosmuotoinen majoitus, aloituspäivä ja loppupäivä. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto")
  sisäoppilaitosmainenMajoitus: Option[List[Aikajakso]] = None
) extends OpiskeluoikeudenLisätiedot

trait AikuistenPerusopetuksenPäätasonSuoritus extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with MonikielinenSuoritus with Suorituskielellinen

case class AikuistenPerusopetuksenOppimääränSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: AikuistenPerusopetus,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suoritustapa: Koodistokoodiviite,
  suorituskieli: Koodistokoodiviite,
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  override val osasuoritukset: Option[List[AikuistenPerusopetuksenOppiaineenSuoritus]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("aikuistenperusopetuksenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("aikuistenperusopetuksenoppimaara", koodistoUri = "suorituksentyyppi")
) extends AikuistenPerusopetuksenPäätasonSuoritus with PerusopetuksenOppimääränSuoritus

@Description("Aikuisten perusopetuksen tunnistetiedot")
case class AikuistenPerusopetus(
 perusteenDiaarinumero: Option[String],
 @KoodistoKoodiarvo("201101")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("201101", koodistoUri = "koulutus"),
 koulutustyyppi: Option[Koodistokoodiviite] = None
) extends Perusopetus

@Description("Perusopetuksen oppiaineen suoritus osana aikuisten perusopetuksen oppimäärän suoritusta")
case class AikuistenPerusopetuksenOppiaineenSuoritus(
  koulutusmoduuli: PerusopetuksenOppiaine,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Title("Kurssit")
  override val osasuoritukset: Option[List[AikuistenPerusopetuksenKurssinSuoritus]] = None,
  @KoodistoKoodiarvo("aikuistenperusopetuksenoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "aikuistenperusopetuksenoppiaine", koodistoUri = "suorituksentyyppi")
) extends PerusopetuksenOppiaineenSuoritus with Vahvistukseton with MahdollisestiSuorituskielellinen

case class AikuistenPerusopetuksenKurssinSuoritus(
  @Description("Aikuisten perusopetuksen kurssin tunnistetiedot")
  koulutusmoduuli: AikuistenPerusopetuksenKurssi,
  @FlattenInUI
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("aikuistenperusopetuksenkurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "aikuistenperusopetuksenkurssi", koodistoUri = "suorituksentyyppi")
) extends KurssinSuoritus with MahdollisestiSuorituskielellinen

sealed trait AikuistenPerusopetuksenKurssi extends Koulutusmoduuli {
  def laajuus: Option[LaajuusVuosiviikkotunneissa]
}

case class PaikallinenAikuistenPerusopetuksenKurssi(
  @FlattenInUI
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends AikuistenPerusopetuksenKurssi with PaikallinenKoulutusmoduuli with StorablePreference {
  def kuvaus: LocalizedString = LocalizedString.empty
}

@Title("Aikuisten perusopetuksen opetussuunnitelman 2015 mukainen kurssi")
@OnlyWhen("../../../../../koulutusmoduuli/perusteenDiaarinumero","19/011/2015")
@OnlyWhen("../../../koulutusmoduuli/perusteenDiaarinumero", "19/011/2015")
@OnlyWhen("../..", None) // allow standalone deserialization
case class ValtakunnallinenAikuistenPerusopetuksenKurssi2015(
  @KoodistoUri("aikuistenperusopetuksenkurssit2015")
  @Title("Nimi")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends AikuistenPerusopetuksenKurssi with KoodistostaLöytyväKoulutusmoduuli

@Title("Aikuisten perusopetuksen päättövaiheen opetussuunnitelman 2017 mukainen kurssi")
@OnlyWhen("../../../../../koulutusmoduuli/perusteenDiaarinumero", "OPH-1280-2017")
@OnlyWhen("../../../koulutusmoduuli/perusteenDiaarinumero", "OPH-1280-2017")
@OnlyWhen("../..", None) // allow standalone deserialization
case class ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017(
  @KoodistoUri("aikuistenperusopetuksenpaattovaiheenkurssit2017")
  @Title("Nimi")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends AikuistenPerusopetuksenKurssi with KoodistostaLöytyväKoulutusmoduuli

@Description("Perusopetuksen yksittäisen oppiaineen oppimäärän suoritus erillisenä kokonaisuutena")
case class PerusopetuksenOppiaineenOppimääränSuoritus(
  @Description("Päättötodistukseen liittyvät oppiaineen suoritukset")
  @Title("Oppiaine")
  @FlattenInUI
  koulutusmoduuli: PerusopetuksenOppiaine,
  toimipiste: OrganisaatioWithOid,
  @Title("Arvosana")
  @FlattenInUI
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  override val vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suoritustapa: Koodistokoodiviite,
  suorituskieli: Koodistokoodiviite,
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @Title("Kurssit")
  override val osasuoritukset: Option[List[AikuistenPerusopetuksenKurssinSuoritus]] = None,
  @KoodistoKoodiarvo("perusopetuksenoppiaineenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenoppiaineenoppimaara", koodistoUri = "suorituksentyyppi")
) extends AikuistenPerusopetuksenPäätasonSuoritus with OppiaineenSuoritus with Todistus with SuoritustavallinenPerusopetuksenSuoritus

@Description("Ks. tarkemmin perusopetuksen opiskeluoikeuden tilat: [confluence](https://confluence.csc.fi/display/OPHPALV/KOSKI+opiskeluoikeuden+tilojen+selitteet+koulutusmuodoittain#KOSKIopiskeluoikeudentilojenselitteetkoulutusmuodoittain-Perusopetus)")
case class AikuistenPerusopetuksenOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[AikuistenPerusopetuksenOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class AikuistenPerusopetuksenOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: Koodistokoodiviite,
  @Description("Opintojen rahoitus")
  @KoodistoUri("opintojenrahoitus")
  @KoodistoKoodiarvo("1")
  @KoodistoKoodiarvo("6")
  override val opintojenRahoitus: Option[Koodistokoodiviite] = None
) extends KoskiOpiskeluoikeusjakso
