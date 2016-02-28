package net.devopssolutions.demo.ws.server.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.IntNode
import net.devopssolutions.demo.ws.rpc.*
import net.devopssolutions.demo.ws.server.component.WsBroadcaster
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import rx.Observable
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.security.Principal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.function.Consumer
import java.util.zip.GZIPOutputStream

@Component
@RpcMethod(RpcMethods.FLOOD)
class FloodHandler : RpcMethodHandler {
    private val log = org.slf4j.LoggerFactory.getLogger(FloodHandler::class.java)

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var wsBroadcaster: WsBroadcaster

    override fun handle(id: String, params: Any, user: Principal) {
        log.info("handling rpc message id: {}, method: {} params: {}", id, RpcMethods.FLOOD, params)

        Observable.range(0, (params as IntNode).intValue())
                .map { createRpcMessage(id, it) }
                .map { getByteBuffer(it) }
                .doOnError { log.warn("exception in flood handler", it) }
                .forEach {
                    wsBroadcaster.broadcastAsync(Consumer { session ->
                        synchronized (session) {
                            session.basicRemote.sendBinary(it)
                        }
                    })
                }
    }

    private fun getByteBuffer(value: RpcMessage<*, *>): ByteBuffer? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        objectMapper.writeValue(GZIPOutputStream(byteArrayOutputStream), value)
        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray())
    }

    private fun createRpcMessage(id: String, response: Any): RpcMessage<Unit, Any> = RpcMessage(
            id = id,
            created = LocalDateTime.now(ZoneOffset.UTC),
            method = RpcMethods.FLOOD,
            type = RpcType.RESPONSE,
            response = response)

}