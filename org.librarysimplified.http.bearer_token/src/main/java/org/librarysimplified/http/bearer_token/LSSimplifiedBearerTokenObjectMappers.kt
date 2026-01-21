package org.librarysimplified.http.bearer_token

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule

object LSSimplifiedBearerTokenObjectMappers {

  fun createObjectMapper(): ObjectMapper {
    val mapper = JsonMapper.builder()
      .configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build()
    val deserializers = LSSimplifiedBearerTokenDeserializers.create()
    val simpleModule = SimpleModule()
    simpleModule.setDeserializers(deserializers)
    mapper.registerModule(simpleModule)
    return mapper
  }
}
