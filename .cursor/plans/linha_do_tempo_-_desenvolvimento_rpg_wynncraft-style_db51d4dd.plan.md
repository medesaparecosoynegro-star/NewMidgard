---
name: Linha do Tempo - Desenvolvimento RPG Wynncraft-Style
overview: Plano de desenvolvimento progressivo do MidgardRPG comparado ao Wynncraft, organizado do básico ao avançado, identificando o que já temos e o que falta implementar.
todos: []
---

# Plano de Desenvolvimento: MidgardRPG - Básico ao Avançado (Estilo Wynncraft)

## Estado Atual vs Wynncraft - Linha do Tempo

### FASE 1: FUNDAÇÃO ✅ (CONCLUÍDO)

**Status**: 100% Implementado

**O que temos:**

- ✅ Sistema de Perfis (Database + Redis)
- ✅ Sistema de Atributos (AttributeRegistry com modificadores)
- ✅ Sistema de Classes (3 classes base: Guerreiro, Mago, Arqueiro)
- ✅ Sistema de Nível/XP (PlayerLevelUpEvent, pontos distribuíveis)
- ✅ Sistema de Combate Base (dano físico/mágico/elemental, críticos, mitigação)
- ✅ Sistema de Itens Customizados (stats, tiers, sockets básicos)
- ✅ Framework de GUIs (BaseGui, PaginatedGui)
- ✅ Sistema de Mensagens i18n (MiniMessage)
- ✅ Multiservidores (Proxy + Redis sync)

**Arquivos relevantes:**

- [`MidgardRPG/midgard-core/src/main/java/me/ray/midgard/core/profile/ProfileManager.java`](MidgardRPG/midgard-core/src/main/java/me/ray/midgard/core/profile/ProfileManager.java) - Gerenciamento de perfis
- [`MidgardRPG/midgard-modules/midgard-classes/src/main/java/me/ray/midgard/modules/classes/ClassesModule.java`](MidgardRPG/midgard-modules/midgard-classes/src/main/java/me/ray/midgard/modules/classes/ClassesModule.java) - Sistema de classes
- [`MidgardRPG/midgard-modules/midgard-combat/src/main/java/me/ray/midgard/modules/combat/CombatModule.java`](MidgardRPG/midgard-modules/midgard-combat/src/main/java/me/ray/midgard/modules/combat/CombatModule.java) - Sistema de combate

---

### FASE 2: ITENS E EQUIPAMENTOS ⚠️ (50% IMPLEMENTADO)

**Status**: Base implementada, faltam mecânicas avançadas

**O que temos:**

- ✅ Geração de itens com stats aleatórios
- ✅ Sistema de sockets para gems
- ✅ Tiers básicos de itens
- ✅ Equipamento aplica atributos automaticamente
- ✅ Durabilidade customizada

**O que falta:**

- ❌ **Sistema de Identificação** (itens dropam "não identificados", precisam de scroll)
- ❌ **Sistema de Reforge** (melhorar stats de itens existentes com materiais)
- ❌ **Sistema de Item Sets** (bônus quando equipa múltiplos itens do mesmo set)
- ❌ **Item Quality/Variants** (Normal, Unique, Rare, Legendary com multiplicadores)
- ❌ **Sistema de Salvamento de Builds** (presets de equipamentos/atributos)

**Prioridade**: ALTA - Essencial para endgame e economia

---

### FASE 3: PROGRESSÃO E CONTEÚDO 🟡 (30% IMPLEMENTADO)

**Status**: XP e levelup existe, mas falta sistema de quests/dungeons

**O que temos:**

- ✅ Sistema de XP e levelup
- ✅ Pontos de atributos distribuíveis
- ✅ Skills desbloqueiam por nível (via classes)

**O que falta:**

- ❌ **Sistema de Quests** (NPCs com missões, progressão por objetivos)
- ❌ **Sistema de Dungeons** (instâncias privadas, bosses, loot tables)
- ❌ **Sistema de Raids** (dungeons para party, cooperação)
- ❌ **Sistema de Daily/Weekly Quests** (missões recorrentes)
- ❌ **Sistema de Progression Gates** (bloqueios por nível em áreas)

**Prioridade**: ALTA - Conteúdo é essencial para retenção

---

### FASE 4: SOCIAL E MULTIPLAYER 🟢 (10% IMPLEMENTADO)

