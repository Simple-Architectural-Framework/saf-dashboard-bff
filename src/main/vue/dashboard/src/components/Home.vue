<script lang="ts" setup>
  import { ref, onMounted } from 'vue'
  defineProps<{
    msg: string
  }>()

  interface UserProfile {
    name: string,
    surname: string,
    email: string
  }

  const user = ref<UserProfile | null>(null);
  const loading = ref<boolean>(true);
  const error = ref<string | null>(null);
  
  onMounted(async () => {
    try {
      const response = await fetch('/saf-dashboard-bff/api/user-info', {
        method: 'GET',
        headers: {
          'Accept': 'application/json'
        }
      });

      if(!response.ok){
        throw new Error('Failed to fetch authenticated user details,');
      }

      const data = await response.json();
      user.value = data;
    } catch(err : any){
      error.value = err.message || 'An error occurred,';
    } finally {
      loading.value = false;
    }
  });


</script>

<template>
  <div class="home-container">
    <h1>Dashboard</h1>
    <div v-if="loading">Loading user profile...</div>
    <div v-else-if="user">
      <p>Hello <strong>{{ user.name }}</strong>!</p>
      <p>Your email is: <strong>{{ user.email }}</strong></p>
    </div>
    <div v-else-if="error" class="error-msg">
      {{ error }}
    </div>
  </div>
</template>