<template>
  <v-container class="fill-height justify-center bg-gradient-admin py-12 px-4" fluid>
    <v-row justify="center" align="center" class="w-100">
      <v-col cols="12" sm="8" md="5" lg="4">
        <!-- Brand Header -->
        <div class="text-center mb-8">
          <v-avatar color="indigo-darken-4" size="80" class="elevation-4 mb-4 glow-indigo">
            <v-icon icon="mdi-lock-reset" size="48" color="white"></v-icon>
          </v-avatar>
          <h1 class="text-h4 font-weight-black text-white tracking-wider mb-1">
            Bingo-AI
          </h1>
          <p class="text-subtitle-1 text-indigo-lighten-3 font-weight-medium">
            Recuperação de Senha
          </p>
        </div>

        <!-- Forgot Password Card -->
        <v-card class="elevation-12 rounded-xl border-card-glass pa-6 pa-sm-8" color="rgba(255, 255, 255, 0.9)">
          <v-card-title class="text-h5 font-weight-bold text-indigo-darken-4 text-center px-0 pb-4">
            {{ step === 1 ? 'Recuperar Senha' : 'Definir Nova Senha' }}
          </v-card-title>

          <!-- Status Alerts -->
          <v-alert
            v-if="authStore.error"
            type="error"
            variant="tonal"
            class="mb-6 rounded-lg text-body-2 font-weight-semibold"
            closable
          >
            {{ authStore.error }}
          </v-alert>

          <v-alert
            v-if="infoMsg"
            type="info"
            variant="tonal"
            class="mb-6 rounded-lg text-body-2 font-weight-semibold"
            closable
          >
            {{ infoMsg }}
          </v-alert>

          <!-- Step 1: Request Recovery Code -->
          <v-form v-if="step === 1" ref="formRequest" v-model="formValid" @submit.prevent="handleRequestCode">
            <p class="text-body-2 text-grey-darken-3 mb-6">
              Insira seu endereço de e-mail cadastrado. Enviaremos um código temporário de 6 dígitos para você alterar sua senha.
            </p>

            <v-text-field
              v-model="email"
              label="Endereço de E-mail"
              placeholder="Digite seu e-mail"
              prepend-inner-icon="mdi-email-outline"
              variant="outlined"
              color="indigo"
              density="comfortable"
              class="mb-6 rounded-lg bg-white"
              :rules="[
                v => !!v || 'E-mail é obrigatório',
                v => /.+@.+\..+/.test(v) || 'E-mail inválido'
              ]"
              required
            ></v-text-field>

            <v-btn
              type="submit"
              color="indigo-darken-4"
              size="large"
              block
              :loading="authStore.isLoading"
              :disabled="!formValid"
              class="rounded-lg font-weight-bold py-3 elevation-2 text-white mb-4"
              min-height="48px"
            >
              Enviar Código
            </v-btn>
          </v-form>

          <!-- Step 2: Validate Code and Change Password -->
          <v-form v-else ref="formReset" v-model="formValid" @submit.prevent="handleResetPassword">
            <p class="text-body-2 text-grey-darken-3 mb-4">
              Código de verificação enviado! Verifique sua caixa de entrada de e-mail (e spam), digite o código de 6 dígitos recebido e defina sua nova senha.
            </p>

            <!-- Readonly Email -->
            <v-text-field
              v-model="email"
              label="E-mail"
              variant="outlined"
              color="indigo"
              density="comfortable"
              class="mb-4 rounded-lg bg-white"
              readonly
              disabled
            ></v-text-field>

            <!-- Reset Code Input -->
            <v-text-field
              v-model="code"
              label="Código de Verificação"
              placeholder="Digite o código de 6 dígitos"
              prepend-inner-icon="mdi-numeric"
              variant="outlined"
              color="indigo"
              density="comfortable"
              class="mb-4 rounded-lg bg-white"
              :rules="[
                v => !!v || 'O código é obrigatório',
                v => v.length === 6 || 'O código deve conter 6 dígitos'
              ]"
              required
            ></v-text-field>

            <!-- New Password Input -->
            <v-text-field
              v-model="newPassword"
              label="Nova Senha"
              placeholder="Mínimo 4 caracteres"
              type="password"
              prepend-inner-icon="mdi-lock-outline"
              variant="outlined"
              color="indigo"
              density="comfortable"
              class="mb-6 rounded-lg bg-white"
              :rules="[
                v => !!v || 'A nova senha é obrigatória',
                v => v.length >= 4 || 'A senha deve conter no mínimo 4 caracteres'
              ]"
              required
            ></v-text-field>

            <v-btn
              type="submit"
              color="indigo-darken-4"
              size="large"
              block
              :loading="authStore.isLoading"
              :disabled="!formValid"
              class="rounded-lg font-weight-bold py-3 elevation-2 text-white mb-4"
              min-height="48px"
            >
              Confirmar Nova Senha
            </v-btn>
          </v-form>

          <!-- Back to Login Button -->
          <v-btn
            variant="text"
            block
            color="indigo-darken-4"
            class="font-weight-bold rounded-lg text-indigo decoration-none-hover"
            to="/admin/login"
          >
            Voltar para o Login
          </v-btn>
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

const email = ref('')
const code = ref('')
const newPassword = ref('')

const step = ref(1)
const formValid = ref(false)
const infoMsg = ref<string | null>(null)

const handleRequestCode = async () => {
  if (!formValid.value) return

  infoMsg.value = null
  const success = await authStore.forgotPassword(email.value)
  if (success) {
    infoMsg.value = 'Código de verificação enviado para o seu e-mail cadastrado! Por favor, verifique sua caixa de entrada e spam.'
    step.value = 2
  }
}

const handleResetPassword = async () => {
  if (!formValid.value) return

  infoMsg.value = null
  const payload = {
    email: email.value,
    code: code.value,
    newPassword: newPassword.value
  }

  const success = await authStore.resetPassword(payload)
  if (success) {
    alert('Senha alterada com sucesso! Redirecionando para o login...')
    router.push('/admin/login')
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
