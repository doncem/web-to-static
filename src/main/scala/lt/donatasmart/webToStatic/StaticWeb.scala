package lt.donatasmart.webToStatic

import java.io.{BufferedOutputStream, FileOutputStream, IOException, PrintWriter}
import java.net.URL
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

class StaticWeb(root: String) extends LazyLogging {

  case class Resources(headLinks: Iterator[String], links: Iterator[String], scripts: Iterator[String], images: Iterator[String])
  object Resources {
    private def getLinks(contents: String) = {
      val r = """<a href="(.*?)"""".r
      val headR = """<link[^>]*href=["'](.*?)["']""".r
      r.findAllMatchIn(contents).map(_.group(1)).filterNot(m =>
        m.startsWith("http") || m.startsWith("//") || m.startsWith("#") || m.startsWith("mailto") || m.startsWith("javascript")
      ) ++
      headR.findAllMatchIn(contents).filterNot(f =>
        f.matched.contains("""rel="apple-touch-icon-precomposed"""") ||
          f.matched.contains("""rel="icon"""") ||
          f.matched.contains("""rel='stylesheet'""")
      ).map(_.group(1)).filterNot(m =>
        m.startsWith("http") || m.startsWith("//")
      )
    }

    private def getHeadLinks(contents: String) = {
      val r = """<link[^>]*href=["'](.*?)["']""".r
      r.findAllMatchIn(contents).filter(f =>
        f.matched.contains("""rel="apple-touch-icon-precomposed"""") ||
          f.matched.contains("""rel="icon"""") ||
          f.matched.contains("""rel='stylesheet'""")
      ).map(_.group(1)).filterNot(m =>
        m.startsWith("http") || m.startsWith("//")
      )
    }

    private def getScripts(contents: String) = {
      val r = """<script[^>]*src=["'](.*?)["']""".r
      r.findAllMatchIn(contents).map(_.group(1)).filterNot(m =>
        m.startsWith("http") || m.startsWith("//")
      )
    }

    private def getImages(contents: String) = {
      val r = """<img[^>]*src=['"](.*?)["']""".r
      r.findAllMatchIn(contents).map(_.group(1)).filterNot(m =>
        m.startsWith("http") || m.startsWith("//")
      )
    }

    def apply(): Resources = Resources(Iterator.empty, Iterator.empty, Iterator.empty, Iterator.empty)
    def apply(contents: String): Resources = Resources(getHeadLinks(contents), getLinks(contents), getScripts(contents), getImages(contents))
  }

  def getPath(path: String = ""): Path = FileSystems.getDefault.getPath(s"$root/$path")

  def cleanUp(): Unit = {
    val path = getPath()

    if (Files.exists(path) && Files.isDirectory(path)) {
      logger.debug(s"Clearing previous extraction: ${path.toFile.getAbsolutePath}")
      Files.walkFileTree(path, new FileVisitor[Path] {
        def visitFileFailed(file: Path, exc: IOException) = FileVisitResult.CONTINUE

        def visitFile(file: Path, attrs: BasicFileAttributes) = {
          Files.delete(file)
          FileVisitResult.CONTINUE
        }

        def preVisitDirectory(dir: Path, attrs: BasicFileAttributes) = FileVisitResult.CONTINUE

        def postVisitDirectory(dir: Path, exc: IOException) = {
          Files.delete(dir)
          FileVisitResult.CONTINUE
        }
      })
    }
  }

  def saveStatic(url: String, domain: String): Resources = {
    logger.debug("URL: {}", url)
    val file = getPath {
      val f = url.replace(domain + "/", "")
      Option(f.lastIndexOf("?")).filter(_ > 0).map(index => f.substring(0, index)).getOrElse(f)
    }

    if (!Files.exists(file)) {
      logger.debug(s"Saving static file: ${file.toFile.getAbsolutePath}")
      Try(Files.createDirectories(file.getParent)) match {
        case Success(_) =>
          val in = new URL(url).openConnection().getInputStream
          val out = new BufferedOutputStream(new FileOutputStream(Files.createFile(file).toFile))
          out.write(Stream.continually(in.read()).takeWhile(-1 !=).map(_.toByte).toArray)
          in.close()
          out.close()
        case Failure(e) => logger.error("Could not create directories: {}", e.getMessage)
      }
    }
    Resources()
  }

  def saveIndex(path: String, contents: String): Resources = {
    val ext = if (contents.startsWith("<?xml")) "xml" else "html"
    val file = getPath(s"$path/index.$ext".replace("//", "/"))
    if (Files.exists(file)) Resources() else {
      logger.debug(s"Saving index file: ${file.toFile.getAbsolutePath}")
      Try(Files.createDirectories(getPath(path)))
      new PrintWriter(file.toFile) { write(contents); close() }
      Resources(contents)
    }
  }
}
