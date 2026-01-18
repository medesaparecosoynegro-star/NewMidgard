# Plano de Desenvolvimento: MidgardProxy (Velocity)

Este plano foca na implementação do plugin **MidgardProxy** para a plataforma Velocity, que atuará como o controlador central da rede MidgardRPG.

## 1. Estrutura do Projeto

O módulo `midgard-proxy` já foi criado no Gradle. A estrutura interna de pacotes será:

```
me.ray.midgard.proxy
├── command/           # Comandos (/lobby, /g, /party)
├── config/            # Gerenciamento de config.toml
├── listener/          # Listeners de eventos (Login, Switch, Chat)
├── manager/           # Lógica de negócio (Party, Queue, Server)
├── redis/             # (Já existente) Comunicação Pub/Sub
└── util/              # Utilitários (Componentes de texto, Serialização)
```

## 2. Funcionalidades Core a Implementar

### 2.1. Sistema de Configuração (TOML)
Implementar carregamento de `config.toml` para evitar valores hardcoded.
*   **Campos**: Credenciais Redis, Servidores de Lobby, Mensagens.
*   **Biblioteca**: Usar Configurate (nativo do Velocity) ou TOML4J.

### 2.2. Gerenciamento de Conexão e Roteamento
*   **Comando `/lobby`**: Envia o jogador para o servidor de lobby configurado.
*   **Fallback**: Redirecionar para o Lobby caso o servidor de destino esteja offline.
*   **Load Balancing** (Opcional): Distribuir jogadores entre múltiplos lobbies (lobby-1, lobby-2).

### 2.3. Sistema de Chat Global
*   **Listener de Chat**: Interceptar mensagens no Velocity.
*   **Comando `/g <msg>`**: Enviar mensagens para toda a rede via Redis Pub/Sub.
*   **Formatação**: Usar MiniMessage para suportar cores e gradientes.

### 2.4. Segurança de Sessão (Profile Locking)
Integrar com o sistema de travamento do `midgard-core`.
*   **Evento `ServerPreConnectEvent`**:
    1.  Interceptar a tentativa de troca de servidor.
    2.  Verificar se o jogador tem dados pendentes de salvamento (via Redis).
    3.  Aguardar confirmação (`sync:saved:<uuid>`) antes de permitir a conexão.

## 3. Protocolo de Comunicação (Redis)

Definição dos canais para comunicação entre Proxy e Spigot (Core):

| Canal | Direção | Mensagem | Ação |
| :--- | :--- | :--- | :--- |
| `midgard:global_chat` | Bidirecional | JSON `{sender, msg}` | Exibir mensagem no chat |
| `midgard:server_switch` | Proxy -> Spigot | UUID | Notificar que jogador vai sair |
| `sync:saved:<uuid>` | Spigot -> Proxy | "saved" | Confirmar que dados foram persistidos |

## 4. Roteiro de Implementação

### Passo 1: Configuração e Base
- [ ] Implementar classe `ConfigManager` para ler `config.toml`.
- [ ] Atualizar `MidgardProxy` para usar as credenciais do config.

### Passo 2: Comandos Básicos
- [ ] Implementar `LobbyCommand` (/lobby, /hub).
- [ ] Implementar `ReloadCommand` (/midgardproxy reload).

### Passo 3: Chat Global
- [ ] Implementar `GlobalChatCommand` (/g).
- [ ] Configurar Pub/Sub no Redis para distribuir mensagens.

### Passo 4: Integração de Segurança
- [ ] Implementar listener `ServerPreConnectEvent`.
- [ ] Adicionar lógica de espera (Wait) baseada no Redis Lock.

**Status**: O módulo Gradle `midgard-proxy` está pronto. O próximo passo é implementar o código Java seguindo este roteiro.