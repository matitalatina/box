package ch.wsl.box.services.mail
import scala.concurrent.{ExecutionContext, Future}

class MailServiceDummy extends MailService {

  override def send(mail: Mail)(implicit ec: ExecutionContext): Future[Boolean] = Future.successful{
    println(mail)
    true
  }
}