**Status**: Multiservidores funciona, mas falta party/guild

**O que temos:**

- ✅ Chat global cross-server
- ✅ Sincronização de perfis entre servidores

**O que falta:**

- ❌ **Sistema de Party** (grupos temporários, compartilhar XP, teleport)
- ❌ **Sistema de Guild** (organizações permanentes, hierarquia, guild bank, guild quests)
- ❌ **Sistema de Friends** (lista de amigos, mensagens diretas)
- ❌ **Sistema de Chat Channels** (local, party, guild, global)
- ❌ **Sistema de Trade** (trading seguro entre jogadores)

**Prioridade**: MÉDIA - Melhora socialização mas não crítico

---

### FASE 5: CRAFTING E ECONOMIA 🔴 (0% IMPLEMENTADO)

**Status**: Não implementado

**O que falta:**

- ❌ **Sistema de Crafting** (profissões: Blacksmithing, Alchemy, Cooking, etc)
- ❌ **Sistema de Resources Nodes** (minerar, coletar, pescar)
- ❌ **Sistema de Auction House** (venda de itens via GUI)
- ❌ **Sistema de NPC Shops** (lojas com preços dinâmicos)
- ❌ **Sistema de Currency** (moedas múltiplas: ouro, gemas, tokens de dungeon)

**Prioridade**: ALTA - Economia é essencial para engajamento

---

### FASE 6: MUNDO E EXPLORAÇÃO 🟢 (20% IMPLEMENTADO)

**Status**: Essentials tem warps, mas falta sistema de mundo

**O que temos:**

- ✅ Warps e Homes (Essentials)

**O que falta:**

- ❌ **Sistema de Fast Travel** (waypoints desbloqueáveis, custo de teleporte)
- ❌ **Sistema de Discovery** (áreas descobertas, pontos de interesse)
- ❌ **Sistema de Territory/Regions** (regiões com níveis, recursos únicos)
- ❌ **Sistema de World Events** (eventos globais, bosses mundiais)
- ❌ **Sistema de Housing** (casas personalizáveis)

**Prioridade**: BAIXA-MÉDIA - Nice to have, mas não essencial

---

### FASE 7: SISTEMAS AVANÇADOS 🔴 (0% IMPLEMENTADO)

**Status**: Não implementado

**O que falta:**

- ❌ **Sistema de Achievements** (conquistas com recompensas)
- ❌ **Sistema de Leaderboards** (rankings: nível, dano, riqueza, etc)
- ❌ **Sistema de Build Presets** (salvar/carregar distribuições de atributos)
- ❌ **Sistema de Build Calculator** (simulador de stats antes de aplicar pontos)
- ❌ **Sistema de PvP Arenas** (modos de combate PvP balanceado)
- ❌ **Sistema de Season Pass/Battle Pass** (progressão sazonal com recompensas)

**Prioridade**: BAIXA - Melhorias de qualidade de vida

---

## Roadmap Recomendado por Prioridade

### CURTO PRAZO (Próximas 2-4 semanas)

#### 1. Sistema de Identificação de Itens (Fase 2)

**Objetivo**: Itens dropam "não identificados", jogador usa scroll para revelar stats.

**Arquivos a modificar/criar:**

- `midgard-modules/midgard-item/src/main/java/me/ray/midgard/modules/item/model/MidgardItem.java` - Adicionar flag `identified`
- `midgard-modules/midgard-item/src/main/java/me/ray/midgard/modules/item/manager/ItemIdentificationManager.java` - Novo gerenciador
- `midgard-modules/midgard-item/src/main/resources/modules/item/items/scrolls/identification_scroll.yml` - Novo tipo de item

**Dependências**: Nenhuma (usa sistema de itens existente)

---

#### 2. Sistema de Reforge (Fase 2)

**Objetivo**: Jogador pode melhorar stats de itens usando materiais e ouro.

**Arquivos a modificar/criar:**

- `midgard-modules/midgard-item/src/main/java/me/ray/midgard/modules/item/gui/ReforgeGui.java` - Nova GUI
- `midgard-modules/midgard-item/src/main/java/me/ray/midgard/modules/item/manager/ReforgeManager.java` - Novo gerenciador
- `midgard-modules/midgard-item/src/main/resources/modules/item/config.yml` - Adicionar configurações de reforge

**Dependências**: Sistema de economia (Vault já integrado)

---

