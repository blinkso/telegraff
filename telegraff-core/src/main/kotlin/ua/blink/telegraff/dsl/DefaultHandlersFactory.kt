package ua.blink.telegraff.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import org.jetbrains.kotlin.utils.addToStdlib.measureTimeMillisWithResult
import org.slf4j.LoggerFactory
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component
import javax.script.ScriptEngineFactory
import kotlin.coroutines.CoroutineContext

@Component
class DefaultHandlersFactory(
    private val context: GenericApplicationContext,
    handlersPath: String
) : HandlersFactory, CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Job() + Dispatchers.Default

    private val resolver = PathMatchingResourcePatternResolver(javaClass.classLoader)
    private val handlers: MutableMap<String, Handler> = hashMapOf()

    init {
        launch {
            val factory: ScriptEngineFactory = KotlinJsr223JvmLocalScriptEngineFactory()

            val resources: Array<Resource> = try {
                resolver.getResources("classpath:$handlersPath/*.kts")
            } catch (e: Exception) {
                log.warn("Can not load handlers by path `{}`: {}", handlersPath, e.message)
                arrayOf()
            }


            for (resource in resources) {
                val handler = try {
                    compile(factory, resource)
                } catch (e: Exception) {
                    log.warn("Can not compile {}: {}", resource, e.message)
                    continue
                }
                addHandler(handler)
            }
        }
    }

    private suspend fun compile(factory: ScriptEngineFactory, resource: Resource): Handler {
        val scriptEngine = factory.scriptEngine

        val compiled = measureTimeMillisWithResult {
            resource.inputStream.bufferedReader().use {
                @Suppress("UNCHECKED_CAST")
                scriptEngine.eval(it) as HandlerDslWrapper
            }
        }

        log.info("Compilation for ${resource.filename} took ${compiled.first} ms")

        return compiled.second(context)
    }

    private fun addHandler(handler: Handler) {
        for (rawCommand in handler.commands) {
            val command = rawCommand.lowercase()
            val previousValue = handlers.put(command, handler)
            if (previousValue != null) {
                throw IllegalArgumentException("$command is already in use.")
            }
        }
    }

    override fun getHandlers(): Map<String, Handler> {
        return handlers
    }

    companion object {
        private val log = LoggerFactory.getLogger(DefaultHandlersFactory::class.java)
    }
}
