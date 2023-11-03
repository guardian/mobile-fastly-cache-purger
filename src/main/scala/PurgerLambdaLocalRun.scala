import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage

import java.time.Instant
import scala.jdk.CollectionConverters._

object PurgerLambdaLocalRun extends App {

  val sqsEvent: SQSEvent = {
    val event = new SQSEvent()
    val sqsMessage = new SQSMessage()
//    sqsMessage.setBody(
//      """{"Type":"Notification","MessageId":"8dc44385-e650-521b-8687-1e29710a57a6","TopicArn":"arn:aws:sns:eu-west-1:163592447864:facia-CODE-FrontsUpdateSNSTopic-RepwK3g95V3w","Message":{"path":"app/front-mss","pressType":"draft","creationTime":1697813786763,"forceConfigUpdate":false},"Timestamp":"2023-10-20T14:56:26.769Z","SignatureVersion":1,"Signature":"RmOs7dhwGWVxyVlbn90hE7zrYk6BlbTUObtVdo5oX1KOKMB2hFVs2MFxbPvMBhX5BXn6t3J/6S8m+yKaIoYoeWx9b37RY+T6JHlTn0hQ7fOT9aThflkOe6Z4KsqVz52SKU137gEZhbfg4wq9F0zl2/zT6zETFIP8O/lFdVxzYWaMka5vS5TRlDKwDRuP9dP43Zhabc5IaKF0wsnN6/Ig1DSB+Z+uXrsEzyHEabmdXk2wEJVJqH8sY/LJY8xWM56tqOfx3UAlsyriUDi+Mv73XnXFj1j9Y7KhteJj+lsCVYj1Kg11aZ/zS6CoUnhWPEEb+JQ3Tx2UB+PoQFbhySbMnQ==","SigningCertURL":"https://sns.eu-west-1.amazonaws.com/SimpleNotificationService-01d088a6f77103d0fe307c0069e40ed6.pem","UnsubscribeURL":"https://sns.eu-west-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:eu-west-1:163592447864:facia-CODE-FrontsUpdateSNSTopic-RepwK3g95V3w:f5e08497-05a4-4543-833f-4e0914c5e985"}"""
//    )

//    sqsMessage.setBody(
//      "\n      {\n        \"Type\" : \"Notification\", \"MessageId\" : \"46478e36-e7c4-526d-ba0a-d389e69e3156\", \"TopicArn\" : \"arn:aws:sns:eu-west-1:163592447864:facia-CODE-FrontsUpdateSNSTopic-RepwK3g95V3w\",\"Message\" : \"{\\\"path\\\":\\\"app/front-mss\\\",\\\"pressType\\\":\\\"draft\\\",\\\"creationTime\\\":1699020570534,\\\"forceConfigUpdate\\\":false}\", \"Timestamp\" : \"2023-11-03T14:09:30.555Z\", \"SignatureVersion\" : \"1\", \"Signature\" : \"LKXAV+I4Kgw3gvbH9w5SNuQg4Cck7yT2dCDgfJec/FhQw2Cdx15UmcXm1SZG710FS8W+mSwMjzN1iGId7PhqrsL3VsgLTGp1Q0aztSSiB4C2iwl2RRB0s3qagH7wnEBve8XftSiUOeHaHgsotte0MvAJSXhfYnmyuKW462UNmgZtC8mYyEHuKO0rGyjLGlKXK+GR4zYXrLk/lw3FweiRFmXoy9LMUoix1cXLc2QOrWL9HLD/RlqgIAUhPH5xK1E4oktWi3k7+vHqtgZ6r8pYPzDn1Nn8D6H2lwoGsZ/F7puVhdz1UT78aG0BINtSnzSp7HTcULHn0GER4G/rZ/Xybw==\", \"SigningCertURL\" : \"https://sns.eu-west-1.amazonaws.com/SimpleNotificationService-01d088a6f77103d0fe307c0069e40ed6.pem\",\n        \"UnsubscribeURL\" : \"https://sns.eu-west-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:eu-west-1:163592447864:facia-CODE-FrontsUpdateSNSTopic-RepwK3g95V3w:f5e08497-05a4-4543-833f-4e0914c5e985\"\n\n      }\n    "
//    )

//    sqsMessage.setBody("{\"path\":\"app/front-mss\",\"pressType\":\"draft\",\"creationTime\":1697813786763,\"forceConfigUpdate\":false}")

    sqsMessage.setAttributes((Map("SentTimestamp" -> s"${Instant.now.toEpochMilli}").asJava))
    event.setRecords(List(sqsMessage).asJava)
    event
  }

  PurgerLambda.handleRequest(sqsEvent, null)
}