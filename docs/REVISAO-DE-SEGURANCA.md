# Revisão da distribuição AquaLeve

O Microsoft Defender do usuário detectou o ZIP anterior como `Trojan:Script/Ulthar.A!ml`. Não foi possível identificar o arquivo interno responsável a partir das capturas. O pacote anterior não deve ser restaurado ou liberado por exceção.

## Mudanças desta cópia

- Distribuição apenas dos fontes Java, interface, configuração de exemplo e documentação.
- Removidos o ZIP aninhado e os scripts Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn`). A execução requer Apache Maven 3.9+ instalado a partir da fonte oficial.
- O script Windows anterior foi comparado ao modelo `only-mvnw.cmd` oficial Apache Maven Wrapper 3.3.4 e era idêntico, considerando a versão e normalização de finais de linha. Essa comparação não confirma a causa do alerta nem prova que o alerta seja falso positivo.
- Nenhuma proteção do Windows deve ser desativada. Não criar exclusões no antivírus.
- Regras de negócio, autenticação, armazenamento de fotos e interface preservados.

## Análise disponível

Foi usado ClamAV 1.5.4, obtido da distribuição oficial Cisco Talos. A varredura inicial do ZIP anterior e dos novos fontes encontrou zero arquivos infectados. Foram carregadas as bases daily 28176 e bytecode 339, com 341253 assinaturas efetivas. O download de main.cvd foi recusado pelo servidor com HTTP 429; portanto, a base de assinaturas estava incompleta. Isso limita o resultado.

ClamAV não reproduz os modelos do Microsoft Defender. Nenhum resultado desta análise é uma garantia de ausência de malware ou de futuros alertas. Não foi realizada verificação com Defender neste ambiente Linux.

## Verificação reproduzível no Windows

Em GitHub Actions, a ação manual `Verificar AquaLeve no Windows` tenta atualizar o Microsoft Defender, examinar os fontes e o ZIP e executar `mvn clean verify`. Somente se todas essas etapas terminarem com sucesso será criado um artefato de download. Se o Defender estiver indisponível no runner, a ação falha e não apresenta uma aprovação fictícia.

A execução inicial [https://github.com/mariaestevam/AquaLeve/actions/runs/42440365904](https://github.com/mariaestevam/AquaLeve/actions/runs/42440365904) concluiu com sucesso a atualização e varredura pelo Microsoft Defender (motor 4.18.26100.4, assinaturas 1.463.184.0) e os 35 testes em Java 17 no Windows. O resultado vale para o commit exibido nessa execução. Consulte a execução correspondente à versão baixada; atualizações futuras do antivírus ainda podem alterar o resultado.

O empacotamento força os finais de linha originais dos blobs Git para que os hashes em `relatorios/SHA256SUMS.txt` sejam reproduzíveis também no Windows. O ZIP fornecido em Releases deve corresponder ao artefato examinado, sem recompactação.

Em caso de nova detecção, manter o bloqueio e submeter o arquivo à análise oficial da Microsoft: https://www.microsoft.com/en-us/wdsi/filesubmission . Não enviar credenciais ou configurações particulares.
