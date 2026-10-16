<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import Button from 'primevue/button'
import ProgressSpinner from 'primevue/progressspinner'
import Message from 'primevue/message'

defineProps<{
  msg: string
}>()



const loading = ref<boolean>(true);
const error = ref<string | null>(null);

const login = async () : Promise<void> => {
  try{
    const response = await fetch('/saf-dashboard-bff/api/auth/login', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        }
    });

    if(response.ok){
      const body = await response.json();
      if(body.redirectUrl){
        window.location.href = body.redirectUrl;
      }
    } else {
      throw new Error('Failed to login,');
    }
  }catch(err : any){
      error.value = err.message || 'An error occurred';
  }finally{
    loading.value = false;
  }
}

// Optional: Automatically trigger the login step when the component mounts 
// so the user doesn't have to manually click the button.
onMounted(() => {
  login();
})
</script>

<template>
  <div class="auth-redirect-container">
    <div class="auth-card">
      <span class="brand-mark"><i class="pi pi-shield" /></span>
      <h1>Signing you in</h1>
      <p class="sub">Redirecting to the security gateway…</p>

      <ProgressSpinner v-if="!error" class="spinner" strokeWidth="4" />
      <Message v-else severity="error" :closable="false" class="w-full">{{ error }}</Message>

      <Button
        label="Click here if not redirected"
        severity="secondary"
        text
        class="w-full"
        @click="login"
      />
    </div>
  </div>
</template>

<style scoped>
.auth-redirect-container {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 1.5rem;
  background:
    radial-gradient(800px 400px at 20% 0%, rgba(99, 102, 241, 0.18), transparent 60%),
    radial-gradient(700px 400px at 100% 100%, rgba(139, 92, 246, 0.14), transparent 60%),
    var(--app-bg);
}

.auth-card {
  width: 100%;
  max-width: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 2.5rem 2rem 1.5rem;
  text-align: center;
  border-radius: var(--app-radius);
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  box-shadow: var(--app-shadow-lg);
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 3.5rem;
  height: 3.5rem;
  border-radius: 16px;
  font-size: 1.5rem;
  color: #fff;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  box-shadow: 0 10px 24px -8px rgba(99, 102, 241, 0.7);
}

h1 {
  margin: 0.5rem 0 0;
  font-size: 1.5rem;
  font-weight: 800;
}

.sub {
  margin: -0.5rem 0 0;
  color: var(--app-text-muted);
}

.spinner {
  width: 2.75rem;
  height: 2.75rem;
  margin: 0.5rem 0;
}
</style>
