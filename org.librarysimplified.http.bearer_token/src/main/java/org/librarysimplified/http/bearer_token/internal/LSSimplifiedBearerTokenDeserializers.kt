package org.librarysimplified.http.bearer_token.internal

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer
import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.type.ArrayType
import com.fasterxml.jackson.databind.type.CollectionLikeType
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.databind.type.MapLikeType
import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.databind.type.ReferenceType
import org.slf4j.LoggerFactory

/**
 * A deserializer that only allows for deserializing a fixed set of classes,
 * for reasons of security.
 */

class LSSimplifiedBearerTokenDeserializers private constructor(
  private val allowedClasses: Set<String>,
) : SimpleDeserializers() {

  private val logger =
    LoggerFactory.getLogger(LSSimplifiedBearerTokenDeserializers::class.java)

  @Throws(JsonMappingException::class)
  override fun findArrayDeserializer(
    type: ArrayType,
    config: DeserializationConfig,
    beanDesc: BeanDescription,
    elementTypeDeserializer: TypeDeserializer,
    elementDeserializer: JsonDeserializer<*>?,
  ): JsonDeserializer<*>? {
    this.checkAllowedClass(type.toCanonical())
    return super.findArrayDeserializer(
      type,
      config,
      beanDesc,
      elementTypeDeserializer,
      elementDeserializer,
    )
  }

  private fun checkAllowedClass(name: String) {
    this.logger.trace("checkWhitelist: {}", name)
    require(this.allowedClasses.contains(name)) {
      String.format("Deserializing a value of type %s is not allowed", name)
    }
  }

  @Throws(JsonMappingException::class)
  override fun findBeanDeserializer(
    type: JavaType,
    config: DeserializationConfig,
    beanDesc: BeanDescription,
  ): JsonDeserializer<*>? {
    this.checkAllowedClass(type.rawClass.canonicalName)
    return super.findBeanDeserializer(type, config, beanDesc)
  }

  @Throws(JsonMappingException::class)
  override fun findCollectionDeserializer(
    type: CollectionType,
    config: DeserializationConfig,
    beanDesc: BeanDescription,
    elementTypeDeserializer: TypeDeserializer,
    elementDeserializer: JsonDeserializer<*>?,
  ): JsonDeserializer<*>? {
    this.checkAllowedClass(type.toCanonical())
    return super.findCollectionDeserializer(
      type,
      config,
      beanDesc,
      elementTypeDeserializer,
      elementDeserializer,
    )
  }

  @Throws(JsonMappingException::class)
  override fun findCollectionLikeDeserializer(
    type: CollectionLikeType,
    config: DeserializationConfig,
    beanDesc: BeanDescription,
    elementTypeDeserializer: TypeDeserializer,
    elementDeserializer: JsonDeserializer<*>?,
  ): JsonDeserializer<*>? {
    this.checkAllowedClass(type.toCanonical())
    return super.findCollectionLikeDeserializer(
      type,
      config,
      beanDesc,
      elementTypeDeserializer,
      elementDeserializer,
    )
  }

  @Throws(JsonMappingException::class)
  override fun findEnumDeserializer(
    type: Class<*>,
    config: DeserializationConfig,
    beanDesc: BeanDescription,
  ): JsonDeserializer<*>? {
    this.checkAllowedClass(type.canonicalName)
    return super.findEnumDeserializer(type, config, beanDesc)
  }

  @Throws(JsonMappingException::class)
  override fun findTreeNodeDeserializer(
    nodeType: Class<out JsonNode?>,
    config: DeserializationConfig,
    beanDesc: BeanDescription,
  ): JsonDeserializer<*>? {
    this.checkAllowedClass(nodeType.canonicalName)
    return super.findTreeNodeDeserializer(nodeType, config, beanDesc)
  }

  @Throws(JsonMappingException::class)
  override fun findReferenceDeserializer(
    refType: ReferenceType,
    config: DeserializationConfig,
    beanDesc: BeanDescription,
    contentTypeDeserializer: TypeDeserializer,
    contentDeserializer: JsonDeserializer<*>?,
  ): JsonDeserializer<*>? {
    this.checkAllowedClass(refType.toCanonical())
    return super.findReferenceDeserializer(
      refType,
      config,
      beanDesc,
      contentTypeDeserializer,
      contentDeserializer,
    )
  }

  @Throws(JsonMappingException::class)
  override fun findMapDeserializer(
    type: MapType,
    config: DeserializationConfig,
    beanDesc: BeanDescription,
    keyDeserializer: KeyDeserializer,
    elementTypeDeserializer: TypeDeserializer,
    elementDeserializer: JsonDeserializer<*>?,
  ): JsonDeserializer<*>? {
    this.checkAllowedClass(type.toCanonical())
    return super.findMapDeserializer(
      type,
      config,
      beanDesc,
      keyDeserializer,
      elementTypeDeserializer,
      elementDeserializer,
    )
  }

  @Throws(JsonMappingException::class)
  override fun findMapLikeDeserializer(
    type: MapLikeType,
    config: DeserializationConfig,
    beanDesc: BeanDescription,
    keyDeserializer: KeyDeserializer,
    elementTypeDeserializer: TypeDeserializer,
    elementDeserializer: JsonDeserializer<*>?,
  ): JsonDeserializer<*>? {
    this.checkAllowedClass(type.toCanonical())
    return super.findMapLikeDeserializer(
      type,
      config,
      beanDesc,
      keyDeserializer,
      elementTypeDeserializer,
      elementDeserializer,
    )
  }

  companion object {

    private val logger =
      LoggerFactory.getLogger(LSSimplifiedBearerTokenDeserializers::class.java)

    private fun allowClasses(): Set<String> {
      return setOf(
        "java.lang.String",
        "java.math.BigInteger",
        "java.net.URI",
        "org.librarysimplified.http.bearer_token.LSSimplifiedBearerToken",
      )
    }

    fun create(): LSSimplifiedBearerTokenDeserializers {
      val allow = this.allowClasses()
      for (entry in allow) {
        this.logger.trace("whitelist: {}", entry)
      }
      return LSSimplifiedBearerTokenDeserializers(allow)
    }
  }
}
