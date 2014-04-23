package io.straight.ete.web.routes

import spray.routing.HttpService
import spray.routing.directives.{SecurityDirectives, CachingDirectives, RespondWithDirectives, DebuggingDirectives}
import spray.http.HttpHeaders._
import spray.http.{HttpHeader, CacheDirectives, HttpHeaders}


trait StaticDocRouter extends HttpService
with DebuggingDirectives
with RespondWithDirectives {

  val staticFilesRoute =  {

    path("favicon.ico") {
       getFromFile("src/main/webapp/favicon.ico")
    }
    pathPrefix("") {
      getFromDirectory("src/main/webapp")
    } ~ // and if it's neither of these, then straight to the index.html
        // just by the nature of the "mapping" we are not allowing "/index.html"
    (path("") | pathPrefix("#")) { respondWithHeaders(addNoCacheHeaders) {
        getFromFile("src/main/webapp/index.html")
      }
    }

  }
  /**
   * Construct a list of No caching HTTP Headers
   *
   * TODO return a good list of cache controlling (images .. cached for a day js.. no cache etc)
   *
   * @return
   */
  private def addNoCacheHeaders : List[HttpHeader] = {

    val pragma = RawHeader("Pragma", "no-cache")
    val expires = RawHeader("Expires", "Fri, 01 Jan 1990 00:00:00 GMT")
    val cache = HttpHeaders.`Cache-Control`(List(CacheDirectives.`must-revalidate`, CacheDirectives.`no-cache`))

    return cache :: expires :: pragma :: Nil
  }

}
