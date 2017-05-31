package fi.oph.koski.localization

import com.typesafe.config.Config
import fi.oph.koski.cache.{Cache, CacheManager, KeyValueCache}
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, VirkailijaHttpClient}
import fi.oph.koski.json.Json._
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.localization.LocalizedString.sanitize
import fi.oph.koski.log.Logging
import org.json4s._

trait LocalizationRepository extends Logging {

  def localizations(): Map[String, LocalizedString]

  def fetchLocalizations(): JValue

  def createOrUpdate(localizations: List[UpdateLocalization])
}

abstract class CachedLocalizationService(implicit cacheInvalidator: CacheManager) extends LocalizationRepository {
  protected val cache = KeyValueCache[String, Map[String, LocalizedString]](
    Cache.cacheAllRefresh("LocalizationRepository.localizations", durationSeconds = 60, maxSize = 1),
    key => fetch()
  )

  def localizations(): Map[String, LocalizedString] = {
    cache("key")
  }

  private def fetch(): Map[String, LocalizedString] = {
    val localized: Map[String, Map[String, String]] = fetchLocalizations().extract[List[LocalizationServiceLocalization]]
      .groupBy(_.key)
      .mapValues(_.map(v => (v.locale, v.value)).toMap)

    readResource("/localization/default-texts.json").extract[Map[String, String]].map {
      case (key, value) =>
        localized.get(key).map(l => (key, sanitize(l).get)).getOrElse {
          logger.info(s"Localizations missing for key $key")
          (key, Finnish(value))
        }
    }
  }
}

object LocalizationRepository {
  def apply(config: Config)(implicit cacheInvalidator: CacheManager): LocalizationRepository = {
    config.getString("localization.url") match {
      case "mock" =>
        new MockLocalizationRepository
      case url: Any =>
        new RemoteLocalizationRepository(VirkailijaHttpClient(config.getString("opintopolku.virkailija.username"), config.getString("opintopolku.virkailija.password"), config.getString("opintopolku.virkailija.url"), "/lokalisointi"))
    }
  }
}

class MockLocalizationRepository(implicit cacheInvalidator: CacheManager) extends CachedLocalizationService {
  override def fetchLocalizations(): JValue = readResource("/mockdata/lokalisointi/koski.json")

  override def createOrUpdate(localizations: List[UpdateLocalization]): Unit = ???
}

class RemoteLocalizationRepository(http: Http)(implicit cacheInvalidator: CacheManager) extends CachedLocalizationService {
  override def fetchLocalizations(): JValue = runTask(http.get(uri"/lokalisointi/cxf/rest/v1/localisation?category=koski")(Http.parseJson[JValue]))

  override def createOrUpdate(localizations: List[UpdateLocalization]): Unit = {
    cache.strategy.invalidateCache()
    runTask(http.post(uri"/lokalisointi/cxf/rest/v1/localisation/update", localizations)(json4sEncoderOf[List[UpdateLocalization]])(Http.unitDecoder))
  }
}

case class UpdateLocalization(
  locale: String,
  key: String,
  value: String,
  category: String = "koski"
)

case class LocalizationServiceLocalization(
  id: Integer,
  locale: String,
  category: String,
  key: String,
  value: String
)

