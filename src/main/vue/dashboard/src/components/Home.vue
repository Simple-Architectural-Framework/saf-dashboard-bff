<script lang="ts" setup>
  import { ref, onMounted } from 'vue'
  import Card from 'primevue/card'
  import Button from 'primevue/button'
  import Tag from 'primevue/tag'
  import Message from 'primevue/message'

  defineProps<{
    msg: string
  }>()

  interface UserProfile {
    name: string,
    surname: string,
    email: string,
    groups: string[]
  }

  const user = ref<UserProfile | null>(null);
  const loading = ref<boolean>(true);
  const error = ref<string | null>(null);
  
  onMounted(async () : Promise<void> => {
    try {
      const response = await fetch('/saf-dashboard-bff/api/auth/user-info', {
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

  const hasRole = (roleName : string ): boolean => {
    return !!user.value?.groups.includes(roleName);
  }


  const launchRelatedBoard = async (appName : string) : Promise<void> => {
      const response = await fetch(`/saf-dashboard-bff/api/auth/redirect-url?appName=${appName}`, {
        method: 'GET',
        headers: {
          'Accept': 'application/json'
        }
      });

      if(!response.ok){
        throw new Error('Failed to find related app url')
      }

      const data = await response.json();
      if(data.url){
        window.location.href=data.url
      }
  }

</script>

<template>
  <div class="dashboard-container">
    <!-- Header Message / Welcome Banner -->
     <Message severity="info" :closable="false" class="mb-4">
        Welcome <strong>{{ user?.name }}</strong>!
     </Message>

     <!-- Dashboard Title -->
      <div class="dashboard-hader">
        <h1>Application Dashboard</h1>
      </div>

      <div class="grid">
        <div v-if="hasRole('saf-dashboard-admin') || hasRole('saf-dashboard-s1000d')" class="col-12 md:col-6 lg:col-4">
          <Card class="border-blue">
            <template #title>
              <div class="flex align-items-center justify-content-between">
                Content Management
                <Tag value="Editor" severity="info" />
              </div>
            </template>
            <template #content>
              <p class="m-0">
                Authorized to modify frontend layouts, change banners, and update card variables.
              </p>
            </template>
            <template #footer>
              <Button label="Launch Editor" icon="pi pi-pencil" class="w-full" security="info" />
            </template>
          </Card>
        </div>
        <div v-if="hasRole('saf-dashboard-admin') || hasRole('saf-meeting-bff')" class="col-12 md:col-6 lg:col-4">
          <Card class="border-blue">
            <template #title>
              <div class="flex align-items-center justify-content-between">
                Meeting Board
                <Tag value="Meeting" severity="info"/>
              </div>
            </template>
            <template #content>
              <p class="m-0">
                meeting application
              </p>
            </template>
            <template #footer>
              <Button 
                label="Launch Meeting Board" 
                icon="pi pi-pencil" 
                class="w-full" 
                security="info"
                @click="launchRelatedBoard(`saf-meeting-bff`)"
              />
            </template>
          </Card>
        </div>
      </div>
  </div>

</template>

<style scoped>
.dashboard-container {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.dashboard-header {
  margin-bottom: 2rem;
}

.dashboard-header h1 {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.metric-value {
  font-size: 1.75rem;
  font-weight: bold;
  margin-top: 1rem;
  color: var(--p-primary-color, #3b82f6);
}

.border-blue {
  border-left: 4px solid var(--p-blue-500, #3b82f6);
}

.border-danger {
  border-left: 4px solid var(--p-red-500, #ef4444);
}

.mx-1 {
  margin-left: 0.25rem;
  margin-right: 0.25rem;
}

.mb-4 {
  margin-bottom: 1.5rem;
}
</style>