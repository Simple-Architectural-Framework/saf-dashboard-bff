<script lang="ts" setup>
import { ref } from 'vue'

defineProps<{
  msg: string
}>()

interface Post {
    id: number;
    title: string;
    body: string;
}

const posts = ref<Post[]>([]);
const isLoading = ref<boolean>(true);
const error = ref<string | null>(null);


const login = async (): Promise<void> => {
    const userField = document.getElementById('userNameTextBox') as HTMLInputElement;
    const passField = document.getElementById('passwordTextBox') as HTMLInputElement;

    if(!userField || !passField) {
        error.value = 'Username or password field not found';
        return;
    }

    const queryParams = new URLSearchParams({
        username: userField.value,
        password: passField.value,
        jss: '1234567890'
    });

    try{
        const response = await fetch(`/saf-dashboard-bff/api/auth/login?${queryParams.toString()}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: null,
        });

        if (!response.ok) {
            throw new Error(`Login failed: ${response.statusText}`);
        } else {
          const data = await response.json();
          // if (data.token) {
          //   localStorage.setItem('authToken', data.token);
          //   await router.push('/home');
          // }
        }

    } catch (err: unknown) {
        const message = err instanceof Error ? err.message : String(err);
        console.error('Login error:', message);
    }
}
</script>

<template>
  <h1>{{ msg }}</h1>
  <div>
    <input
      id="userNameTextBox"
      className="loginNameBox"
      type="text"
      placeholder="UserName"
      value=""
      />
    <br />
    <input
      id="passwordTextBox"
      className="loginPassBox"
      type="password"
      placeholder="Password"
      value=""
      />
  </div>
  <br />
  <div>    
    <button type="button" @click="login">Login</button>
  </div>
</template>

<style scoped>
.loginNameBox {
  width: 200px;
  height: 30px;
}
.loginPassBox {
  width: 200px;
  height: 30px;
  -webkit-text-security: disc;
}
button {
  width: 100px;
  height: 30px;
  background-color: #4CAF50; /* Green */
  border: none;
  color: white;
  text-align: center;
  text-decoration: none;
  display: inline-block;
  font-size: 16px;
}
</style>
