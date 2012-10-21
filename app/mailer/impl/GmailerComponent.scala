package mailer.impl

import mailer.MailerComponent

import org.apache.commons.mail.{DefaultAuthenticator, HtmlEmail};

trait GmailerComponent extends MailerComponent{
  val mailer = new Gmailer
  class Gmailer extends Mailer{
    private val username = System.getenv("EMAIL_USER_NAME")
    private val password = System.getenv("EMAIL_PASSWORD")
    private val server   = "smtp.gmail.com"

    def sendMail(recipients:List[(String, String)], subject:String, body:String)={
      val emailFn =(email:HtmlEmail)=>{
        email.setSubject(subject)
        recipients.foreach(recipient =>{
          email.addTo(recipient._1, recipient._2)
        })
        email.setHtmlMsg(body)
      }
      withEmail(emailFn)
    }

    private def withEmail[A](fn:HtmlEmail => A)={
      val email = new HtmlEmail
      email.setHostName(server)
      email.setAuthenticator(new DefaultAuthenticator(username, password));
      email.setSmtpPort(587);
      email.getMailSession.getProperties.put("mail.smtp.host", server)
      email.getMailSession.getProperties.put("mail.smtp.port", "587")
      email.getMailSession.getProperties.put("mail.smtp.user", username)
      email.getMailSession.getProperties.put("mail.smtp.password", password)
      email.getMailSession.getProperties.put("mail.smtp.starttls.enable", "true")
      email.getMailSession.getProperties.put("mail.smtp.auth", "true")
      email.setFrom("farehound.robot@gmail.com")
      email.setTLS(true)
      fn(email)
      email.send
    }
  }
}
