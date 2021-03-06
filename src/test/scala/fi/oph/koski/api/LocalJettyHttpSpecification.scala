package fi.oph.koski.api

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.jettylauncher.{JettyLauncher, SharedJetty}
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.{AuditLogTester, Logging}
import fi.oph.koski.util.PortChecker

trait LocalJettyHttpSpecification extends HttpSpecification {
  def refreshElasticSearchIndex = {
    KoskiApplicationForTests.elasticSearch.refreshIndex
  }

  override def baseUrl = {
    LocalJettyHttpSpecification.setup(this)
    LocalJettyHttpSpecification.baseUrl
  }

  def defaultUser = MockUsers.kalle
}

object LocalJettyHttpSpecification extends Logging {
  private lazy val myJetty = externalJettyPort match {
    case None => SharedJetty
    case Some(port) =>
      logger.info(s"Using external jetty on port $port")
      new JettyLauncher(port)
  }

  var running = false
  def setup(spec: HttpSpecification) = synchronized {
    if (!running) {
      running = true
      if (externalJettyPort.isEmpty) SharedJetty.start
      AuditLogTester.setup
      spec.resetFixtures
    }
  }

  def baseUrl = myJetty.baseUrl

  def externalJettyPort = Option(System.getProperty("test.externalJettyPort")).map(_.toInt)
  def portForSharedJetty = externalJettyPort.getOrElse(PortChecker.findFreeLocalPort)
}