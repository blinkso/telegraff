package ua.blink.telegraff.component

import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.codec.CodecConfigurer
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import ua.blink.telegraff.dto.*
import ua.blink.telegraff.dto.request.*
import java.time.Duration

class DefaultTelegramApi(
    telegramAccessKey: String,
    private val paymentProviderToken: String?,
) : TelegramApi {

    private val restTemplate = WebClient.builder()
        .baseUrl("https://api.telegram.org/bot$telegramAccessKey")
        .exchangeStrategies(
            ExchangeStrategies.builder()
                .codecs(this::configureCodecs)
                .build()
        )
        .build()
    private val fileRestTemplate = WebClient.builder()
        .baseUrl("https://api.telegram.org/file/bot$telegramAccessKey")
        .exchangeStrategies(
            ExchangeStrategies.builder()
                .codecs(this::configureCodecs)
                .build()
        )
        .build()

    private fun configureCodecs(configurer: CodecConfigurer) {
        configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) // 16MB
    }

    override fun getMe(): TelegramUser {
        return restTemplate.get()
            .uri("/getMe")
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("getMe()") }
            )
            .bodyToMono(object : ParameterizedTypeReference<TelegramResponse<TelegramUser>>() {})
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .toFuture()
            .get()
            .result!!
    }

    override fun getUpdates(offset: Long?, timeout: Int?): List<TelegramUpdate> {
        val params = hashMapOf<String, Any>()
        offset?.let { params["offset"] = it }
        timeout?.let { params["timeout"] = it }

        return restTemplate
            .post()
            .uri("/getUpdates")
            .body(BodyInserters.fromValue(params))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("getUpdates($offset $timeout)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<TelegramResponse<List<TelegramUpdate>>>() {})
            .toFuture()
            .get()
            .result!!
    }

    override fun getFile(fileId: String): TelegramFile {
        val params = hashMapOf("file_id" to fileId)

        return restTemplate
            .post()
            .uri("/getFile")
            .body(BodyInserters.fromValue(params))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("getFile($fileId)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<TelegramResponse<TelegramFile>>() {})
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .toFuture()
            .get()
            .result!!
    }

    override fun getFileByPath(filePath: String): ByteArray {
        val params = mapOf<String, String>()

        return fileRestTemplate.get()
            .uri("/$filePath", params)
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("getFileByPath($filePath)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<ByteArray>() {})
            .toFuture()
            .get()
    }

    override fun setWebhook(url: String): Boolean {
        val params = hashMapOf("url" to url)

        return restTemplate
            .post()
            .uri("/setWebhook")
            .body(BodyInserters.fromValue(params))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("setWebhook($url)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<TelegramResponse<Boolean>>() {})
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .toFuture()
            .get()
            .result!!
    }

    override fun removeWebhook(): Boolean {
        return setWebhook("")
    }

    override fun sendMessage(request: TelegramMessageSendRequest): TelegramMessage {
        return restTemplate
            .post()
            .uri("/sendMessage")
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("sendMessage($request)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<TelegramResponse<TelegramMessage>>() {})
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .toFuture()
            .get()
            .result!!
    }

    override fun sendPayment(request: TelegramPaymentRequest): TelegramMessage {
        return restTemplate
            .post()
            .uri("/sendInvoice")
            .body(BodyInserters.fromValue(request.apply { providerToken = paymentProviderToken ?: "" }))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("sendPayment($request)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<TelegramResponse<TelegramMessage>>() {})
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .toFuture()
            .get()
            .result!!
    }

    override fun sendDocument(request: TelegramDocumentSendRequest): TelegramMessage {
        val formData = createFormData(request).apply {
            add("document", object : ByteArrayResource(request.document) {
                override fun getFilename(): String {
                    return request.name
                }
            })
        }

        return restTemplate
            .post()
            .uri("/sendDocument")
            .body(BodyInserters.fromMultipartData(formData))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("sendDocument($request)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<TelegramResponse<TelegramMessage>>() {})
            .toFuture()
            .get()
            .result!!
    }

    override fun sendPhoto(request: TelegramPhotoSendRequest): TelegramMessage {
        val formData = createFormData(request).apply {
            add("photo", object : ByteArrayResource(request.photo) {
                override fun getFilename(): String {
                    return "picture.png"
                }
            })
        }

        return restTemplate
            .post()
            .uri("/sendPhoto")
            .body(BodyInserters.fromMultipartData(formData))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("sendPhoto($request)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<TelegramResponse<TelegramMessage>>() {})
            .toFuture()
            .get()
            .result!!
    }

    override fun sendVoice(request: TelegramVoiceSendRequest): TelegramMessage {
        val formData = createFormData(request).apply {
            add("voice", object : ByteArrayResource(request.voice) {
                override fun getFilename(): String {
                    return "voice.mp3"
                }
            })
        }

        return restTemplate
            .post()
            .uri("/sendVoice")
            .body(BodyInserters.fromMultipartData(formData))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("sendVoice($request)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<TelegramResponse<TelegramMessage>>() {})
            .toFuture()
            .get()
            .result!!
    }

    override fun sendChatAction(request: TelegramChatActionRequest): Boolean {
        return restTemplate
            .post()
            .uri("/sendChatAction")
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("sendChatAction($request)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<TelegramResponse<Boolean>>() {})
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .toFuture()
            .get()
            .result!!
    }

    override fun sendAnswerCallbackQuery(callbackQueryId: Long): Boolean {
        val params = hashMapOf("callback_query_id" to callbackQueryId)

        return restTemplate
            .post()
            .uri("/answerCallbackQuery")
            .body(BodyInserters.fromValue(params))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("sendAnswerCallbackQuery($callbackQueryId)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<TelegramResponse<Boolean>>() {})
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .toFuture()
            .get()
            .result!!
    }

    override fun sendAnswerPreCheckoutQuery(preCheckoutQueryId: Long, errorMessage: String?): Boolean {
        val params = hashMapOf<String, Any>()
        params["pre_checkout_query_id"] = preCheckoutQueryId
        if (errorMessage != null) {
            params["ok"] = false
            params["error_message"] = errorMessage
        } else {
            params["ok"] = true
        }

        return restTemplate
            .post()
            .uri("/answerPreCheckoutQuery")
            .body(BodyInserters.fromValue(params))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("sendAnswerPreCheckoutQuery($preCheckoutQueryId $errorMessage)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<TelegramResponse<Boolean>>() {})
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .toFuture()
            .get()
            .result!!
    }

    override fun setMyCommands(locale: String?, commands: List<TelegramBotCommand>): Boolean {
        val request = TelegramBotCommandsSendRequest(
            commands = commands,
            languageCode = locale
        )

        return restTemplate
            .post()
            .uri("/setMyCommands")
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("setMyCommands($locale $commands)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<TelegramResponse<Boolean>>() {})
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .toFuture()
            .get()
            .result!!
    }

    override fun deleteMyCommands(locale: String?): Boolean {
        val request = TelegramBotCommandsSendRequest(
            languageCode = locale
        )

        return restTemplate
            .post()
            .uri("/deleteMyCommands")
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("deleteMyCommands($locale)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<TelegramResponse<Boolean>>() {})
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .toFuture()
            .get()
            .result!!
    }

    private fun createFormData(request: TelegramMediaSendRequest): LinkedMultiValueMap<String, Any> =
        LinkedMultiValueMap<String, Any>().apply {
            add("chat_id", request.chatId)
            add("reply_markup", request.replyKeyboard)
            add("disable_notification", request.disableNotification)
            request.caption?.let { add("caption", request.caption) }
            request.parseMode?.let { add("parse_mode", request.parseMode.name) }
        }

    private fun ClientResponse.handleError(logMarker: String): Mono<Throwable> {
        return bodyToMono(String::class.java).flatMap { errorBody ->
            val responseBody = "$logMarker ${statusCode()} $errorBody"
            val responseException = RuntimeException(responseBody)
            log.error(responseBody, responseException)
            Mono.error(responseException)
        }
    }

    private companion object {
        private val log = LoggerFactory.getLogger(DefaultTelegramApi::class.java)
        private const val REQUEST_TIMEOUT_SECONDS = 10L
    }
}