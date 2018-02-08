package controllers
import javax.inject.Inject

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.i18n.I18nSupport
import sun.security.util.Password
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json._
import scala.concurrent._

case class User(firstname : String, lastname: String, email:String,mobile:String,username:String,password: String)
case class UserData(firstname : String, lastname: String, email:String,mobile:String,username:String,password: String )

class HomeController @Inject()(val controllerComponents: ControllerComponents,dbConfigProvider: DatabaseConfigProvider)
  extends BaseController
    with I18nSupport {
  //  mapping of userdetails class

  val userDataForm: Form[UserData] = Form(
    mapping(
      "firstname" -> nonEmptyText,
      "lastname" -> nonEmptyText,
      "email" -> nonEmptyText,
      "mobile" -> nonEmptyText,
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(UserData.apply)(UserData.unapply)
  )


  //  render user form which take input as firstname and lastname
  def addUser = Action { implicit request =>
    Ok(views.html.index(userDataForm))

  }

  //  add values of User object to user table and shows message
  def addUserToDB = Action.async { implicit request =>
    val data = userDataForm.bindFromRequest.get //get user object which has input values
    val newUser = User( data.firstname,data.lastname,data.email,data.mobile,  data.username, data.password)
    add(newUser) map { users =>
      Ok(views.html.user(users))
    }
  }

  class UserTableDef(tag: Tag) extends Table[User](tag, "user") {

    def firstname = column[String]("firstname")
    def lastname = column[String]("lastname")
    def email = column[String]("email")
    def mobile = column[String]("mobile")
    def username = column[String]("username")
    def password = column[String]("password")

    override def * =
      (firstname,lastname,email,mobile,username,password) <>(User.tupled, User.unapply)
  }

    val dbConfig = dbConfigProvider.get[JdbcProfile]

    val users = TableQuery[UserTableDef]

    def add(user: User): Future[Seq[User]]= {
      dbConfig.db.run(users += user)
      dbConfig.db.run(users.result)
    }


}