import java.io._
import java.security.MessageDigest
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.httpclient.util.URIUtil
import org.apache.commons.io.IOUtils
import org.apache.commons.httpclient.{HttpStatus, HttpMethod, HttpClient}
import java.net.URLEncoder
import org.apache.commons.httpclient.methods.GetMethod
import org.junit.Test
import org.scalatest.junit.JUnitSuite

class GenerateDiagramSpec extends JUnitSuite {
  import GenerateDiagramSpec._
  
  @Test
  def drawAll:Unit = {
    diagramDefinitionDir.listFiles.foreach { f:File =>
      val fileName = f.getName
      if(fileName.endsWith(EXTENSION)) {
        val name = fileName.substring(0, fileName.length-EXTENSION.length)
        call(fileToString(f), name)
      }
    }
  }
}

object GenerateDiagramSpec {

  val EXTENSION = ".diag"
  val DIAG_DIR = "doc/yuml/"
  val OUT_DIR  = "doc/diagrams/"
  val MD5_DIR = ".yuml/"

  def cleanup (diagram:String):String = {
    var clean = diagram.replaceAll("[\r\n]+", ", ")
    if(clean.endsWith(", "))
      clean = clean.substring(0, clean.length-2)
    clean
  }

  def call(diagram: String, name: String): Unit = {
    val clean = cleanup(diagram)

    val md5 = DigestUtils.md5Hex(diagram.getBytes("utf-8"))

    val fileOut = new File(OUT_DIR, name+".jpg")
    val fileMd5 = new File(MD5_DIR, name)
          
    if(fileOut.exists) {
      if(fileMd5.exists) {
        val md5Stream = new FileInputStream(fileMd5)
        try {
          if(IOUtils.toString(md5Stream)==md5) {
            println("Diagram already generated with same hash, skipping: <" + name + ">")
            return;
          }
        }finally{
          IOUtils.closeQuietly(md5Stream)
        }
      }
    }

    println("Generating <" + name + ">")

    // create md5 cache dir
    new File(MD5_DIR).mkdirs

    val encoded = URIUtil.encodeAll(clean, "UTF-8")
    val client = new HttpClient()
    val method = new GetMethod("http://yuml.me/diagram/scruffy/class/" + encoded)

    val streamOut = new FileOutputStream(fileOut)
    val streamMd5 = new FileOutputStream(fileMd5)
    try {
      client.executeMethod(method);
      if (method.getStatusCode() == HttpStatus.SC_OK) {
        IOUtils.copy(method.getResponseBodyAsStream(), streamOut)

        // update md5 cache
        streamMd5.write(md5.getBytes("UTF-8"))
      }
    } catch {
      case e: Exception => e.printStackTrace()
    }
    finally {
      IOUtils.closeQuietly(streamOut)
      IOUtils.closeQuietly(streamMd5)
      method.releaseConnection()
    }
  }

  def diagramDefinitionDir:File = new File(DIAG_DIR);

  def get(resource: String): String = {
    fileToString(new File(diagramDefinitionDir,resource))
  }

  def fileToString(file: File): String = {
    var resourceAsStream: InputStream = null
    try {
      resourceAsStream = new FileInputStream(file)
      IOUtils.toString(resourceAsStream)
    }
    finally {
      IOUtils.closeQuietly(resourceAsStream)
    }
  }
}