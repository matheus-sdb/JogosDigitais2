# Levar o projeto para outro repositório (entrega semanal)

O projeto foi feito para funcionar **a partir do `.zip` do GitHub**, em qualquer computador e em qualquer repositório, mesmo enviando os arquivos pelo site (*Add files via upload*).

## 1. Baixar

No repositório do grupo: **Code → Download ZIP** e extraia em qualquer pasta. Pode ter acento ou espaço no caminho (ex.: `F:\Temporário\Jogos Digitais`).

## 2. Testar no computador (opcional)

Precisa do **JDK 17 ou mais novo**. Na pasta extraída:

```bash
gradlew.bat lwjgl3:run      # Windows
sh gradlew lwjgl3:run       # Linux / macOS
```

> Se a pasta tiver acento, o Gradle avisa *"Pasta com acento detectada"* e guarda os arquivos de build em `~/.gradle/triotiles-build/`. É normal: o Java no Windows não lida bem com acento nesses arquivos. O `.jar` continua saindo em `lwjgl3/build/libs/`.

## 3. Enviar para o outro repositório

No repositório de destino: **Add file → Upload files** e arraste os arquivos/pastas.

**Primeira vez:** envie **tudo** que veio no `.zip`, inclusive:

| Arquivo | Para quê |
|---|---|
| `.github/workflows/build.yml` | O build automático (aba **Actions**) |
| `.gitattributes` e `.gitignore` | Finais de linha certos e arquivos ignorados |
| `gradlew`, `gradlew.bat`, `gradle/wrapper/` (com o `gradle-wrapper.jar`) | Rodar o Gradle sem instalar nada |

**Semanas seguintes:** envie só os arquivos que mudaram (o GitHub substitui os antigos). Se algum arquivo foi **apagado** no repositório do grupo, apague também no de destino.

**Não envie:** pastas `build/`, `.gradle/`, `.idea/` ou arquivos `.jar` de `lwjgl3/build/libs/`. Elas são geradas pelo computador e não vêm no `.zip`.

Limites do site: até **100 arquivos** e **25 MB por arquivo** em cada envio. Se passar disso, divida em mais de um envio.

## 4. Conferir

Aba **Actions** do repositório de destino → execução mais recente deve ficar verde ✅. O `.jar` do jogo fica em **Artifacts → TrioTiles-jar**.

### Se der erro

| Mensagem | Causa | O que fazer |
|---|---|---|
| `./gradlew: Permission denied` | `build.yml` antigo, de antes da correção | Envie o `.github/workflows/build.yml` atual |
| `Could not find or load main class org.gradle.wrapper.GradleWrapperMain` | Faltou o `gradle/wrapper/gradle-wrapper.jar` | Envie a pasta `gradle/wrapper/` inteira |
| `ClassNotFoundException` nos testes, no Windows | `build.gradle` antigo + pasta com acento | Envie o `build.gradle` atual |
