package lt.donatasmart.webToStatic

import com.typesafe.scalalogging.LazyLogging

import scala.io.Source

class Extractor(domain: String, staticFolder: String) extends LazyLogging {

  logger.info(s"Web extractor started. Preparing '$staticFolder' for extraction")
  val root = s"static/$staticFolder"
  val staticWeb = new StaticWeb(root)
  staticWeb.cleanUp()

  def getPage(url: String): String = Source.fromURL(url).mkString

  def extract(url: String, `type`: String, extracted: Vector[String] = Vector("")): Unit = {
    logger.debug(s"Getting $url")
    val resources = if (`type` == "resource") {
      staticWeb.saveStatic(url, domain)
    } else {
      staticWeb.saveIndex(url.replace(domain, ""), getPage(url))
    }
    val (linksToIterate, linksToAppend) = resources.links.filterNot(extracted.contains).duplicate
    val (headLinksToIterate, headLinksToAppend) = resources.headLinks.filterNot(extracted.contains).duplicate
    val (scriptsToIterate, scriptsToAppend) = resources.scripts.filterNot(extracted.contains).duplicate
    val (imagesToIterate, imagesToAppend) = resources.images.filterNot(extracted.contains).duplicate
    val linksExtracted = extracted ++ linksToAppend ++ headLinksToAppend ++ scriptsToAppend ++ imagesToAppend

    linksToIterate.foreach(link => extract(domain + link, "index", linksExtracted))
    headLinksToIterate.foreach(link => extract(domain + link, "resource", linksExtracted))
    scriptsToIterate.foreach(link => extract(domain + link, "resource", linksExtracted))
    imagesToIterate.foreach(link => extract(domain + link, "resource", linksExtracted))
  }

  extract(domain, "index")
}
