package com.example.ping_pong_rtt_UDP;

import com.example.ping_pong_rtt_UDP.model.Pacote;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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

    private class Cliente implements Runnable{
        @SneakyThrows
        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            DatagramSocket socket = new DatagramSocket();
            while (true){
//                System.out.println("Digite o endereço: ");
//                String endereco = scanner.nextLine();
                Pacote pacote = Pacote.builder()
                        .tipo(Pacote.TipoPacote.PING)
                        .nome("Marina")
                        .timeNano(System.nanoTime()).build();
                String strPacote = new ObjectMapper().writeValueAsString(pacote);
                byte[] bArray = strPacote.getBytes("UTF-8");
                DatagramPacket udpPacket = new DatagramPacket(bArray,
                        0,
                        bArray.length,
                        InetAddress.getByName("255.255.255.255"),
                        5555
                );
                System.out.println("Conectando ao endereço 255.255.255.255");
                socket.send(udpPacket);
                System.out.println("Ping enviado");

                //Receber a resposta
                byte[] buf = new byte[1024];
                DatagramPacket resp = new DatagramPacket(buf, buf.length);
                socket.receive(resp);
                String strResp = new String(buf, 0, resp.getLength(), "UTF-8");
                Pacote pacoteResp = new ObjectMapper().readValue(strResp, Pacote.class);
                if (pacoteResp.getTipo() == Pacote.TipoPacote.PONG){
                    long timeNano = System.nanoTime() - pacote.getTimeNano();
                    System.out.println("RTT de "+ timeNano + "ns");
                } else {
                    System.out.println("Pacote recebido de "+ resp.getAddress().getHostName()+ " deveria ser Pong.");
                }
                TimeUnit.SECONDS.sleep(5);
            }
        }
    }
    private class Servidor implements Runnable {
        @SneakyThrows
        @Override
        public void run() {
            //Recebe o pacote do cliente e muda seu tipo para PONG, aí retorna para o endereco e a porta
            DatagramSocket socketServidor = new DatagramSocket(5555);
            while (true){
                byte[] buf = new byte[1024];
                DatagramPacket resp = new DatagramPacket(buf, buf.length);
                socketServidor.receive(resp);
                String strResp = new String(buf, 0, resp.getLength(), "UTF-8");
                Pacote pacoteResp = new ObjectMapper().readValue(strResp, Pacote.class);
                pacoteResp.setTipo(Pacote.TipoPacote.PONG);
                pacoteResp.setNome("Servidor-Marina");
                Date data = new Date();
                pacoteResp.setTimeNano(data.getTime());

                String strPacote = new ObjectMapper().writeValueAsString(pacoteResp);
                byte[] bArray = strPacote.getBytes("UTF-8");
                DatagramPacket udpPacket = new DatagramPacket(bArray,
                        0,
                        bArray.length,
                        resp.getAddress(),
                        resp.getPort()
                );

                socketServidor.send(udpPacket);
                System.out.println("Pong devolvido");
            }
        }
    }
}
