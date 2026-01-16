import com.gu.conf.{ConfigurationLoader, SSMConfigurationLocation}
import com.gu.{AppIdentity, AwsIdentity, DevIdentity}
import com.typesafe.config.{Config => Conf}
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProviderChain, DefaultCredentialsProvider, ProfileCredentialsProvider}

case class Config(fastlyServiceId: String, fastlyApiKey: String, faciaRole: String, faciaEnvironment: String)

object Config {

  private def fetchConfiguration(): Conf = {

    lazy val credentials = AwsCredentialsProviderChain.of(
      ProfileCredentialsProvider.builder.profileName("mobile").build,
      DefaultCredentialsProvider.create
    )


    val defaultAppName = "mobile-fastly-cache-purger"
    val identity = Option(System.getenv("MOBILE_LOCAL_DEV")) match {
      case Some(_) => DevIdentity(defaultAppName)
      case None => AppIdentity
        .whoAmI(defaultAppName, credentials)
        .getOrElse(DevIdentity(defaultAppName))
    }

    ConfigurationLoader.load(identity, credentials) {
      case AwsIdentity(_, _, stage, region) => SSMConfigurationLocation(s"/cache-purger/$stage", region)
    }
  }

  def load(): Config = {
    println("Loading fastly-purger config...")

    val config = fetchConfiguration()
    Config(config.getString("FastlyServiceId"), config.getString("FastlyAPIKey"), config.getString("apis.facia.role"), config.getString("apis.facia.environment"))
  }
}