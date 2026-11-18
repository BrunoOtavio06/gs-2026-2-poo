# Territory Predictive Insights: calor e energia

Global Solution do 2º semestre de 2026, disciplina de Programação Orientada a Objetos.

| Integrante                        | RM     |
| --------------------------------- | ------ |
| Bruno Otávio da Cruz Carvalho     | 562354 |
| João Vitor Santana Silva Ribeiro  | 564693 |
| Letícia Gabrielle Andrade Temóteo | 563985 |

Recorte escolhido: ondas de calor e risco de sobrecarga na rede elétrica.

## Público beneficiado

O sistema foi pensado para a equipe de uma associação de bairro ou de uma subprefeitura que acompanha um setor da Zona Leste de São Paulo atendido por um único transformador de distribuição. Em dias de onda de calor, a população mais afetada são idosos e crianças pequenas, e o uso de ventiladores e ar-condicionado no fim da tarde aumenta a carga da rede até o transformador superaquecer ou desligar.

Uma queda de energia num momento como esse deixa as pessoas sem ventilação justamente quando o tempo está mais quente.

Nosso sistema foi construído em cima desse problema: com base nas leituras das últimas horas, a equipe deve abrir os pontos de resfriamento, pedir à população que adie o uso de aparelhos de alto consumo, ou acionar a Defesa Civil e a concessionária.

Todos os dados nesse sistema foram simulados e são fictícios.

## Como rodar

É preciso ter o JDK 17 ou mais recente instalado. O Maven não precisa ser instalado, porque o projeto traz o Maven Wrapper (`mvnw`). O banco é o H2, que vem como dependência do projeto e grava os dados na pasta `dados/`, criada sozinha na primeira execução.

Pelo terminal, na pasta do projeto:

```bash
./mvnw spring-boot:run
```

Para rodar os testes:

```bash
./mvnw test
```

Um guia curto para testar o sistema:

1. Opção 1 com 8 rodadas, para gravar leituras dos três sensores. Algumas leituras falham de propósito (sensor sem resposta ou valor corrompido) e o menu explica o que aconteceu.
2. Opção 2, para rodar as duas previsões em cada sensor e emitir os alertas.
3. Opções 3 e 4, para ver o histórico de um sensor e os alertas gravados.
4. Opção 5, e em seguida 3, para tentar mudar um limiar. O código de acesso é `GS2026`. O sistema recusa a alteração se testado com outro código.
5. Sair com 0, rodar de novo e abrir a opção 6. As leituras e os alertas continuam lá.

## O que o sistema faz

Um transformador de 500 kVA tem três sensores instalados. O termômetro de rua mede a temperatura do ar, o medidor de carga informa quanto da capacidade do transformador está em uso, e o termômetro do óleo isolante mede o aquecimento do próprio transformador. As leituras são simuladas com `Math.random()` em faixas de uma tarde de verão em onda de calor.

Cada leitura válida é gravada no banco. E então, usando esse histórico, rodam duas previsões. A média móvel calcula a média das últimas 5 leituras. A comparação com o período anterior compara a média das últimas 3 leituras com a das 3 anteriores e supõe que a tendência continua. O valor previsto é comparado com o limiar do sensor:

| Sensor                               | Limiar inicial     |
| ------------------------------------ | ------------------ |
| Temperatura do ar                    | 35 °C              |
| Carga da rede                        | 85 % da capacidade |
| Temperatura do óleo do transformador | 95 °C              |

Quando a previsão alcança o limiar, o sistema grava um alerta de ATENÇÃO. A partir de 10% acima do limiar, o alerta vira ALERTA, e a partir de 20% acima, vira CRÍTICO.

Cada alerta guarda o sensor, o valor previsto, o método usado e o limite estourado, e o menu mostra o que a equipe deve fazer em cada nível.

## Estrutura

```
src/main/java/br/com/territory/
├── dominio/       ComponenteTerritorial, Transformador, Sensor, SensorTemperaturaAr,
│                  SensorCargaRede, SensorTemperaturaOleo, DadosTerritorio,
│                  EstrategiaPrevisao, PrevisaoMediaMovel, PrevisaoComparacao,
│                  Medicao, Alerta, NivelAlerta
├── excecao/       DadosInvalidosException, FonteIndisponivelException
├── persistencia/  MedicaoRepository, AlertaRepository
├── servico/       ServicoMonitoramento, FabricaPrevisao, ConfiguracaoSistema, ExportadorCsv
└── SistemaMonitoramento.java   (menu principal)
src/test/java/br/com/territory/
├── dominio/       MedicaoTest, PrevisaoMediaMovelTest, PrevisaoComparacaoTest
└── servico/       ServicoMonitoramentoTest
```

## Decisões da equipe

**Spring Boot com JPA e H2 em arquivo.** A equipe escolheu o caminho Spring Data JPA porque é o que usamos nas aulas 13 a 15, e o Spring fecha as conexões sozinho. O H2 roda dentro do próprio programa, então quem corrige não precisa instalar banco nenhum.

