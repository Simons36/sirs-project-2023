package GrooveServer;

import io.grpc.BindableService;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptors;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import GrooveServer.service.ServerService;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class AppServerInit implements Runnable {

    private int port;

    public AppServerInit(int port) {
        this.port = port;
    }

    public void run() {

        final BindableService service = new ServerService();

        try {
            // Load the Java KeyStore (containing server's certificate and private key)
            String keystorePath = "keys/server.p12";
            String trustStorePath = "keys/servertruststore.jks";
            String keystorePassword = "changeme";

            KeyStore keyStore = KeyStore.getInstance("pkcs12");
            try (FileInputStream fis = new FileInputStream(keystorePath)) {
                keyStore.load(fis, keystorePassword.toCharArray());
            }
            KeyStore trustStore = KeyStore.getInstance("JKS");
            try (FileInputStream fis = new FileInputStream(trustStorePath)) {
                trustStore.load(fis, keystorePassword.toCharArray());
            }

            // Create KeyManagerFactory for the server's keystore
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
            
            // Create the trust Manager Factory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            // Create an SslContext with KeyManager for the server to authenticate itself to the client
            SslContext sslContext = GrpcSslContexts.configure(SslContextBuilder.forServer(keyManagerFactory))
                    .trustManager(trustManagerFactory)
                    .build();

            // Build and start the gRPC server
            Server server = NettyServerBuilder.forPort(port)
                    .sslContext(sslContext)
                    .addService(service)
                    .build()
                    .start();

            /*Server server = ServerBuilder.forPort(port).addService(service).build();
            server.start();*/

            System.out.println("Application Server started, listening on " + port);

            // Await termination of the server
            server.awaitTermination();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}