#### 3. Sistema de Quests Básico (Fase 3)

**Objetivo**: NPCs oferecem missões com objetivos (matar X mobs, coletar Y itens, etc).

**Arquivos a criar:**

- `midgard-modules/midgard-quests/` - Novo módulo
- `midgard-modules/midgard-quests/src/main/java/me/ray/midgard/modules/quests/QuestManager.java`
- `midgard-modules/midgard-quests/src/main/java/me/ray/midgard/modules/quests/model/Quest.java`
- `midgard-modules/midgard-quests/src/main/java/me/ray/midgard/modules/quests/gui/QuestGui.java`
- `midgard-modules/midgard-quests/src/main/resources/modules/quests/quests/` - YAMLs de quests

**Dependências**: FancyNpcs (já integrado), MythicMobs (já integrado)

---

### MÉDIO PRAZO (1-2 meses)

#### 4. Sistema de Crafting (Fase 5)

**Objetivo**: Profissões (Blacksmithing, Alchemy) com receitas e XP.

**Arquivos a criar:**

- `midgard-modules/midgard-crafting/` - Novo módulo
- Integração com sistema de itens existente

#### 5. Sistema de Dungeons (Fase 3)

**Objetivo**: Instâncias privadas com bosses e loot tables.

**Arquivos a criar:**

- `midgard-modules/midgard-dungeons/` - Novo módulo
- Integração com MythicMobs para bosses

#### 6. Sistema de Party (Fase 4)

**Objetivo**: Grupos temporários com compartilhamento de XP.

**Arquivos a criar:**

- `midgard-modules/midgard-party/` - Novo módulo
- Integração com Redis para cross-server

---

### LONGO PRAZO (3-6 meses)

#### 7. Sistema de Guilds (Fase 4)

#### 8. Sistema de Auction House (Fase 5)

#### 9. Sistema de Achievements (Fase 7)

---

## Comparação Visual: MidgardRPG vs Wynncraft

```
WYNNCRAFT COMPLETO:
├── [✅] Atributos e Stats
├── [✅] Classes (5 classes)
├── [✅] Sistema de Combate
├── [✅] Itens Customizados
├── [✅] Identificação de Itens
├── [✅] Reforge System
├── [✅] Quest System (milhares)
├── [✅] Dungeons (100+)
├── [✅] Crafting (5 profissões)
├── [✅] Party System
├── [✅] Guild System
├── [✅] Fast Travel
└── [✅] Auction House

MIDGARDRPG ATUAL:
├── [✅] Atributos e Stats (AttributeRegistry)
├── [✅] Classes (3 classes base)
├── [✅] Sistema de Combate (físico/mágico/elemental)
├── [🟡] Itens Customizados (faltam identificação/reforge)
├── [❌] Identificação de Itens
├── [❌] Reforge System
├── [❌] Quest System
├── [❌] Dungeons
├── [❌] Crafting
├── [❌] Party System
├── [❌] Guild System
├── [🟡] Fast Travel (warps básicos)
└── [❌] Auction House
```

---

## Próximos Passos Imediatos

**Recomendação**: Começar pela **Fase 2 - Sistema de Itens Avançado** (Identificação + Reforge), pois:

1. Usa sistemas já existentes (midgard-item)
2. É essencial para economia e endgame
3. Relativamente rápido de implementar (1-2 semanas)
4. Melhora significativamente a experiência do jogador

**Sequência sugerida:**

1. Sistema de Identificação (1 semana)
2. Sistema de Reforge (1-2 semanas)
3. Sistema de Item Sets (1 semana)
4. Sistema de Quests Básico (2-3 semanas)

---

## Notas Técnicas

### Padrões a Seguir (já estabelecidos)

- Todos os módulos devem estender `RPGModule`
- Dados de módulos via `ModuleData` no `MidgardProfile`
- GUIs via `BaseGui` ou `PaginatedGui`
- Mensagens via `MessageUtils` com chaves `messages.yml`
- Logging via `MidgardLogger`
- Operações DB assíncronas via `DatabaseManager`

### Integrações Disponíveis

- **MythicMobs**: Para mobs e bosses customizados
- **FancyNpcs**: Para NPCs interativos
- **ItemsAdder**: Para custom models de itens
- **WorldGuard**: Para proteção de áreas
- **Vault**: Para economia (já integrado)
- **Redis**: Para dados cross-server (já integrado)