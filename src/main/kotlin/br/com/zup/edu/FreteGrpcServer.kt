package br.com.zup.edu

import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FreteGrpcServer : FreteZupServiceGrpc.FreteZupServiceImplBase() {
    private val logger = LoggerFactory.getLogger(FreteGrpcServer::class.java)

    override fun calculaFrete(request: FreteRequest?, responseObserver: StreamObserver<FreteResponse>?) {
        logger.info("Calculando o frete para o cep ${request!!.cep}")

        val response = FreteResponse.newBuilder()
            .setCep(request!!.cep)
            .setValor(Random.nextDouble(0.0, 140.0))
            .build()

        logger.info("Frete calculado: valor $response")

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }
}