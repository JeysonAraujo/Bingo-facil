<template>
  <v-app>
    <v-app-bar color="primary" density="compact">
      <v-app-bar-title class="text-white font-weight-bold">
        bingo-ai
      </v-app-bar-title>
      <v-spacer></v-spacer>
      <v-chip color="secondary" variant="flat" size="small" class="mr-2">
        🔴 Live - Sincronizado
      </v-chip>
    </v-app-bar>

    <v-main class="bg-background">
      <v-container class="py-8">
        <v-row justify="center">
          <v-col cols="12" md="8" class="text-center">
            <v-card class="pa-6 elevation-2 rounded-lg">
              <h1 class="text-h4 font-weight-bold mb-4 color-primary">
                Bem-vindo ao bingo-ai!
              </h1>
              <p class="text-body-1 text-grey-darken-1 mb-6">
                A plataforma híbrida de bingo com marcação automática assistida em tempo real.
              </p>
              <v-btn color="primary" size="large" class="rounded-lg">
                Começar Onboarding
              </v-btn>
            </v-card>
          </v-col>
        </v-row>
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
// Main layout setup
</script>

<style scoped>
.color-primary {
  color: #1A237E;
}
</style>
