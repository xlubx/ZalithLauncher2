# COBBLEVERSE APK - patch para o fork do Zalith Launcher 2

Estas pastas tem a MESMA estrutura do repositorio do Zalith. Elas substituem/adicionam 7 arquivos:

| Arquivo | O que muda |
|---|---|
| ZalithLauncher/gradle.properties | nome "COBBLEVERSE Mobile", versao 2.8-cobbleverse1 |
| ZalithLauncher/build.gradle.kts | applicationId proprio (br.cobbleverse.mobile) e assinatura propria |
| .../cobbleverse/CobbleverseBootstrap.kt (NOVO) | le o cobbleverse-pack.json, baixa o pack e importa sozinho |
| .../viewmodel/ModpackImportViewModel.kt | permite nome de versao fixo (sem perguntar) |
| .../context/Contexts.kt | aceita arquivo local no importador |
| .../ui/activities/MainActivity.kt | chama o bootstrap; desliga a checagem de update do Zalith oficial |
| .../ui/screens/splash/WelcomeScreen.kt | aviso "Unofficial Modified Version" (exigido pela licenca) |
| .github/workflows/cobbleverse-build.yml (NOVO) | compila o APK na nuvem |

## Passo a passo (tudo pelo site do GitHub)
1. No SEU fork, entre em cada pasta e use **Add file > Upload files** (ou arraste as pastas deste zip
   para a pagina do fork) e faca **Commit changes**. Arquivos com o mesmo caminho serao substituidos.
2. Abra `CobbleverseBootstrap.kt` (icone de lapis) e troque `SEU-USUARIO` na linha `PACK_JSON_URL`
   pelo seu usuario do GitHub (o do repositorio onde ficara o JSON, passo 4).
3. Em `.github/workflows`, APAGUE `build.yml`, `push_ci.yml` e `release_ci.yml` (sao do projeto original;
   se ficarem, cada commit dispara compilacoes demoradas que podem falhar).
4. Crie um repositorio PUBLICO `cobbleverse-pack`: envie o `cobbleverse-pack.json` (troque SEU-USUARIO dentro dele)
   e crie um Release `v1.7.42` anexando o `COBBLEVERSE-MOBILE-1.7.42-CF.zip`.
5. No fork: Settings > Secrets and variables > Actions > New repository secret:
   - `CURSEFORGE_API_KEY` = chave gratuita de https://console.curseforge.com (necessaria para baixar os mods).
   - (opcional) `KEYSTORE_B64`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` para assinar com chave propria.
     Sem eles o APK sai assinado com a chave de debug do projeto: funciona e atualiza normalmente, mas qualquer
     pessoa conhece essa chave. Para um grupo de amigos costuma bastar.
6. Aba **Actions** > "COBBLEVERSE APK" > **Run workflow**. Em ~20-40 min baixe o APK em **Artifacts**.
7. Instale no celular (permitir fontes desconhecidas), abra e aguarde: ele baixa o pack e instala o COBBLEVERSE.

## Para atualizar o pack depois
Suba o novo zip no Release, edite o `cobbleverse-pack.json` (mude `version`, `pack_zip_url`, `pack_zip_sha1`
e de um novo `version_name`, ex.: "COBBLEVERSE 1.7.43", para nao conflitar com a versao ja instalada).
Na proxima abertura do app, ele baixa e instala. Nao precisa recompilar.

## Avisos
- NAO COMPILEI este patch (meu ambiente nao tem Android SDK nem internet). Se o build falhar, copie o log
  do erro (Actions > execucao > passo que falhou) e me envie.
- Licenca GPL-3.0: o fork deve ser PUBLICO. Mantenha os creditos ao Zalith Launcher e ao PojavLauncher.
  O nome nao pode conter "ZalithLauncher"/"ZL" e a tela inicial deve dizer "Unofficial Modified Version" (ja incluido).
- Icone: o app ainda usa o icone do Zalith. Para trocar, substitua os arquivos em
  ZalithLauncher/src/main/res/mipmap-*/ic_launcher*.webp (e o drawable img_launcher na tela inicial).
- Peca permissao a LUMYVERSE antes de distribuir o pack modificado fora do seu grupo de amigos.
