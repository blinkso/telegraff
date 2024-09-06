package ua.blink.telegraff.autoconfigure

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import ua.blink.telegraff.autoconfigure.property.TelegramProperties
import ua.blink.telegraff.client.TelegramPollingClient
import ua.blink.telegraff.client.TelegramWebhookClient
import ua.blink.telegraff.component.DefaultTelegramApi
import ua.blink.telegraff.component.TelegramApi
import ua.blink.telegraff.dsl.DefaultHandlersFactory
import ua.blink.telegraff.dsl.HandlersFactory
import ua.blink.telegraff.filter.*

/**
 * Configuration for Telegraff when used in a servlet web context.
 *
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(TelegramPollingClient::class, TelegramWebhookClient::class)
class TelegraffServletWebConfiguration(val telegramProperties: TelegramProperties) {

    @Bean
    fun objectMapper(): ObjectMapper {
        return Jackson2ObjectMapperBuilder()
            .modulesToInstall(KotlinModule.Builder().build())
            .build<ObjectMapper>()
            .apply { configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) }
    }

    @Bean
    @ConditionalOnMissingBean(TelegramApi::class)
    fun telegramApi(): TelegramApi {
        return DefaultTelegramApi(telegramProperties.accessKey, telegramProperties.paymentProvider)
    }

    @Bean
    @ConditionalOnMissingBean(name = ["telegramProperties"])
    fun telegramProperties(): TelegramProperties = telegramProperties

    // region Clients

    @Bean
    @ConditionalOnMissingBean(ua.blink.telegraff.client.TelegramClient::class)
    @ConditionalOnProperty(name = ["telegram.mode"], havingValue = "polling", matchIfMissing = true)
    fun telegramPollingClient(telegramApi: TelegramApi, publisher: ApplicationEventPublisher): TelegramPollingClient {
        return TelegramPollingClient(telegramApi, publisher)
    }

    @Bean
    @ConditionalOnMissingBean(ua.blink.telegraff.client.TelegramClient::class)
    @ConditionalOnProperty(name = ["telegram.mode"], havingValue = "webhook")
    fun telegramWebhookClient(telegramApi: TelegramApi, publisher: ApplicationEventPublisher): TelegramWebhookClient {
        // TODO: Reconfigure with one of the following approaches
        /*
        @Bean(name = ["/ruslanys"])
        fun ruslanController(): Controller {
            val aa = RequestMappingHandlerAdapter()
            val bb = RequestMappingHandlerMapping()
            return Controller { request, response ->
                response.writer.print("ok")

                ModelAndView("index")
                // null
            }
        }
        */
        return TelegramWebhookClient(telegramApi, publisher, telegramProperties.getWebhookUrl())
    }

    // endregion

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val redisConfig = RedisStandaloneConfiguration().apply {
            hostName = telegramProperties.redisHostName
            port = telegramProperties.redisPort
            // If you have a password set for Redis, uncomment and set it:
            // setPassword("your-redis-password")
        }
        return LettuceConnectionFactory(redisConfig)
    }

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.setConnectionFactory(connectionFactory)
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()

        return template
    }

    @Bean
    @ConditionalOnMissingBean(HandlersFactory::class)
    fun handlersFactory(
        context: GenericApplicationContext,
        redisTemplate: RedisTemplate<String, String>,
        objectMapper: ObjectMapper
    ): DefaultHandlersFactory {
        return DefaultHandlersFactory(
            handlersPath = telegramProperties.handlersPath,
            context = context,
            redisTemplate = redisTemplate,
            objectMapper = objectMapper
        )
    }

    // region Filters

    @Bean
    @ConditionalOnMissingBean(TelegramFiltersFactory::class, TelegramFilterProcessor::class)
    fun telegramFiltersFactory(filters: List<TelegramFilter>): DefaultTelegramFiltersFactory {
        return DefaultTelegramFiltersFactory(filters)
    }

    @Bean
    @ConditionalOnMissingBean(HandlersFilter::class)
    fun handlersFilter(
        telegramApi: TelegramApi,
        handlersFactory: HandlersFactory
    ): HandlersFilter {
        return HandlersFilter(telegramApi, handlersFactory)
    }

    @Bean
    @ConditionalOnMissingBean(CallbackQueryAnswerFilter::class)
    fun callbackQueryFilter(telegramApi: TelegramApi): CallbackQueryAnswerFilter {
        return CallbackQueryAnswerFilter(telegramApi)
    }
}