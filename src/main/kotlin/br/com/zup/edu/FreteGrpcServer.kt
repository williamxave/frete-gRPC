package br.com.zup.edu

import com.google.protobuf.Any
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FreteGrpcServer : FreteZupServiceGrpc.FreteZupServiceImplBase() {
    private val logger = LoggerFactory.getLogger(FreteGrpcServer::class.java)

    override fun calculaFrete(request: FreteRequest?, responseObserver: StreamObserver<FreteResponse>?) {
        logger.info("Calculando o frete para o cep ${request!!.cep}")

        val cep = request?.cep
        if (cep == null || cep.isBlank()) {
            val e = Status.INVALID_ARGUMENT
                .withDescription("É preciso informar o cep")
                .asRuntimeException()
            responseObserver?.onError(e)
        }

        if (!cep!!.matches("[0-9]{5}-[0-9]{3}".toRegex())) {
            val e = Status.INVALID_ARGUMENT
                .withDescription("Cep inválido")
                .augmentDescription("Formato de cep inválido! Formato esperado 00000-000")
                .asRuntimeException()
            responseObserver?.onError(e)
        }

        var valor = 0.0
        try {
            valor = Random.nextDouble(0.0, 140.0)
            if (valor > 100.0) {
                throw IllegalStateException("Erro inesperado")
            }
        } catch (e: Exception) {
            responseObserver?.onError(
                Status.INTERNAL
                    .withDescription(e.message)
                    .withCause(e) // anexado ao Status, mas não enviado ao Client
                    .asRuntimeException()
            )
        }

        //SIMULAR uma verificação de segurança
        if (cep.endsWith("333")) {
            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(Code.PERMISSION_DENIED.number)
                .setMessage("Usuário não pode acessar esse recurso")
                .addDetails(
                    Any.pack(
                        ErroDetails.newBuilder()
                            .setCode(401)
                            .setMessage("Token expirado")
                            .build()
                    )
                )
                .build()
            val e = StatusProto.toStatusRuntimeException(statusProto)
            responseObserver?.onError(e)
        }

        val response = FreteResponse.newBuilder()
            .setCep(request!!.cep)
            .setValor(valor)
            .build()

        logger.info("Frete calculado: valor $response")

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }
}