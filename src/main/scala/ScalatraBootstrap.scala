import javax.servlet.ServletContext

import fi.oph.tor.config.TorProfile
import fi.oph.tor.db._
import fi.oph.tor.fixture.FixtureServlet
import fi.oph.tor.tutkinto.TutkintoServlet
import fi.oph.tor.oppija.OppijaServlet
import fi.oph.tor.oppilaitos.OppilaitosServlet
import fi.oph.tor.user.{UserRepository, UserServlet}
import fi.oph.tor.{SingleFileServlet, SuoritusServlet, TodennetunOsaamisenRekisteri}
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra._

class ScalatraBootstrap extends LifeCycle with Logging with GlobalExecutionContext with Futures {
  override def init(context: ServletContext) {
    val profile: TorProfile with GlobalExecutionContext = Option(context.getAttribute("tor.profile").asInstanceOf[String]).map(TorProfile.fromString(_)).getOrElse(TorProfile.fromSystemProperty)
    val database: TorDatabase = profile.database
    val rekisteri = new TodennetunOsaamisenRekisteri(database.db)
    implicit val userRepository = UserRepository(profile.config)

    context.mount(new SuoritusServlet(rekisteri), "/api/suoritus")
    context.mount(new OppijaServlet(profile.oppijaRepository, profile.opintoOikeusRepository, profile.tutkintoRepository, profile.oppilaitosRepository), "/api/oppija")
    context.mount(new UserServlet(profile.directoryClient, profile.userRepository), "/user")
    context.mount(new SingleFileServlet("web/static/index.html"), "/oppija")
    context.mount(new SingleFileServlet("web/static/index.html"), "/uusioppija")
    context.mount(new OppilaitosServlet(profile.oppilaitosRepository), "/api/oppilaitos")
    context.mount(new TutkintoServlet(profile.tutkintoRepository), "/api/tutkinto")
    context.mount(new FixtureServlet(profile), "/fixtures")

  }

  override def destroy(context: ServletContext) = {
  }
}