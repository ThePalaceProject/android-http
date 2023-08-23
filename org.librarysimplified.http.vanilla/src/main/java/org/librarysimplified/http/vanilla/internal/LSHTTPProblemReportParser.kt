package org.librarysimplified.http.vanilla.internal

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.librarysimplified.http.api.LSHTTPProblemReport
import org.librarysimplified.http.api.LSHTTPProblemReportParserType
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream

class LSHTTPProblemReportParser(
  private val uri: String,
  private val stream: InputStream,
) : LSHTTPProblemReportParserType {

  private val logger =
    LoggerFactory.getLogger(LSHTTPProblemReportParser::class.java)

  override fun execute(): LSHTTPProblemReport {
    try {
      val objectMapper = ObjectMapper()
      val node = objectMapper.readTree(this.stream)
      if (node is ObjectNode) {
        return this.fromObjectNode(node)
      }
      throw JsonException("Expected an object, got a ${node.nodeType}")
    } catch (e: Exception) {
      this.logger.error("[{}]: failed to parse problem report: ", this.uri, e)
      throw IOException(e)
    }
  }

  private class JsonException(
    message: String,
  ) : JsonProcessingException(message)

  private fun fromObjectNode(
    objectNode: ObjectNode,
  ): LSHTTPProblemReport {
    val statusNode = objectNode["status"]
    val typeNode = objectNode["type"]
    val titleNode = objectNode["title"]
    val detailNode = objectNode["detail"]

    val status =
      if (statusNode != null && statusNode.canConvertToInt()) {
        statusNode.asInt()
      } else {
        null
      }

    val type =
      if (typeNode != null && typeNode.isTextual) {
        typeNode.textValue()
      } else {
        null
      }

    val title =
      if (titleNode != null && titleNode.isTextual) {
        titleNode.textValue()
      } else {
        null
      }

    val detail =
      if (detailNode != null && detailNode.isTextual) {
        detailNode.textValue()
      } else {
        null
      }

    return LSHTTPProblemReport(
      status = status,
      title = title,
      detail = detail,
      type = type,
    )
  }

  override fun close() {
    this.stream.close()
  }
}
