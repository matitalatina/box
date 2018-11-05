import org.mitre.dsmiley.httpproxy.ProxyServlet


class BoxServlet extends ProxyServlet {

  println("Starting box")
  ch.wsl.box.rest.Box.start()


}