package views.html
package appeal

import controllers.routes
import play.api.data.Form

import lila.api.Context
import lila.app.templating.Environment._
import lila.app.ui.ScalatagsTemplate._
import lila.report.Report.Inquiry
import lila.user.User

object tree {

  import trans.contact.doNotMessageModerators
  import views.html.base.navTree._

  private def cleanMenu(implicit ctx: Context): Branch =
    Branch(
      "root",
      "Your account is not marked or restricted. You're all good!",
      List(
        Leaf(
          "clean-other-account",
          "I want to appeal for another account",
          frag(
            p(
              "Sorry we don't take appeals from other accounts. The appeal should come from nowhere else, but the concerned account."
            )
          )
        ),
        Leaf(
          "clean-warning",
          "I want to discuss a warning I received",
          frag(
            p(
              "Please note that warnings are only warnings, and that your account has not been restricted currently.",
              br,
              "If you still want to file an appeal, use the following form:"
            ),
            newAppeal()
          )
        ),
        Leaf(
          "clean-other-issue",
          "I have another issue to discuss",
          p(
            "This channel of communication is for appealing moderation related issues.",
            br,
            "Please use ",
            a(href := routes.Main.contact)("the contact page"),
            " or ",
            a(href := "https://discordapp.com/invite/pvHanhg")("our discord server"),
            " to contact us about other issues.",
            br,
            "You can also ",
            a(href := routes.Page.loneBookmark("appeal"))("find here more information about appeals.")
          )
        )
      )
    )

  private def engineMenu(implicit ctx: Context): Branch = {
    val accept =
      "I accept that I used outside assistance in my games."
    val deny =
      "I deny having used outside assistance in my games."
    Branch(
      "root",
      "Your account is marked for illegal assistance in games.",
      List(
        Leaf(
          "engine-accept",
          accept,
          frag(
            sendUsAnAppeal,
            newAppeal(s"$accept I am sorry and I would like another chance.")
          )
        ),
        Leaf(
          "engine-deny",
          deny,
          frag(
            sendUsAnAppeal,
            newAppeal(deny)
          )
        )
      )
    )
  }

  private def boostMenu(implicit ctx: Context): Branch = {
    val accept = "I accept that I manipulated my rating."
    val acceptFull =
      "I accept that I deliberately manipulated my rating by losing games on purpose, or by playing another account that was deliberately losing games. I am sorry and I would like another chance."
    val deny =
      "I deny having manipulated my rating."
    val denyFull =
      "I deny having manipulated my rating. I have never lost rated games on purpose, or played several games with someone who does."
    Branch(
      "root",
      "Your account is marked for rating manipulation.",
      List(
        Leaf(
          "boost-accept",
          accept,
          frag(
            sendUsAnAppeal,
            newAppeal(acceptFull)
          )
        ),
        Leaf(
          "boost-deny",
          deny,
          frag(
            sendUsAnAppeal,
            newAppeal(deny)
          )
        )
      )
    )
  }

  private def muteMenu(implicit ctx: Context): Branch = {
    val accept = "I accept that I have not followed the communication guidelines"
    val acceptFull =
      "I accept that I have not followed the communication guidelines. I will behave better in future, please give me another chance."
    val deny =
      "I have followed the communication guidelines"
    val denyFull =
      "I deny having manipulated my rating. I have never lost rated games on purpose, or played several games with someone who does."
    Branch(
      "root",
      "Your account is muted.",
      List(
        Leaf(
          "mute-accept",
          accept,
          frag(
            p(
              "I accept that I have not followed the ",
              a(href := routes.Page.loneBookmark("chat-etiquette"))("communication guidelines"),
              ". I will behave better in future, please give me another chance."
            ),
            sendUsAnAppeal,
            newAppeal(acceptFull)
          )
        ),
        Leaf(
          "mute-deny",
          deny,
          frag(
            sendUsAnAppeal,
            newAppeal(deny)
          )
        )
      )
    )
  }

  private def playbanMenu(implicit ctx: Context): Branch = {
    Branch(
      "root",
      "You have a play timeout.",
      List(
        Leaf(
          "playban-abort",
          "For having aborted too many games.",
          frag(
            p(
              "We understand your frustration, but temporary play bans for aborting too many games are necessary. It's very annoying for your opponent when the game gets aborted and we have to discourage it."
            ),
            p("A few things we can suggest are:"),
            ul(
              li("Don't send a challenge if you don't want to play and then abort the game."),
              li(
                "If you don't want to face lower or higher rated opponents, set a rating range on your seek."
              ),
              li(
                "Don't abort games if you want to have a particular color, you have to play with both colors."
              )
            )
          )
        ),
        Leaf(
          "playban-timeout",
          "For letting my game clock time run out.",
          p(
            "We understand your frustration, but temporary play bans for stalling in games are necessary, it can be very frustrating for opponents to waste time in lost positions before resigning."
          )
        ),
        Leaf(
          "playban-disconnect",
          "For frequently disconnecting from games.",
          frag(
            p(
              "We understand your frustration, but temporary play bans for losing connection are necessary, even if you don't disconnect on purpose. It's very annoying to suddenly lose your opponent during a game and we have to discourage it."
            ),
            p(
              "The only thing we can suggest to you is that you try to get a better connection or play longer time-control games that are more forgiving of disconnections."
            )
          )
        )
      )
    )
  }

  def apply(me: User, playban: Boolean)(implicit ctx: Context) =
    bits.layout("Appeal a moderation decision") {
      main(cls := "page page-small box box-pad appeal")(
        h1("Appeal"),
        div(cls := "nav-tree")(
          renderNode(
            {
              if (playban || ctx.req.queryString.contains("playban")) playbanMenu
              else if (me.marks.engine || ctx.req.queryString.contains("engine")) engineMenu
              else if (me.marks.boost || ctx.req.queryString.contains("boost")) boostMenu
              else if (me.marks.troll || ctx.req.queryString.contains("shadowban")) muteMenu
              else cleanMenu
            },
            none
          )
        ),
        p(cls := "appeal__moderators text", dataIcon := "")(doNotMessageModerators())
      )
    }

  private val sendUsAnAppeal = frag(
    p("Send us an appeal, and a moderator will review it as soon as possible."),
    p("Add any relevant information that could help us process your appeal."),
    p("Please be honest, concise, and on point.")
  )

  private def newAppeal(preset: String = "")(implicit ctx: Context) =
    discussion.renderForm(
      lila.appeal.Appeal.form.fill(preset),
      action = routes.Appeal.post.url,
      isNew = true,
      presets = none
    )

  private def renderHelp =
    div(cls := "appeal__help")(
      p(
        "If your account has been restricted for violation of ",
        a(href := routes.Page.tos)("the Lichess rules"),
        " you may file an appeal here."
      ),
      p(
        "You can read more about the appeal process ",
        a(href := routes.Page.loneBookmark("appeal"))("here.")
      )
    )
}