**Transformador como componente do território.** `ComponenteTerritorial` é abstrata porque não existe um componente genérico no bairro. A filha concreta é o `Transformador`, que é o equipamento em risco no nosso recorte. Ele tem os sensores instalados (associação) e implementa `verificarStatus()` consultando cada um deles. Os três sensores implementam a interface `Sensor`, e o resto do sistema só conhece a interface.

**Previsões sem if de tipo.** `EstrategiaPrevisao` é abstrata. As filhas sobrescrevem `prever()` e chamam `super.prever()`, que confere se há leituras suficientes, antes de fazer a própria conta. O menu recebe a lista de estratégias da fábrica e chama `prever()` em todas. Cada objeto calcula do seu jeito, sem `if (tipo == ...)` e sem `instanceof`.

**Exceções.** As duas exceções estendem `RuntimeException`. `DadosInvalidosException` sai dos setters e dos serviços (leitura fora da faixa física, população negativa, limiar zero ou negativo, código de acesso errado, ou histórico curto demais). `FonteIndisponivelException` sai do sensor que não respondeu. O menu captura cada uma com uma mensagem própria, e um sensor com problema não impede a leitura dos outros.

**Padrões criacionais.** Usamos dois:

- `FabricaPrevisao` (Factory) é um `@Component` do Spring e monta as estratégias com os parâmetros da configuração (janela de 5 leituras para a média móvel e períodos de 3 para a comparação). Ela fica aqui porque é o único ponto do sistema em que um objeto depende de configuração para nascer. O serviço e o menu pedem as estratégias à fábrica e nunca dão `new` nelas. Se a janela mudar, muda só a fábrica.
- `ConfiguracaoSistema` (Singleton) guarda os limiares, com construtor privado e `getInstancia()` sincronizado. Os limiares precisam ser os mesmos para o serviço, para a fábrica e para o menu. Com duas instâncias, uma alteração feita no menu poderia não valer para o cálculo do alerta. Os sensores, as medições e os alertas não são singletons porque existem vários de cada.

**Código de acesso para os limiares.** Mudar um limiar muda quando o bairro recebe um alerta, então `DadosTerritorio` guarda um código de acesso sem getter e só expõe `validarCodigoAcesso()`. O serviço confere o código antes de alterar o limiar.

**Bônus.** A opção 7 do menu exporta o histórico de um sensor para `historico-<sensor>.csv`, separado por ponto e vírgula para abrir direto no Excel.

## Reflexão crítica

**Quando o sistema deve se calar.** Com menos de 5 leituras no banco, nenhuma previsão é emitida. A média de 2 ou 3 valores muda muito a cada leitura nova, e um alerta baseado nisso acionaria a equipe por acaso. A comparação com o período anterior pede 6 leituras, porque precisa de dois períodos de 3. Quando falta histórico, o menu diz quantas leituras existem e quantas faltam, em vez de mostrar um número. O sistema também descarta leituras impossíveis, como carga de -999%, em vez de deixar que elas puxem a média.

**O que os dados não representam.** Um termômetro instalado no poste do transformador mede a temperatura daquele ponto. Ele não mostra o calor dentro de uma casa de telhado de fibrocimento, que costuma ser maior, nem a diferença entre uma rua arborizada e outra sem sombra. A carga do transformador também não diz quais casas estão consumindo, nem se há alguém que depende de um equipamento médico elétrico. As previsões são simples de propósito: a média móvel reage devagar a uma mudança brusca, e a comparação exagera quando uma única leitura foge do normal. Os números servem para acompanhar a tendência, e não para afirmar o que vai acontecer.

**Por que o alerta apoia a decisão em vez de decidir.** Abrir um ponto de resfriamento, chamar a Defesa Civil ou pedir que o bairro desligue aparelhos tem custo e afeta pessoas, e a equipe sabe coisas que o sistema não sabe: se há um evento na praça, se a concessionária já está trocando o transformador, quem mora sozinho na rua. Por isso cada alerta explica o sensor, o valor previsto, o método e o limite estourado. Assim a pessoa responsável consegue discordar do sistema quando tiver motivo.

## Uso de IA

A equipe usou o Claude como apoio na construção do projeto. Ele nos ajudou a revisar os conteúdos aprendidos em aula, de modo a facilitar a construção do repositório como um todo. Também nos ajudou na construção dessa documentação, juntando todas as informações do repositório.

A revisão foi feita pela equipe: cada integrante leu as classes da própria parte, além de validar com os testes unitários e testes de uso.

## Divisão de tarefas

| Integrante | Contribuição                                                           |
| ---------- | ---------------------------------------------------------------------- |
| Bruno      | Configuração do projeto, domínio, exceções, menu, bônus e documentação |
| João       | Persistência                                                           |
| Letícia    | Previsões, alertas e testes                                            |
