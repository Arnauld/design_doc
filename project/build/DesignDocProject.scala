
import java.io.File
import java.util.jar.Attributes
import java.util.jar.Attributes.Name._
import sbt._
import sbt.CompileOrder._

class DesignDocProject(info: ProjectInfo) extends DefaultProject(info) {

  override def compileOptions = super.compileOptions ++
    Seq("-deprecation",
        "-Xmigration",
        "-Xcheckinit",
        //"-Xstrict-warnings",
        "-Xwarninit",
        "-encoding", "utf8")
        .map(x => CompileOption(x))
  override def javaCompileOptions = JavaCompileOption("-Xlint:unchecked") :: super.javaCompileOptions.toList

  lazy val SCALATEST_VERSION  = "1.2"
  
  // Dependencies
  val commons_codec      = "commons-codec"          % "commons-codec"       % "1.4"   % "compile"
  val commons_io         = "commons-io"             % "commons-io"          % "1.4"   % "compile"
  val commons_httpclient = "commons-httpclient"     % "commons-httpclient"  % "3.1"   % "compile"
  val junit              = "junit"                  % "junit"               % "4.8.1" % "test"
  val scalatest          = "org.scalatest"          % "scalatest"           % SCALATEST_VERSION % "test"
  val rhino              = "rhino"                  % "js"                  % 1.7R2   % "compile"
}