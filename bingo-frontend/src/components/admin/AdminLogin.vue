<template>
  <v-container class="fill-height justify-center bg-gradient-admin py-12 px-4" fluid>
    <v-row justify="center" align="center" class="w-100">
      <v-col cols="12" sm="8" md="5" lg="4">
        <!-- Logo / Brand Header -->
        <div class="text-center mb-8">
          <v-avatar color="indigo-darken-4" size="80" class="elevation-4 mb-4 glow-indigo">
            <v-icon icon="mdi-shield-lock" size="48" color="white"></v-icon>
          </v-avatar>
          <h1 class="text-h4 font-weight-black text-white tracking-wider mb-1">
            Bingo Fácil
          </h1>
          <p class="text-subtitle-1 text-indigo-lighten-3 font-weight-medium">
            Painel Administrativo
          </p>
        </div>

        <!-- Login Card -->
        <v-card class="elevation-12 rounded-xl border-card-glass pa-6 pa-sm-8" color="rgba(255, 255, 255, 0.9)">
          <v-card-title class="text-h5 font-weight-bold text-indigo-darken-4 text-center px-0 pb-4">
            Entrar no Sistema
          </v-card-title>

          <!-- Error Feedback -->
          <v-alert
            v-if="authStore.error"
            type="error"
            variant="tonal"
            density="compact"
            class="mb-6 rounded-lg text-body-2 font-weight-semibold"
            closable
          >
            {{ authStore.error }}
          </v-alert>

          <v-form ref="form" v-model="formValid" @submit.prevent="handleLogin">
            <!-- Username Input -->
            <v-text-field
              v-model="username"
              label="Nome de usuário"
              placeholder="Digite seu usuário"
              prepend-inner-icon="mdi-account-circle"
              variant="outlined"
              color="indigo"
              density="comfortable"
              class="mb-4 rounded-lg bg-white"
              :rules="[v => !!v || 'Usuário é obrigatório']"
              required
            ></v-text-field>

            <!-- Password Input -->
            <v-text-field
              v-model="password"
              label="Senha de acesso"
              placeholder="Digite sua senha"
              prepend-inner-icon="mdi-lock-outline"
              :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
              :type="showPassword ? 'text' : 'password'"
              variant="outlined"
              color="indigo"
              density="comfortable"
              class="mb-2 rounded-lg bg-white"
              :rules="[v => !!v || 'Senha é obrigatória']"
              @click:append-inner="showPassword = !showPassword"
              required
            ></v-text-field>

            <!-- Forgot Password Link -->
            <div class="text-right mb-6">
              <router-link
                to="/admin/forgot-password"
                class="text-caption font-weight-bold text-indigo decoration-none-hover"
              >
                Esqueceu sua senha?
              </router-link>
            </div>

            <!-- Submit Button -->
            <v-btn
              type="submit"
              color="indigo-darken-4"
              size="large"
              block
              :loading="authStore.isLoading"
              :disabled="!formValid"
              class="rounded-lg font-weight-bold py-3 elevation-2 text-white"
              min-height="48px"
            >
              Entrar
            </v-btn>
          </v-form>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const router = useRouter()

const username = ref('')
const password = ref('')
const showPassword = ref(false)
const formValid = ref(false)
const form = ref<any>(null)

const handleLogin = async () => {
  if (!formValid.value) return

  const success = await authStore.login(username.value, password.value)
  if (success) {
    router.push('/admin')
  }
}
</script>

<style scoped>
.bg-gradient-admin {
  background: linear-gradient(135deg, #1a237e 0%, #311b92 50%, #001035 100%);
  min-height: 100vh;
}

.border-card-glass {
  border: 1px solid rgba(255, 255, 255, 0.4) !important;
  backdrop-filter: blur(10px);
}

.glow-indigo {
  box-shadow: 0 0 15px rgba(63, 81, 181, 0.5) !important;
}

.decoration-none-hover {
  text-decoration: none;
}

.decoration-none-hover:hover {
  text-decoration: underline;
}

.tracking-wider {
  letter-spacing: 0.1em;
}
</style>
