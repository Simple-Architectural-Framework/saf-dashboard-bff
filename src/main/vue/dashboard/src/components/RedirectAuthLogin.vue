<!-- <script lang="ts" setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router';

const router = useRouter();

defineProps<{
  msg: string
}>()

const generateRandomState = (): string => {
    const array = new Uint32Array(8);
    window.crypto.getRandomValues(array);
    return Array.from(array, dec => ('0' + dec.toString(16)).padStart(8, '0')).join('');
}

const randomState = generateRandomState();

const handleCallback = async (): Promise<void> => {
    const urlParams = new URLSearchParams(window.location.search);
    const error = urlParams.get('error');

    if(error === 'login_required' || error === 'interaction_required') {
        console.log('User session has expired. Redirecting to login page...');
        await router.push('/login');
        return;
    }


    const code = urlParams.get('code');
    if (code) {
        console.log('Valid active session detected');
        // Here you would typically exchange the authorization code for an access token
        // For example, you might make a POST request to your backend to handle this
        await router.push('/home');
    } else {
        console.log('No authorization code found in the URL.');
    }
}

const checkSession = async(): Promise<void> => {

    if(window.location.search.includes('code=')) {
        await handleCallback();
        return;
    }

    const baseUrl = "https://authentik.local/application/o/authorize/";
  
    const queryParams = new URLSearchParams({
        response_type: "code",
        client_id: "rhVj2KBmlvm0DY9rJiLL68Gg4f5mLKIYB8K1PqJE",
        redirect_uri: "http://localhost:8081/saf-dashboard-bff/api/home",
        // redirect_uri: window.location.origin,
        scope: "openid profile email",
        // state: "xyz123randomstring" // Consider generating this dynamically for security
        state: randomState // Consider generating this dynamically for security
    });

    window.location.href = `${baseUrl}?${queryParams.toString()}`;

}


const login = async(): Promise<void> => {
  await checkSession();
};


</script>



<template>
  <h1>redirect auth login</h1>
    <div>    
    <button type="button" @click="login">Authentik Login</button>
  </div>
</template>

<style scoped>
</style> -->


<script lang="ts" setup>
import { ref, onMounted } from 'vue'

defineProps<{
  msg: string
}>()

/*const login = (): void => {
  // Let Spring Boot initiate the secure authorization flow.
  // It handles state validation, code exchanges, and token generation on the server.
  window.location.href = '/saf-dashboard-bff/api/oauth2/authorization/authentik';
};*/


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