package ch.wsl.box.rest.logic

import ch.wsl.box.model.shared.NewsEntry
import ch.wsl.box.rest.boxentities.News
import ch.wsl.box.rest.utils.{Auth, DateTimeFormatters, UserProfile}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

object NewsLoader {

  def get(lang:String)(implicit userProfile: UserProfile, ec:ExecutionContext): Future[Seq[NewsEntry]] = {
    val q = for{
      news <- News.News
      news_i18n <- News.News_i18n if news_i18n.lang === lang && news.news_id === news_i18n.news_id
    } yield (news.datetime,news_i18n.title,news_i18n.text,news.author)

    Auth.adminUserProfile.boxDb.run{
      q.result
    }.map{ _.map{ x =>
      NewsEntry(DateTimeFormatters.timestamp.format(x._1),x._2,x._3,x._4)
    }}
  }


}
