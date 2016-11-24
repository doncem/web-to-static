package lt.donatasmart.webToStatic

object MainApp extends App {

  def help(): Unit = println(
    """
      |Usage:
      |
      |  - `run https://www.google.com` - saves to `static/_tmp`
      |  - `run https://www.google.com google` - saves to `static/google`
    """.stripMargin)

  args.headOption match {
    case Some("help") => help()
    case Some(arg) =>
      new Extractor(arg, args.tail.headOption.getOrElse("_tmp"))
//      sys.exit()
    case _ => sys.error("Missing at least one parameter")
  }
}
