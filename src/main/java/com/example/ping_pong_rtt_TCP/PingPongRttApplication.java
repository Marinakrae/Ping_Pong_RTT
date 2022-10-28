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

                DataOutputStream outToServer = new DataOutputStream(socket
                        .getOutputStream());

                System.out.println("Ping enviado");

                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));

                outToServer.writeBytes(pacote.getTimeNano().toString());

                tempoServidor = inFromServer.readLine();

                long timeNano = System.nanoTime() - Long.parseLong(tempoServidor);
                System.out.println("RTT de "+ timeNano + "ns");

                socket.close();
        }
    }

    private class Servidor implements Runnable {
        @SneakyThrows
        @Override
        public void run() {

            String tempoCliente;
            ServerSocket welcomeSocket = new ServerSocket(5555);

            Socket connectionSocket = welcomeSocket.accept();

            while (true) {
                BufferedReader inFromClient = new BufferedReader(
                        new InputStreamReader(
                                connectionSocket.getInputStream()
                        )
                );
                DataOutputStream outToClient = new DataOutputStream(
                        connectionSocket.getOutputStream()
                );

                tempoCliente = inFromClient.readLine();

                Date data = new Date();
                tempoCliente = data.toString();

                System.out.println("Pong devolvido");
                outToClient.writeBytes(tempoCliente);
                // connectionSocket.close();
            }
        }
    }
}

