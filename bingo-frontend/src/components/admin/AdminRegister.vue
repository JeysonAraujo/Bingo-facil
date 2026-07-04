<template>
  <v-container class="py-8 bg-light-gray min-height-main">
    <v-row justify="center">
      <v-col cols="12" md="8" lg="6">
        <!-- Back Button & Page Header -->
        <v-btn
          variant="text"
          prepend-icon="mdi-arrow-left"
          color="indigo-darken-4"
          class="font-weight-bold mb-4"
          to="/admin"
        >
          Voltar ao Painel
        </v-btn>

        <v-card class="elevation-3 rounded-xl border-indigo pa-6 pa-sm-8 bg-white">
          <!-- Card Header -->
          <div class="d-flex align-center mb-6">
            <v-avatar color="indigo-lighten-5" size="56" class="mr-4">
              <v-icon icon="mdi-account-plus" size="32" color="indigo-darken-4"></v-icon>
            </v-avatar>
            <div>
              <h1 class="text-h5 font-weight-black color-primary mb-1">
                Cadastrar Administrador
              </h1>
              <p class="text-caption text-grey-darken-1 mb-0">
                Apenas Super-Administradores podem criar outros acessos administrativos.
              </p>
            </div>
          </div>

          <v-divider class="mb-6"></v-divider>

          <!-- Feedback Alerts -->
          <v-alert
            v-if="authStore.error"
            type="error"
            variant="tonal"
            class="mb-6 rounded-lg font-weight-medium"
            closable
          >
            {{ authStore.error }}
          </v-alert>

          <v-alert
            v-if="successMsg"
            type="success"
            variant="tonal"
            class="mb-6 rounded-lg font-weight-medium"
            closable
          >
            {{ successMsg }}
          </v-alert>

          <!-- Signup Form -->
          <v-form ref="form" v-model="formValid" @submit.prevent="handleSignup">
            <!-- Username Input -->
            <v-text-field
              v-model="username"
              label="Nome de Usuário"
              placeholder="Ex: joao_silva"
              prepend-inner-icon="mdi-account-circle-outline"
              variant="outlined"
              color="indigo"
              class="mb-4 rounded-lg"
              :rules="[
                v => !!v || 'Username é obrigatório',
                v => v.length >= 3 || 'Deve ter no mínimo 3 caracteres'
              ]"
              required
            ></v-text-field>

            <!-- Email Input -->
            <v-text-field
              v-model="email"
              label="Endereço de E-mail"
              placeholder="Ex: joao@exemplo.com"
              type="email"
              prepend-inner-icon="mdi-email-outline"
              variant="outlined"
              color="indigo"
              class="mb-4 rounded-lg"
              :rules="[
                v => !!v || 'E-mail é obrigatório',
                v => /.+@.+\..+/.test(v) || 'Formato de e-mail inválido'
              ]"
              required
            ></v-text-field>

            <!-- Password Input -->
            <v-text-field
              v-model="password"
              label="Senha Inicial"
              placeholder="Digite a senha temporária"
              type="password"
              prepend-inner-icon="mdi-lock-outline"
              variant="outlined"
              color="indigo"
              class="mb-4 rounded-lg"
              :rules="[
                v => !!v || 'Senha é obrigatória',
                v => v.length >= 4 || 'Senha deve ter no mínimo 4 caracteres'
              ]"
              required
            ></v-text-field>

            <!-- Role Selector -->
            <v-select
              v-model="role"
              label="Papel / Permissões"
              :items="[
                { title: 'Administrador Comum (Gerencia seus bingos)', value: 'ROLE_ADMIN' },
                { title: 'Super Administrador (Acesso total)', value: 'ROLE_SUPER_ADMIN' }
              ]"
              item-title="title"
              item-value="value"
              prepend-inner-icon="mdi-shield-outline"
              variant="outlined"
              color="indigo"
              class="mb-6 rounded-lg"
              :rules="[v => !!v || 'Selecione um papel']"
              required
            ></v-select>

            <!-- Action Buttons -->
            <v-btn
              type="submit"
              color="indigo-darken-4"
              size="large"
              block
              :loading="authStore.isLoading"
              :disabled="!formValid"
              class="rounded-lg font-weight-bold text-white mb-3"
              min-height="48px"
            >
              Criar Conta Administrador
            </v-btn>
          </v-form>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()

const username = ref('')
const email = ref('')
const password = ref('')
const role = ref('ROLE_ADMIN')

const formValid = ref(false)
const form = ref<any>(null)
const successMsg = ref<string | null>(null)

const handleSignup = async () => {
  if (!formValid.value) return

  successMsg.value = null
  const payload = {
    username: username.value,
    email: email.value,
    password: password.value,
    role: role.value
  }

  const success = await authStore.signup(payload)
  if (success) {
    successMsg.value = `Administrador "${username.value}" criado com sucesso!`
    // Reset form
    username.value = ''
    email.value = ''
    password.value = ''
    role.value = 'ROLE_ADMIN'
    if (form.value) {
      form.value.resetValidation()
    }
  }
}
</script>

<style scoped>
.bg-light-gray {
  background-color: #f5f5f7;
}

.min-height-main {
  min-height: calc(100vh - 64px);
}

.border-indigo {
  border: 2px solid #e0e0e0 !important;
}

.color-primary {
  color: #1a237e;
}
</style>
