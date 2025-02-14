object Platform {
  sealed abstract class OS(val string: String) extends Product with Serializable
  object OS {
    case object Windows extends OS("windows")
    case object MacOS extends OS("osx")
    case object Linux extends OS("linux")
    case object Unknown extends OS("unknown")

    val all: List[OS] = List(Windows, MacOS, Linux, Unknown)
    def detect(osNameProp: String): OS = normalise(osNameProp) match {
      case p if p.startsWith("linux")                         => OS.Linux
      case p if p.startsWith("windows")                       => OS.Windows
      case p if p.startsWith("osx") || p.startsWith("macosx") => OS.MacOS
      case _                                                  => OS.Unknown
    }
  }
  sealed abstract class Arch extends Product with Serializable {}
  object Arch {
    case object Intel extends Arch {}
    case object Arm extends Arch {}

    val all: List[Arch] = List(Intel, Arm)
    def detect(osArchProp: String): Arch = normalise(osArchProp) match {
      case "amd64" | "x64" | "x8664" | "x86" => Intel
      case "aarch64" | "arm64"               => Arm
    }
  }

  sealed abstract class Bits extends Product with Serializable
  object Bits {
    case object x32 extends Bits
    case object x64 extends Bits

    def detect(sunArchProp: String): Bits =
      sunArchProp match {
        case "64" => x64
        case "32" => x32
      }
  }

  case class Target(os: OS, arch: Arch, bits: Bits)

  lazy val os: OS = OS.detect(sys.props.getOrElse("os.name", ""))
  lazy val arch: Arch = Arch.detect(sys.props.getOrElse("os.arch", ""))
  lazy val bits: Bits = Bits.detect(sys.props.getOrElse("sun.arch.data.model", ""))
  lazy val target: Target = Target(os, arch, bits)

  private def normalise(s: String) =
    s.toLowerCase(java.util.Locale.US).replaceAll("[^a-z0-9]+", "")

}