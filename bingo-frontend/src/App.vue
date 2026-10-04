<template>
  <v-app>
    <v-app-bar color="primary" density="compact" elevation="2">
      <!--<v-app-bar-title class="text-white font-weight-bold">
        bingo-ai
      </v-app-bar-title>!-->

      <!-- Componente de imagem -->
      <img src="./assets/logo-bingo-facil-hor.svg" alt="Bingo Fácil – O bingo mais emocionante da internet" height="150px" width="150px" class="my-2">

      <v-spacer></v-spacer>
      <v-chip color="secondary" variant="flat" size="small" class="mr-2 text-primary font-weight-bold">
        🔴 Live
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

.v-toolbar {
  padding-left: 20px;
  padding-top: 10px;
  padding-bottom: 10px;
}
</style>
