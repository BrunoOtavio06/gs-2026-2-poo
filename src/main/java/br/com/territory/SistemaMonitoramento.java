package br.com.territory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SistemaMonitoramento {

    public static void main(String[] args) {
        SpringApplication.run(SistemaMonitoramento.class, args);
        System.out.println("\n🌡️  TERRITORY PREDICTIVE INSIGHTS - Calor e Energia");
        System.out.println("Projeto configurado. O menu chega quando as camadas estiverem prontas.");
    }
}
