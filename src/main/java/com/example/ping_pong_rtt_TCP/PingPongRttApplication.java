package com.example.ping_pong_rtt_TCP;

import com.example.ping_pong_rtt_UDP.model.Pacote;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class PingPongRttApplication {

    public static void main(String[] args) {
        new PingPongRttApplication();
    }

    public PingPongRttApplication() {
        new Thread(new Servidor()).start();
        new Thread(new Cliente()).start();
    }

    private class Cliente implements Runnable {
        @SneakyThrows
        @Override
        public void run() {

            Scanner scanner = new Scanner(System.in);
            String tempoServidor;


                System.out.println("Digite o endereço: ");
                String endereco = scanner.nextLine();

                Socket socket = new Socket(endereco, 5555);

                Pacote pacote = Pacote.builder()
                        .tipo(Pacote.TipoPacote.PING)
                        .nome("Marina")
                        .timeNano(System.nanoTime()).build();
                String strPacote = new ObjectMapper().writeValueAsString(pacote);

                socket.getOutputStream().write(strPacote.getBytes(StandartCharsets.UTF_8));
                socket.getOutputStream().flush();

                System.out.println("Ping enviado");

                byte[] buffer = new byte[1024];
                int size = socket.getInputStream().read(buffer);
                String strRetorno = new String(buffer, 0, size);

                Pacote pacoteRetorno = new ObjectMapper().readValue(strRetorno, Pacote.class);
                long timeNano = System.nanoTime() - pacoteRetorno.getTimestampNanos();
                System.out.println("RTT de "+ timeNano + "ns");

                socket.close();
        }
    }

    private class Servidor implements Runnable {
        @SneakyThrows
        @Override
        public void run() {

            String tempoCliente;
            ServerSocket ss = new ServerSocket(5555);

            while (true) {

                Socket socket = ss.accept();

                byte[] buffer = new byte[1024];
                int size = socket.getInputStream().read(buffer);

                Pacote pacote = new ObjectMapper().readValue(new String(buffer, 0, size), Pacote.class);

                System.out.println("Recebeu pacote" + pacote);

                if (pacote.getTipo() == Pacote.Tipo.PING) {
                    socket.getOutputStream().write(buffer, 0, size);
                }
                socket.close();
            }
        }
    }
}

