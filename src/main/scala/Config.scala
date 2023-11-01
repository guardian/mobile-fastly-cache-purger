import com.gu.conf.{ConfigurationLoader, SSMConfigurationLocation}
import com.gu.{AppIdentity, AwsIdentity}
import com.typesafe.config.{Config => Conf}

case class Config(fastlyServiceId: String, fastlyApiKey: String)

object Config {

  private def fetchConfiguration(): Conf = {
    val identity = AppIdentity.whoAmI(defaultAppName = "mobile-fastly-cache-purger")
    ConfigurationLoader.load(identity) {
      case AwsIdentity(_, _, stage, _) => SSMConfigurationLocation(s"/cache-purger/$stage")
    }
  }

  def load(): Config = {
    println("Loading facia-purger config...")

    val config = fetchConfiguration()
    Config(config.getString("FastlyServiceId"), config.getString("FastlyAPIKey"))
  }
}

case class FaciaConfig(role: String, environment: String, s3Bucket: String)

object FaciaConfig {
  private def fetchConfiguration(): Conf = {
    val identity = AppIdentity.whoAmI(defaultAppName = "mobile-fastly-cache-purger")
    ConfigurationLoader.load(identity) {
      case AwsIdentity(_, _, stage, _) => SSMConfigurationLocation(s"/cache-purger/$stage/")
    }
  }
    def load(): FaciaConfig = {

      val config = fetchConfiguration()
      FaciaConfig(config.getString("apis.facia.role"), config.getString("CODE"), config.getString("facia-tool-store"))
      }
}