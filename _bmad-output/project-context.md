---
project_name: 'bingo-ai'
user_name: 'Jeyson'
date: '2026-09-26'
sections_completed: ['technology_stack', 'domain_models']
existing_patterns_found: 13
---

# Project Context for AI Agents

_This file contains critical rules and patterns that AI agents must follow when implementing code in this project. Focus on unobvious details that agents might otherwise miss._

---

## Technology Stack & Versions

### Frontend
- Vue 3
- Vue CLI / Vite (as per current configuration)
- Vuetify
- Axios
- Firebase

### Backend
- Java
- Spring Boot
- Maven
- REST API
- HATEOAS

### Database
- PostgreSQL
- Docker (currently used for local database hosting)

### Intended Infrastructure
- Firebase Hosting
- Google Cloud Run
- Google Cloud SQL
- Docker

## Domain Entities & Business Rules

The system involves the following core domain models and concepts that must be respected:
- **Admin**: System administrators.
- **Player**: Participants of the bingo game.
- **Bingo**: The main game entity.
- **Round**: Specific rounds within a bingo game.
- **Drawn Numbers**: The numbers drawn during a round.
- **Card Pack**: A collection of cards purchased or assigned to players.
- **Cards**: The individual bingo cards used by players.

All established business rules regarding these entities must be strictly followed and preserved in any new implementation.
