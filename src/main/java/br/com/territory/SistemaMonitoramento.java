package br.com.territory;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import br.com.territory.dominio.Alerta;
import br.com.territory.dominio.DadosTerritorio;
import br.com.territory.dominio.EstrategiaPrevisao;
import br.com.territory.dominio.Medicao;
import br.com.territory.dominio.Sensor;
import br.com.territory.excecao.DadosInvalidosException;
import br.com.territory.excecao.FonteIndisponivelException;
import br.com.territory.servico.ConfiguracaoSistema;
import br.com.territory.servico.ExportadorCsv;
import br.com.territory.servico.ServicoMonitoramento;

@SpringBootApplication
public class SistemaMonitoramento {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss");

    public static void main(String[] args) {
        // O Spring sobe o banco e cria os beans; o ApplicationContext é a "fábrica geral"
        ApplicationContext context = SpringApplication.run(SistemaMonitoramento.class, args);
        ServicoMonitoramento servico = context.getBean(ServicoMonitoramento.class);
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n🌡️  TERRITORY PREDICTIVE INSIGHTS - Calor e Energia");
        System.out.println("Região: " + servico.getTerritorio().getRegiao());

        int opcao = -1;
        while (opcao != 0) {
            exibirMenu();
            try {
                opcao = Integer.parseInt(scanner.nextLine().trim());
                executarOpcao(opcao, servico, scanner);
            } catch (NumberFormatException e) {
                System.err.println("❌ Digite apenas o número da opção ou do valor pedido.");
            } catch (DadosInvalidosException e) {
                System.err.println("❌ " + e.getMessage());
            }
        }
        System.out.println("Até logo! O histórico ficou salvo na pasta ./dados.");
    }

    private static void exibirMenu() {
        System.out.println("\n========================================");
        System.out.println("1 - Verificar sensores e registrar leituras");
        System.out.println("2 - Rodar previsões e emitir alertas");
        System.out.println("3 - Ver histórico de um sensor");
        System.out.println("4 - Ver alertas emitidos");
        System.out.println("5 - Gerenciar dados do território");
        System.out.println("6 - Status completo");
        System.out.println("7 - Exportar histórico em CSV (bônus)");
        System.out.println("0 - Sair");
        System.out.print("Escolha: ");
    }

    private static void executarOpcao(int opcao, ServicoMonitoramento servico, Scanner scanner) {
        switch (opcao) {
            case 1 -> verificarSensores(servico, scanner);
            case 2 -> rodarPrevisoes(servico);
            case 3 -> verHistorico(servico, scanner);
            case 4 -> verAlertas(servico);
            case 5 -> gerenciarTerritorio(servico, scanner);
            case 6 -> exibirStatusCompleto(servico);
            case 7 -> exportarCsv(servico, scanner);
            case 0 -> System.out.println("Encerrando...");
            default -> System.out.println("Opção inválida.");
        }
    }

    // 1. Cada rodada consulta os 3 sensores; um sensor com problema não derruba os outros
    private static void verificarSensores(ServicoMonitoramento servico, Scanner scanner) {
        System.out.println("\n" + servico.getTransformador().verificarStatus());
        System.out.print("Quantas rodadas de leitura? (1 a 24): ");
        int rodadas = Integer.parseInt(scanner.nextLine().trim());
        if (rodadas < 1 || rodadas > 24) {
            throw new DadosInvalidosException("o número de rodadas deve ficar entre 1 e 24: " + rodadas);
        }

        for (int rodada = 1; rodada <= rodadas; rodada++) {
            System.out.println("\n--- Rodada " + rodada + " ---");
            for (Sensor sensor : servico.getSensores()) {
                try {
                    Medicao medicao = servico.registrarLeitura(sensor);
                    System.out.printf("✅ %-10s %-38s %6.1f %s (gravado)%n", sensor.getCodigo(),
                            sensor.retornarTipo(), medicao.getValor(), sensor.retornarUnidade());
                } catch (FonteIndisponivelException e) {
                    System.err.println("📡 " + e.getMessage() + ". Leitura ignorada nesta rodada.");
                } catch (DadosInvalidosException e) {
                    System.err.println("⚠️ " + sensor.getCodigo() + ": " + e.getMessage() + ". Leitura descartada.");
                }
            }
        }
    }

    // 2. Polimorfismo: o mesmo comando prever() para cada estratégia
    private static void rodarPrevisoes(ServicoMonitoramento servico) {
        List<EstrategiaPrevisao> estrategias = servico.getEstrategias();
        for (Sensor sensor : servico.getSensores()) {
            System.out.println("\n📈 " + sensor.retornarTipo());
            for (EstrategiaPrevisao estrategia : estrategias) {
                try {
                    System.out.println("   " + servico.preverEAvaliar(sensor, estrategia));
                } catch (DadosInvalidosException e) {
                    System.err.println("   🤐 Previsão não emitida: " + e.getMessage()
                            + ". Registre mais leituras na opção 1.");
                }
            }
        }
    }

    // 3. Histórico persistido no banco, por sensor
    private static void verHistorico(ServicoMonitoramento servico, Scanner scanner) {
        Sensor sensor = escolherSensor(servico, scanner);
        List<Medicao> historico = servico.buscarHistoricoPorSensor(sensor.getCodigo());
        System.out.println("\n📜 Histórico de " + sensor.getCodigo() + " (" + historico.size() + " leituras)");
        for (Medicao medicao : historico) {
            System.out.printf("   %s  %6.1f %s%n", medicao.getDataHora().format(FORMATO_DATA),
                    medicao.getValor(), sensor.retornarUnidade());
        }
    }

