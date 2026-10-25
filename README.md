# TrioTiles

Jogo sério de reabilitação da mão e dos dedos, no estilo *Piano Tiles*, feito em **libGDX** e controlado por um teclado USB de **3 teclas + 1 knob**.

- Proposta, encaixe na reabilitação e roadmap: [docs/PROPOSTA.md](docs/PROPOSTA.md)
- Guia para testar o aparelho: [docs/TESTE_APARELHO.md](docs/TESTE_APARELHO.md)

**Status:** Sprint 0 quase concluída: projeto pronto e aparelho configurado e testado (4/4 OK). Falta só o quadro Kanban. Próximo: Sprint 1, o núcleo do jogo.

> **Windows:** clone o projeto numa pasta **sem acento** no caminho (ex.: `C:\dev\TrioTiles`). Com acento (ex.: `Temporário`), o Gradle não encontra as classes e `gradlew test` falha.

![Menu](docs/img/menu.png)

## Como rodar

Precisa do **JDK 17 ou mais novo** (ex.: [Temurin](https://adoptium.net/)). O Gradle é baixado sozinho na primeira vez.

```bash
# Windows
gradlew.bat lwjgl3:run

# Linux / macOS
./gradlew lwjgl3:run
```

No IntelliJ IDEA: *File → Open* na pasta do projeto e rode a tarefa Gradle `lwjgl3 > application > run`.

### Gerar o executável (.jar)

```bash
./gradlew lwjgl3:jar
java -jar lwjgl3/build/libs/TrioTiles-0.1.0.jar
```

Cada push no GitHub também gera o `.jar` automaticamente: aba **Actions** → execução mais recente → artefato **TrioTiles-jar**.

### Rodar os testes

```bash
./gradlew test
```

## Controles

O jogo é controlado só pelo aparelho. Enquanto ele não está disponível, o teclado do PC simula:

| Aparelho | Ação | Teclado do PC |
|---|---|---|
| Tecla 1 / 2 / 3 | Trilhas esquerda / meio / direita | `A` / `S` / `D` (ou `F13`/`F14`/`F15`) |
| Knob girar ← / → | Navegar nos menus · na pausa, voltar ao menu | `↑` / `↓` |
| Knob clique | Confirmar · pausar e continuar o jogo | `Enter` |

## Estrutura

```
core/     lógica do jogo (independente de plataforma)
  input/    leitura do aparelho: DeviceInput, DeviceMapping, DeviceAction
  game/     regras, sem desenho: Note, Chart (sequência de notas), Playfield (acerto/erro)
  screens/  telas: MenuScreen, GameScreen, DeviceTestScreen
lwjgl3/   versão desktop (Lwjgl3Launcher)
assets/   imagens, sons e fontes
docs/     proposta, guias e prints
```

O resto do jogo nunca lê teclas diretamente: só recebe ações como "Tecla 2 pressionada" da classe `DeviceInput`. Se o mapeamento do aparelho mudar, só `DeviceMapping` muda.
