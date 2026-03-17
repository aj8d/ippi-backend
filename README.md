作業効率化Webアプリ「ippi」のバックエンドリポジトリです。<br>
詳細な情報はこちらを参照してください:https://github.com/aj8d/ippi-frontend

ローカル起動は backend 直下で以下を使えます。

Windows Command Prompt:
run-local.cmd

PowerShell:
.\run-local.ps1

bash (Git Bash / WSL):
./run-local.sh

上記はどちらも local プロファイルで以下を実行します。
mvn spring-boot:run -Dspring-boot.run.profiles=local
