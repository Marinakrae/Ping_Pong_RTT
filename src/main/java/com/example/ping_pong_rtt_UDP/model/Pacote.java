package com.example.ping_pong_rtt_UDP.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Pacote implements Serializable {

    private TipoPacote tipo;
    private Long timeNano;
    private String nome;

    public enum TipoPacote { PING, PONG }
}
