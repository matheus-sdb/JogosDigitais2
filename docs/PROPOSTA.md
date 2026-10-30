# TrioTiles: proposta do projeto

> Jogo sério de reabilitação da mão e dos dedos, no estilo *Piano Tiles*, controlado por um teclado USB de 3 teclas + 1 knob, desenvolvido em libGDX.
> O personagem principal se chama **Pipo**.
> **Status:** dispositivo aprovado pelo professor, recebido, configurado e testado em 22/10/2026: os 4 testes passaram ([resultado](TESTE_APARELHO.md#4-registrar-o-resultado)).

---

## 1. A ideia em uma frase

Notas caem em **3 trilhas**. O paciente aperta a tecla da trilha certa no momento certo, usando **um dedo para cada tecla**. O **knob** serve para navegar nos menus e, nas fases mais difíceis, vira uma mecânica do jogo ("notas de giro"). Cada toque gera dados (tempo de reação, acerto, tempo segurando a tecla) que viram a pontuação **e** o relatório de evolução do paciente.

---

## 2. O dispositivo

| Item | Detalhe |
|---|---|
| Aparelho | Mini teclado USB de 3 teclas mecânicas + 1 knob (encoder rotativo com clique), RGB |
| Conexão | USB-C, reconhecido pelo computador como **teclado comum (HID)** |
| Programação | O software do fabricante permite escolher qual tecla cada botão envia |
| Entradas disponíveis | `Tecla 1`, `Tecla 2`, `Tecla 3`, `Knob ←`, `Knob →`, `Knob clique` = **6 ações** |

### Mapeamento sugerido

| Botão físico | Tecla enviada (configurar no software do aparelho) | Ação no jogo |
|---|---|---|
| Tecla 1 | `A` (ou `F13`) | Trilha esquerda |
| Tecla 2 | `S` (ou `F14`) | Trilha do meio |
| Tecla 3 | `D` (ou `F15`) | Trilha direita |
| Knob girar ← | `↑` | Menu: sobe / Jogo: nota de giro anti-horário |
| Knob girar → | `↓` | Menu: desce / Jogo: nota de giro horário |
| Knob clique | `Enter` | Menu: confirma / Jogo: **pausa** |

Com isso o jogo inteiro (menu, pausa, reinício e gameplay) funciona **só com o aparelho**, sem mouse ou teclado, como o requisito exige.

> **Por que `F13`–`F15`?** Nenhum teclado comum tem essas teclas, então dá para garantir que o comando veio do aparelho e não do teclado do PC. Só vale se o software do aparelho permitir. Se não permitir, usamos `A`/`S`/`D`.

### Pontos a testar no aparelho antes de programar (Sprint 0)

1. **Segurar a tecla mantém ela pressionada?** Alguns teclados macro baratos mandam "apertou + soltou" na hora, mesmo se o dedo continuar em cima. Se isso acontecer, **não teremos notas longas** (seção 5.4) e trocamos por outra mecânica.
2. **Duas teclas ao mesmo tempo funcionam?** (necessário para os "acordes" da Fase 3)
3. **O knob envia teclas normais** ou só volume/mídia? Teclas de mídia o libGDX não lê, então é preciso reprogramar.
4. **O software do fabricante roda no nosso sistema?** Geralmente é só para Windows. Basta configurar uma vez, a configuração fica salva no aparelho.

### É "IoT"?

✅ **Aprovado pelo professor.** O enunciado aceita "outros dispositivos de IoT, a definir com o professor, mediante autorização prévia". Argumentos usados (bons para o short paper):

- Ele é o **sensor** do exercício: mede *qual* dedo, *quando* apertou, *quanto tempo* segurou e *quanto* girou.
- O controle é feito **exclusivamente** por ele.
- Para reforçar o lado "IoT" (opcional, seção 8, Sprint 6): o jogo envia o resumo de cada sessão para um servidor/planilha online, e o fisioterapeuta acompanha a evolução à distância.

---

## 3. Problema de reabilitação

**Foco: reabilitação da função motora fina da mão e dos dedos.**

Públicos que se beneficiam:

- Pessoas **pós-AVC** com perda de destreza e de **individuação dos dedos** (dificuldade em mexer um dedo sem mexer os outros).
- Pacientes em recuperação de **cirurgia ou lesão na mão/punho** (fraturas, tendões, túnel do carpo).
- Pessoas com **Parkinson** ou tremores, que precisam treinar ritmo, tempo de reação e controle do movimento.
- Idosos que precisam manter a coordenação e a agilidade das mãos.

O que o exercício trabalha:

| Capacidade | Como o jogo exige |
|---|---|
| Individuação dos dedos | Cada dedo tem a sua tecla; apertar a tecla errada conta como erro |
| Tempo de reação / velocidade | A nota precisa ser apertada quando cruza a linha de acerto |
| Ritmo e consistência | Sequências em ritmo constante; medimos a variação do tempo |
| Força e resistência do dedo | Notas longas: segurar a tecla até o fim |
| Coordenação entre dedos | Acordes: duas teclas ao mesmo tempo |
| Rotação fina (pinça + punho) | Notas de giro: girar o knob na direção indicada |

> O jogo é uma **ferramenta de apoio** ao tratamento, para ser usada com orientação de fisioterapeuta ou terapeuta ocupacional. Não substitui a avaliação profissional.

---

## 4. Justificativa: por que a gamificação ajuda

1. **Repetição sem tédio.** A recuperação motora depende de *muitas* repetições do mesmo movimento. Em formato de jogo musical, centenas de toques viram "uma música", e o paciente nem conta.
2. **Feedback imediato.** Cada toque mostra na hora "Perfeito / Bom / Ok / Errou". O paciente corrige o movimento na própria sessão.
3. **Progressão gradual.** As 3 fases aumentam velocidade e complexidade aos poucos, como uma progressão de carga em fisioterapia.
4. **Motivação e adesão.** Pontuação, combo, recordes e o personagem evoluindo incentivam o paciente a voltar, inclusive em casa.
5. **Dados objetivos para o terapeuta.** Em vez de "acho que melhorou", o jogo mostra números: precisão por dedo, tempo de reação médio, consistência, sessão a sessão.
6. **Base na literatura.** Existe uma linha de pesquisa chamada *Music-Supported Therapy* (terapia apoiada por música), em que pacientes pós-AVC treinam tocando teclados e pads, com bons resultados na motricidade fina. Vale citar no short paper (procurar: *Schneider et al., 2007, "Using musical instruments to improve motor skill recovery following a stroke"*, e trabalhos seguintes sobre MST).

---

## 5. Descrição do jogo

### 5.1 Tela de jogo

```
 ┌───────────────────────────────┐
 │ Pontos: 1250   Combo x8  ⏸    │
 │   │  ███  │       │       │   │   ← notas caindo
 │   │       │  ███  │       │   │
 │   │       │  ███  │  ███  │   │   ← nota longa (segurar)
 │   │  ███  │       │       │   │
 │ ══╪═══════╪═══════╪═══════╪══ │   ← linha de acerto
 │   [ 1 ]   [ 2 ]   [ 3 ]       │   ← acende quando a tecla é apertada
 │          🙂 Pipo              │   ← personagem reage aos acertos
 └───────────────────────────────┘
```

### 5.2 Personagem: Pipo

Pipo é um pequeno músico que **toca conforme o jogador acerta**:

- Acerto → Pipo toca a nota e se anima; combo alto → ele dança.
- Erro → Pipo tropeça ou faz careta (leve, sem punir demais).
- **Narrativa das fases:** Pipo começa tocando no quarto, depois vai para a praça e termina no grande teatro. A "carreira" de Pipo acompanha a evolução do paciente.

### 5.3 Menus (controlados pelo knob)

- **Menu principal:** Jogar · Progresso · Configurações · Sair
- **Seleção de fase:** Fase 1 / 2 / 3 (a fase seguinte libera ao atingir a meta da anterior)
- **Pausa** (clique do knob durante o jogo): Continuar · Reiniciar fase · Voltar ao menu
- **Fim de fase:** pontuação + resumo das métricas + Repetir / Próxima / Menu
- **Configurações:** nome do paciente, mão usada (direita/esquerda, inverte as trilhas), velocidade base, tamanho da janela de acerto, volume

### 5.4 As 3 fases (dificuldade progressiva)

| | Fase 1: Aquecimento (Quarto) | Fase 2: Ritmo (Praça) | Fase 3: Destreza (Teatro) |
|---|---|---|---|
| Objetivo terapêutico | Ativar cada dedo, reação simples | Ritmo constante, resistência | Coordenação e controle fino |
| Velocidade | Lenta | Média | Rápida |
| Tipos de nota | Toque simples, uma por vez | Toque + **nota longa** (segurar) | Toque + longa + **acorde** (2 teclas) + **giro do knob** |
| Janela de acerto | Larga (tolerante) | Média | Mais estreita |
| Duração | ~1 min | ~1,5 min | ~2 min |
| Meta para avançar | ≥ 70% de precisão | ≥ 75% de precisão | Concluir |

Os números são iniciais e serão ajustados nos testes.

### 5.5 Pontuação e métricas (o requisito de "velocidade e consistência")

Para cada nota o jogo mede o **erro de tempo** (`Δt` = momento do toque − momento ideal):

| Avaliação | Δt (valor inicial, ajustável) | Pontos |
|---|---|---|
| Perfeito | ≤ 80 ms | 100 |
| Bom | ≤ 160 ms | 70 |
| Ok | ≤ 250 ms | 40 |
| Errou | fora da janela, tecla errada ou não apertou | 0 (zera o combo) |

Pontos × multiplicador de combo = pontuação da fase.

**Métricas salvas por sessão** (estas são a avaliação de reabilitação):

| Métrica | Como calcular | O que indica |
|---|---|---|
| **Precisão** | acertos ÷ total de notas | Controle geral |
| **Velocidade** | média de \|Δt\| (ms) | Tempo de reação |
| **Consistência** | desvio padrão de Δt (ms); quanto menor, melhor | Regularidade do movimento |
| **Precisão por dedo** | precisão separada por trilha 1, 2 e 3 | Qual dedo está mais fraco |
| **Sustentação** | % do tempo que as notas longas foram seguradas | Força/resistência do dedo |
| **Toques errados** | teclas apertadas sem nota na trilha | Individuação (dedo "vizinho" escapando) |

Esses dados são gravados num arquivo local (JSON) com data e hora. A tela **Progresso** mostra um gráfico simples da evolução sessão a sessão, que serve como métrica de melhora do paciente pedida no enunciado.

---

## 6. Requisitos do enunciado × nossa solução

| Requisito | Como atendemos |
|---|---|
| Desenvolvido em libGDX | Projeto Java com libGDX, gerado pelo gdx-liftoff |
| Integração com dispositivo IoT | Teclado de 3 teclas + knob como sensor do exercício (aprovado pelo professor), com envio opcional dos dados para a nuvem |
| Controle exclusivo pelo dispositivo | Gameplay e menus funcionam só com as 3 teclas + knob |
| Ações do jogo influenciadas pelo uso do dispositivo | Cada nota depende de qual dedo aperta, quando e por quanto tempo |
| Mínimo 3 fases com dificuldade progressiva | Aquecimento → Ritmo → Destreza |
| Personagem principal | Pipo, que reage ao desempenho e "viaja" entre as fases |
| Menu: iniciar, pausar, reiniciar | Menu principal + menu de pausa com Reiniciar |
| Velocidade e consistência viram pontuação | Δt médio (velocidade) e desvio padrão de Δt (consistência) |
| Métrica de evolução do paciente | Histórico de sessões + tela de Progresso |
| Executável | `.jar` / `.exe` para desktop |
| Short paper (até 6 páginas) | Seções 3, 4 e 5 deste documento já são o rascunho |
| Commits semanais + Kanban | Roadmap abaixo organizado em sprints semanais |

---

## 7. Arquitetura técnica (resumo)

- **Linguagem:** Java 17
- **Plataforma:** Desktop (LWJGL3). O aparelho é USB, então desktop é o mais simples. Android (APK) fica como extra, via cabo OTG.
- **Projeto:** Gradle, no mesmo formato do [gdx-liftoff](https://github.com/libgdx/gdx-liftoff), libGDX 1.14.2
- **Bibliotecas:** libGDX core + Scene2D (menus) + FreeType (fontes). Sem dependências exóticas.

Organização sugerida:

```
core/
  TrioTilesGame.java          ← classe principal, troca de telas
  input/
    DeviceInput.java          ← traduz teclas do aparelho em ações (TRILHA_1..3, KNOB_ESQ/DIR/CLIQUE)
    DeviceMapping.java        ← qual tecla = qual ação (configurável)
  screens/
    MenuScreen, LevelSelectScreen, GameScreen, PauseOverlay,
    ResultScreen, ProgressScreen, SettingsScreen
  game/
    Note.java (TAP, HOLD, CHORD, SPIN)
    Lane.java
    LevelData.java            ← definição das fases (velocidade, padrões, janela)
    NoteSpawner.java
    Judge.java                ← calcula Δt e Perfeito/Bom/Ok/Errou
    Pipo.java                 ← personagem e animações
  metrics/
    SessionStats.java         ← coleta Δt, acertos, por dedo...
    SessionRepository.java    ← salva/carrega histórico em JSON
assets/
  sprites, fundos das fases, sons das notas, música
```

A camada `DeviceInput` é importante: o resto do jogo nunca sabe que tecla foi apertada, só recebe "trilha 2 pressionada". Assim, se o mapeamento do aparelho mudar, só um arquivo muda.

---

## 8. Roadmap

Cada sprint ≈ 1 semana, para casar com os commits semanais e a reunião com o professor. Cada membro do grupo deve fazer commit toda semana; distribuam os itens no Kanban (colunas: **A fazer · Fazendo · Revisão · Feito**).

### Sprint 0: Preparação
- [x] Mostrar a proposta ao professor e **pedir aprovação do dispositivo**
- [x] Tela "Teste do Aparelho" no jogo e guia [TESTE_APARELHO.md](TESTE_APARELHO.md)
- [x] Receber o aparelho e fazer os 4 testes da seção 2: todos OK (segura a tecla, 3 teclas juntas, knob normal)
- [x] Configurar as teclas no software do aparelho (A/S/D, knob ↑/Enter/↓)
- [x] Gerar o projeto libGDX e subir no GitHub (com CI que gera o `.jar` a cada push)
- [x] Criar o quadro Kanban (GitHub Projects) com este roadmap, no repositório da entrega

### Sprint 1: Núcleo do jogo
- [x] `DeviceInput`: ler as 6 ações do aparelho (adiantado na Sprint 0)
- [x] Desenhar 3 trilhas e a linha de acerto (`GameScreen`)
- [x] Notas simples caindo em velocidade fixa (`Chart`: 40 notas, ~70 por minuto, sem 3 seguidas na mesma trilha)
- [x] Apertar a tecla certa faz a nota sumir (acerto) ou contar erro (`Playfield`: janela de ±250 ms; conta perdidas e "tecla errada")
- [x] Extra: pausa pelo clique do knob e tela de fim com acertos, perdidas e teclas erradas
- **Entrega:** ✅ fase crua jogável com o aparelho (testada em 22/10/2026: 29 de 40 acertos na primeira partida)

![Fim da fase na Sprint 1](img/jogo-sprint1.png)

### Sprint 2: Pontuação e métricas
- [x] `Judge`: Δt e Perfeito/Bom/Ok/Errou (80 / 160 / 250 ms; janelas configuráveis para a Sprint 5)
- [x] Pontos, combo, HUD (`Score`: multiplicador sobe a cada 10 acertos seguidos, até 4x; nota perdida ou tecla errada zera o combo)
- [x] `SessionStats`: precisão, velocidade (média de \|Δt\|), consistência (desvio padrão de Δt), tendência (adianta/atrasa), precisão e teclas erradas por dedo
- [x] Tela de resultado ao fim da fase (`ResultScreen`): métricas, barras por dedo, aviso do "dedo para treinar mais" e opções Jogar de novo / Menu pelo knob
- **Entrega:** ✅ testada em 28/10/2026

![Resultado da fase na Sprint 2](img/resultado-sprint2.png)

> **Observação para os testes:** na primeira partida a tendência foi "atrasa 97 ms". Parte disso pode ser atraso do monitor e do sistema, não do paciente. Se continuar aparecendo com outras pessoas, vale incluir nas Configurações (Sprint 5) um ajuste de atraso (calibração).

### Sprint 3: Menus e personagem
- [ ] Menu principal, pausa (iniciar / pausar / reiniciar) navegando pelo knob
- [ ] Pipo com animações básicas (parado, acerto, erro, dançando)
- [ ] Sons de acerto/erro

### Sprint 4: As 3 fases
- [ ] `LevelData` com as 3 fases e dificuldade progressiva
- [ ] Notas longas (HOLD); o aparelho segura a tecla, teste aprovado
- [ ] Acordes (até 3 teclas juntas, teste aprovado) e notas de giro do knob (Fase 3)
- [ ] Fundos de cada fase (quarto, praça, teatro) e desbloqueio por meta

### Sprint 5: Evolução do paciente
- [ ] Salvar histórico de sessões em JSON
- [ ] Tela de Progresso com gráfico simples
- [ ] Configurações (nome, mão, velocidade, janela de acerto)

### Sprint 6: Polimento e "IoT" (opcional)
- [ ] Testes com colegas, ajuste de velocidades e janelas
- [ ] (Opcional) enviar resumo da sessão para um servidor/planilha online
- [ ] (Opcional) dificuldade adaptativa: o jogo sugere a velocidade da próxima sessão
- [ ] Gerar executável

### Sprint 7: Entrega
- [ ] Prints de tela de todas as fases e telas
- [ ] Short paper (até 6 páginas) a partir das seções 3, 4 e 5
- [ ] Revisão final do código e README com instruções de uso

---

## 9. Riscos e perguntas para o professor

| Risco / dúvida | Plano |
|---|---|
| Aparelho não segura a tecla (envia apertar+soltar na hora) | Trocar notas longas por "toques rápidos repetidos" (resistência por repetição) |
| Knob envia só teclas de mídia | Reprogramar no software; se impossível, usar só para o menu com outra combinação |
| Software do aparelho só roda no Windows | Configurar uma vez num PC Windows (fica salvo no aparelho) |
| Exige APK? | Perguntar; o padrão será executável desktop |

---

## 10. Próximo passo

Aprovada a ideia (e o aparelho), começamos pela **Sprint 0**: gerar o projeto libGDX, subir no GitHub e testar o aparelho.
