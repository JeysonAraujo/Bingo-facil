<template>
  <v-app>
    <v-app-bar color="primary" density="compact" elevation="2">
      <v-app-bar-title class="text-white font-weight-bold">
        bingo-ai
      </v-app-bar-title>
      <v-spacer></v-spacer>
      <v-chip color="secondary" variant="flat" size="small" class="mr-2 text-primary font-weight-bold">
        🔴 Live - Sincronizado
      </v-chip>
    </v-app-bar>

    <v-main class="bg-background">
      <router-view></router-view>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
// App root container
</script>

<style>
/* Global base overrides */
.v-application {
  font-family: 'Roboto', 'Inter', sans-serif !important;
}
</style>
