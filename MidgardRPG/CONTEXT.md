# PROJETO: MIDGARD RPG SYSTEM

## 1. Visão Geral da Arquitetura
O projeto é um sistema RPG modular para Minecraft (Spigot/Paper) dividido em um núcleo (`midgard-core`) e módulos funcionais.
- **Linguagem:** Java (JDK 17+)
- **Build System:** Gradle
- **Framework:** Bukkit/Spigot API

## 2. Estrutura de Diretórios e Módulos
Raiz:
├── midgard-core/           (API Base, Gerenciadores, Database, Redis, Utils)
├── midgard-character/      (Stats, Leveling, Profiles)
├── midgard-combat/         (Sistema de Dano Customizado, Scaling, Skills)
├── midgard-item/           (Gerador de Itens, Lore, NBT/PDC)
├── midgard-spells/         (Sistema de Magias, Árvore de Skills)
├── midgard-mythicmobs/     (Integração e Mecânicas customizadas para MM)
└── midgard-essentials/     (Homes, Warps, Teleports)

## 3. Padrões de Código (Code Style)

### Core & Singleton
- A classe principal é `MidgardCore` (`me.ray.midgard.core`).
- Use `MidgardCore.getInstance()` para acessar gerenciadores globais.
- **Managers Principais:** `ModuleManager`, `ProfileManager`, `DatabaseManager`, `RedisManager`.

### Criação de Módulos
- Todo novo módulo deve ser um subprojeto Gradle.
- A classe principal do módulo deve estender `RPGModule` (não `JavaPlugin` diretamente).
- Pacote padrão: `me.ray.midgard.modules.<nome_do_modulo>`.

### Persistência de Dados
- **SQL:** Use `DatabaseManager` para dados persistentes críticos.
- **Redis:** Use `RedisManager` para cache e dados voláteis/cross-server.
- **PDC (PersistentDataContainer):** Use `PDCUtils` ou `ItemPDC` para armazenar dados em itens e entidades.

### Comandos e GUI
- **Comandos:** Registre via `CommandManager` ou `BukkitCommandWrapper`.
- **Comandos Admin:** Registre via `MidgardCore.getAdminCommand().registerSubcommand(command)`.
  - Acessíveis via `/rpg admin <subcomando>` ou `/midgard admin <subcomando>`.
  - Subcomandos disponíveis: reload, reset, stats, scan, item, class, dummy, performance.
- **GUIs:** Estenda `BaseGui` ou `PaginatedGui` (`me.ray.midgard.core.gui`). Não use InventoryHolder cru.

### Integrações (Hooks)
O projeto possui wrappers para plugins externos. Use-os em vez de chamar a API direta:
- `MythicMobsIntegration`
- `WorldGuardIntegration`
- `VaultIntegration`
- `ItemsAdderUtils`

## 4. Regras de Ouro para a IA
1. **Não alucine imports:** Verifique se a classe existe em `midgard-core` antes de sugeri-la.
2. **Modularidade:** Se estou trabalhando no `midgard-combat`, não sugira código que pertença ao `midgard-item` a menos que haja uma dependência explícita.
3. **Logs:** Use `MidgardLogger` em vez de `System.out.println` ou `Bukkit.getLogger()`.
4. **Tratamento de Erros:** Exceções de banco de dados devem ser logadas, mas não devem crashar o servidor (try/catch seguro).

---