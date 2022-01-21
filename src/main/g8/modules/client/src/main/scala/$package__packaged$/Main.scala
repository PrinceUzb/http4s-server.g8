package $package$
import japgolly.scalajs.react._
import japgolly.scalajs.react.callback.Callback
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.document
import org.scalajs.dom.html.{Div, Element}

import java.time.LocalDateTime
import scala.scalajs.js

object Main extends App {

  case class State(second: String, minute: String, hour: String)

  def currentTime: State = {
    val now = LocalDateTime.now()
    println(now.getSecond.toString, now.getMinute.toString, now.getHour.toString)
    State(now.getSecond.toString, now.getMinute.toString, now.getHour.toString)
  }

  class Backend(\$: BackendScope[Unit, State]) {

    def tick: Callback =
      \$.setState(currentTime)

    def start: Callback = Callback {
      js.timers.setInterval(1000)(tick.runNow())
    }

    start.runNow()

    def render(state: State): VdomTagOf[Element] =
      <.figure(
        <.div(^.className := "face top",
          <.p(state.second)
        ),
        <.div(^.className := "face front",
          <.p(state.minute)
        ),
        <.div(^.className := "face left",
          <.p(state.hour)
        )
      )
  }

  val Clock = ScalaComponent.builder[Unit]
    .initialState(currentTime)
    .renderBackend[Backend]
    .build

  Clock().renderIntoDOM(document.getElementById("app"))

}
