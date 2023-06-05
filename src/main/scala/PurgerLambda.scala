import com.amazonaws.services.lambda.runtime.Context

object PurgerLambda {
  def handleRequest(context: Context): Unit = {
    println("Hello World!")
  }
}
