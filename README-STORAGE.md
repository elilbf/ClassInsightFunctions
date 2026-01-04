# Configuração de Storage Local para Azure Functions

## Passo 1: Instalar Azurite (Storage Emulator)

### Opção A: Com npm/Node.js
```bash
npm install -g azurite
```

### Opção B: Com Docker
```bash
docker run -p 10001:10001 -p 10002:10002 mcr.microsoft.com/azure-storage/azurite
```

### Opção C: Azure Storage Emulator (Windows)
- Baixe e instale Microsoft Azure Storage Emulator
- Inicie como Administrador

## Passo 2: Iniciar o Storage

### Com Azurite:
```bash
azurite --silent --location ./azure-storage --debug ./azure-storage/debug
```

### Com Docker:
```bash
docker run -d -p 10001:10001 -p 10002:10002 mcr.microsoft.com/azure-storage/azurite
```

## Passo 3: Executar a Aplicação

```bash
mvn azure-functions:run
```

## Configuração Atual

local.settings.json está configurado com:
```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "UseDevelopmentStorage=true",
    "FUNCTIONS_WORKER_RUNTIME": "java"
  }
}
```

## Testes

- HTTP Function: `POST http://localhost:7071/api/avaliacao`
- Timer Function: Executa a cada 1 minuto (verifique os logs)

## Solução de Problemas

Se ainda der erro de storage:
1. Verifique se Azurite está rodando
2. Confirme as portas 10001/10002 estão livres
3. Use connection string real se tiver conta Azure
