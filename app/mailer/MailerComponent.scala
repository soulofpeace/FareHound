package mailer

trait MailerComponent{
  val mailer:Mailer

  trait Mailer{
    def sendMail(recipients:List[(String, String)], subject:String, body:String)
  }
}