    // 4. Alertas já emitidos (também vêm do banco)
    private static void verAlertas(ServicoMonitoramento servico) {
        List<Alerta> alertas = servico.listarAlertas();
        System.out.println("\n🚨 Alertas emitidos: " + alertas.size());
        for (Alerta alerta : alertas) {
            System.out.printf("[%s] %s  %s%n   O que fazer: %s%n", alerta.getNivel().getDescricao(),
                    alerta.getDataHora().format(FORMATO_DATA), alerta.getMotivo(), alerta.getNivel().getOrientacao());
        }
    }

    // 5. Dados do território: setters validam e o limiar exige o código de acesso
    private static void gerenciarTerritorio(ServicoMonitoramento servico, Scanner scanner) {
        DadosTerritorio territorio = servico.getTerritorio();
        System.out.println("\n🏘️  " + territorio.getRegiao());
        System.out.println("População: " + territorio.getPopulacao()
                + " | População vulnerável ao calor: " + territorio.getPopulacaoVulneravel());
        System.out.println("1 - Alterar população");
        System.out.println("2 - Alterar população vulnerável");
        System.out.println("3 - Alterar limiar de alerta (pede código de acesso)");
        System.out.println("0 - Voltar");
        System.out.print("Escolha: ");
        int opcao = Integer.parseInt(scanner.nextLine().trim());

        switch (opcao) {
            case 1 -> {
                System.out.print("Nova população: ");
                territorio.setPopulacao(Integer.parseInt(scanner.nextLine().trim()));
                System.out.println("✅ População atualizada.");
            }
            case 2 -> {
                System.out.print("Nova população vulnerável: ");
                territorio.setPopulacaoVulneravel(Integer.parseInt(scanner.nextLine().trim()));
                System.out.println("✅ População vulnerável atualizada.");
            }
            case 3 -> {
                Sensor sensor = escolherSensor(servico, scanner);
                System.out.print("Novo limiar (" + sensor.retornarUnidade() + "): ");
                double novoLimite = Double.parseDouble(scanner.nextLine().trim().replace(",", "."));
                System.out.print("Código de acesso: ");
                servico.alterarLimiar(scanner.nextLine().trim(), sensor.retornarTipo(), novoLimite);
                System.out.println("✅ Limiar de " + sensor.retornarTipo() + " agora é " + novoLimite);
            }
            default -> System.out.println("Voltando ao menu.");
        }
    }

    // 6. Visão geral: território, transformador, leituras, limiares e último alerta
    private static void exibirStatusCompleto(ServicoMonitoramento servico) {
        DadosTerritorio territorio = servico.getTerritorio();
        ConfiguracaoSistema config = ConfiguracaoSistema.getInstancia();
        System.out.println("\n📋 STATUS COMPLETO");
        System.out.println("Território: " + territorio.getRegiao() + " | " + territorio.getPopulacao()
                + " habitantes, " + territorio.getPopulacaoVulneravel() + " vulneráveis ao calor");
        System.out.println(servico.getTransformador().verificarStatus());
        for (Sensor sensor : servico.getSensores()) {
            System.out.printf("   %-10s %-38s leituras no banco: %3d | limiar: %.1f %s%n", sensor.getCodigo(),
                    sensor.retornarTipo(), servico.contarLeituras(sensor.getCodigo()),
                    config.buscarLimiar(sensor.retornarTipo()), sensor.retornarUnidade());
        }
        List<Alerta> alertas = servico.listarAlertas();
        System.out.println("Alertas emitidos: " + alertas.size());
        if (!alertas.isEmpty()) {
            Alerta ultimo = alertas.get(0);
            System.out.println("Último alerta: [" + ultimo.getNivel().getDescricao() + "] " + ultimo.getMotivo());
        }
    }

    // 7. Bônus: exporta o histórico de um sensor para CSV
    private static void exportarCsv(ServicoMonitoramento servico, Scanner scanner) {
        Sensor sensor = escolherSensor(servico, scanner);
        List<Medicao> historico = servico.buscarHistoricoPorSensor(sensor.getCodigo());
        try {
            String arquivo = new ExportadorCsv().exportar(sensor.getCodigo(), historico);
            System.out.println("✅ " + historico.size() + " leituras exportadas para " + arquivo);
        } catch (IOException e) {
            System.err.println("❌ Não foi possível gravar o arquivo CSV: " + e.getMessage());
        }
    }

    private static Sensor escolherSensor(ServicoMonitoramento servico, Scanner scanner) {
        List<Sensor> sensores = servico.getSensores();
        for (int i = 0; i < sensores.size(); i++) {
            System.out.println((i + 1) + " - " + sensores.get(i).getCodigo() + " (" + sensores.get(i).retornarTipo() + ")");
        }
        System.out.print("Sensor: ");
        int escolha = Integer.parseInt(scanner.nextLine().trim());
        if (escolha < 1 || escolha > sensores.size()) {
            throw new DadosInvalidosException("escolha um sensor entre 1 e " + sensores.size());
        }
        return sensores.get(escolha - 1);
    }
}
