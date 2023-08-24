package org.librarysimplified.http.tests

import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.slf4j.LoggerFactory
import java.io.File
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Security
import java.security.cert.X509Certificate
import java.util.Calendar
import java.util.Date
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

class LSHTTPTestTLS(
  val clientKeyPair: KeyPair,
  val clientCert: X509Certificate,
  val clientContext: SSLContext,
  val serverKeyPair: KeyPair,
  val serverCert: X509Certificate,
  val serverContext: SSLContext,
) {

  companion object {

    val provider =
      BouncyCastleProvider()
    val tlsProvider =
      BouncyCastleJsseProvider()

    init {
      Security.addProvider(this.provider)
      Security.addProvider(this.tlsProvider)
    }

    val logger =
      LoggerFactory.getLogger(LSHTTPTestTLS::class.java)

    fun createRSAKeyPair(
      name: String,
    ): KeyPair {
      this.logger.debug("generating $name keypair")
      val generator = KeyPairGenerator.getInstance("RSA")
      generator.initialize(1024)
      return generator.generateKeyPair()
    }

    fun create(): LSHTTPTestTLS {
      val clientKeyPair =
        this.createRSAKeyPair("client")
      val serverKeyPair =
        this.createRSAKeyPair("server")
      val clientCert =
        this.createCertificate("client", clientKeyPair, "C=XX, ST=Nowhere, L=ClientLand, O=LibrarySimplified, CN=localhost")
      val serverCert =
        this.createCertificate("server", serverKeyPair, "C=XX, ST=Nowhere, L=ServerLand, O=LibrarySimplified, CN=localhost")
      val clientContext =
        this.createSSLContext(
          name = "client",
          ownKeyPair = clientKeyPair,
          ownCert = clientCert,
          peerCert = serverCert,
        )
      val serverContext =
        this.createSSLContext(
          name = "server",
          ownKeyPair = serverKeyPair,
          ownCert = serverCert,
          peerCert = clientCert,
        )

      return LSHTTPTestTLS(
        clientKeyPair,
        clientCert,
        clientContext,
        serverKeyPair,
        serverCert,
        serverContext,
      )
    }

    private fun createSSLContext(
      name: String,
      ownKeyPair: KeyPair,
      ownCert: X509Certificate,
      peerCert: X509Certificate,
    ): SSLContext {
      this.logger.debug("creating $name SSL context")

      File("/tmp/$name.key").bufferedWriter().use {
        val pemWriter = JcaPEMWriter(it)
        pemWriter.writeObject(ownKeyPair.private)
        pemWriter.flush()
        pemWriter.close()
      }
      File("/tmp/$name.cert").bufferedWriter().use {
        val pemWriter = JcaPEMWriter(it)
        pemWriter.writeObject(ownCert)
        pemWriter.flush()
        pemWriter.close()
      }

      val keyStore = KeyStore.getInstance("PKCS12")
      keyStore.load(null, null)
      keyStore.setCertificateEntry("$name-cert", ownCert)
      keyStore.setKeyEntry("$name-key", ownKeyPair.private, "12345678".toCharArray(), arrayOf(ownCert))

      val trustStore = KeyStore.getInstance("PKCS12")
      trustStore.load(null, null)
      trustStore.setCertificateEntry("localhost", peerCert)

      val keyManager =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
      keyManager.init(keyStore, "12345678".toCharArray())

      val trustManagerFactory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
      trustManagerFactory.init(trustStore)

      val sslContext = SSLContext.getInstance("TLSv1.2", tlsProvider)
      sslContext.init(keyManager.keyManagers, trustManagerFactory.trustManagers, null)
      return sslContext
    }

    fun createCertificate(
      name: String,
      keyPair: KeyPair,
      subjectDN: String,
    ): X509Certificate {
      this.logger.debug("creating $name X509 certificate")

      val now = System.currentTimeMillis()
      val startDate = Date(now)
      val dnName = X500Name(subjectDN)
      val certSerialNumber = BigInteger(now.toString())
      val calendar = Calendar.getInstance()
      calendar.time = startDate
      calendar.add(Calendar.YEAR, 1)
      val endDate = calendar.time

      val contentSigner =
        JcaContentSignerBuilder("SHA256WithRSA")
          .build(keyPair.private)

      val certBuilder =
        JcaX509v3CertificateBuilder(
          dnName,
          certSerialNumber,
          startDate,
          endDate,
          dnName,
          keyPair.public,
        )

      return JcaX509CertificateConverter()
        .setProvider(this.provider)
        .getCertificate(certBuilder.build(contentSigner))
    }
  }
}
