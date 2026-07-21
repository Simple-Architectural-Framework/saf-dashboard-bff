<script lang="ts" setup>
import { ref, onMounted } from 'vue'

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
    <h1>Redirecting to Security Gateway...</h1>
    <div>    
      <button type="button" @click="login">Click here if not redirected</button>
    </div>
  </div>
</template>

<style scoped>
</style>