<script lang="ts" setup>
  import { ref, computed, onMounted } from 'vue'
  import Button from 'primevue/button'
  import Avatar from 'primevue/avatar'
  import Skeleton from 'primevue/skeleton'
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

  interface AppModule {
    title: string,
    tag: string,
    description: string,
    icon: string,
    accent: string,
    roles: string[],
    actionLabel: string,
    appName?: string
  }

  const modules: AppModule[] = [
    {
      title: 'S1000D Module',
      tag: 'Editor',
      description: 'Authorized to modify frontend layouts, change banners, and update card variables.',
      icon: 'pi pi-book',
      accent: 'indigo',
      roles: ['saf-dashboard-admin', 'saf-s1000d-bff'],
      actionLabel: 'Launch Editor',
      appName: 'saf-s1000d-bff'
    },
    {
      title: 'Meeting Module',
      tag: 'Meeting',
      description: 'Meeting application',
      icon: 'pi pi-calendar',
      accent: 'emerald',
      roles: ['saf-dashboard-admin', 'saf-meeting-bff'],
      actionLabel: 'Launch Meeting Board',
      appName: 'saf-meeting-bff'
    }
  ]

  const user = ref<UserProfile | null>(null);
  const loading = ref<boolean>(true);
  const error = ref<string | null>(null);
  const launching = ref<string | null>(null);

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

  const visibleModules = computed(() => modules.filter(m => m.roles.some(hasRole)));

  const initials = computed(() => {
    const first = user.value?.name?.charAt(0) ?? '';
    const last = user.value?.surname?.charAt(0) ?? '';
    return (first + last).toUpperCase() || '?';
  });

  const greeting = computed(() => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
  });

  const launchRelatedBoard = async (appName : string) : Promise<void> => {
    launching.value = appName;
    console.log("appName is: " +  appName);
    console.log("appName after encodeURIComponent func: " + encodeURIComponent(appName));
    try {
      const response = await fetch(`/saf-dashboard-bff/api/auth/redirect-url?appName=${encodeURIComponent(appName)}`, {
        method: 'GET',
        headers: {
          'Accept': 'application/json'
        }
      });

      if(!response.ok){
        throw new Error('Failed to find related app url')
      }

      // const data = await response.json();
      const { url } = await response.json();
      const target = new URL(url, window.location.origin);
      window.location.assign(target.href);

      // if(data.url){
      //   window.location.href=data.url
      // }
    } finally {
      launching.value = null;
    }
  }

</script>

<template>
  <div class="page">
    <!-- Top bar -->
    <header class="topbar">
      <div class="topbar-inner">
        <div class="brand">
          <span class="brand-mark"><i class="pi pi-th-large" /></span>
          <span class="brand-name">SAF Dashboard</span>
        </div>
        <div v-if="user" class="user-chip">
          <div class="user-meta">
            <span class="user-name">{{ user.name }} {{ user.surname }}</span>
            <span class="user-email">{{ user.email }}</span>
          </div>
          <Avatar :label="initials" shape="circle" class="user-avatar" />
        </div>
        <Skeleton v-else-if="loading" shape="circle" size="2.5rem" />
      </div>
    </header>

    <main class="content">
      <!-- Hero -->
      <section class="hero">
        <template v-if="loading">
          <Skeleton width="10rem" height="1rem" class="mb-3" />
          <Skeleton width="22rem" height="2.25rem" />
        </template>
        <template v-else>
          <p class="eyebrow">{{ greeting }}</p>
          <h1 class="hero-title">
            Welcome back<span v-if="user">, {{ user.name }}</span> 👋
          </h1>
          <p class="hero-sub">Pick an application below to get started.</p>
        </template>
      </section>

      <Message v-if="error" severity="error" :closable="false" class="mb-4">
        {{ error }}
      </Message>

      <!-- Applications -->
      <section>
        <div class="section-head">
          <h2>Your applications</h2>
          <span v-if="!loading" class="count">{{ visibleModules.length }}</span>
        </div>

        <div v-if="loading" class="app-grid">
          <div v-for="n in 3" :key="n" class="app-card">
            <Skeleton width="3rem" height="3rem" borderRadius="14px" class="mb-4" />
            <Skeleton width="60%" height="1.25rem" class="mb-2" />
            <Skeleton width="90%" height="0.9rem" class="mb-4" />
            <Skeleton width="100%" height="2.5rem" borderRadius="10px" />
          </div>
        </div>

        <div v-else-if="visibleModules.length" class="app-grid">
          <article
            v-for="module in visibleModules"
            :key="module.title"
            class="app-card"
            :class="`accent-${module.accent}`"
          >
            <div class="app-card-top">
              <span class="app-icon"><i :class="module.icon" /></span>
              <span class="app-tag">{{ module.tag }}</span>
            </div>
            <h3 class="app-title">{{ module.title }}</h3>
            <p class="app-desc">{{ module.description }}</p>
            <Button
              :label="module.actionLabel"
              icon="pi pi-arrow-right"
              iconPos="right"
              class="w-full app-action"
              :loading="launching === module.appName"
              :disabled="!module.appName"
              @click="module.appName && launchRelatedBoard(module.appName)"
            />
          </article>
        </div>

        <div v-else-if="!error" class="empty">
          <i class="pi pi-lock" />
          <p>You don't have access to any applications yet.</p>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background:
    radial-gradient(1200px 400px at 10% -10%, rgba(99, 102, 241, 0.12), transparent 60%),
    radial-gradient(900px 400px at 100% 0%, rgba(16, 185, 129, 0.08), transparent 60%),
    var(--app-bg);
}

/* Top bar */
.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  backdrop-filter: saturate(180%) blur(12px);
  background: color-mix(in srgb, var(--app-surface) 75%, transparent);
  border-bottom: 1px solid var(--app-border);
}

.topbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0.75rem 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 2.25rem;
  height: 2.25rem;
  border-radius: 10px;
  color: #fff;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  box-shadow: 0 6px 16px -6px rgba(99, 102, 241, 0.6);
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  line-height: 1.2;
}

.user-name {
  font-weight: 600;
  font-size: 0.9rem;
}

.user-email {
  font-size: 0.8rem;
  color: var(--app-text-muted);
}

.user-avatar {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-weight: 600;
}

/* Content */
.content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 3rem 1.5rem 4rem;
}

.hero {
  margin-bottom: 2.5rem;
}

.eyebrow {
  margin: 0 0 0.5rem;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--p-primary-color);
}

.hero-title {
  margin: 0;
  font-size: clamp(1.75rem, 3vw, 2.5rem);
  font-weight: 800;
}

.hero-sub {
  margin: 0.5rem 0 0;
  color: var(--app-text-muted);
}

.section-head {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  margin-bottom: 1.25rem;
}

.section-head h2 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 700;
}

.count {
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.125rem 0.5rem;
  border-radius: 999px;
  background: var(--p-primary-100);
  color: var(--p-primary-700);
}

/* Cards */
.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.25rem;
}

.app-card {
  --accent-from: #6366f1;
  --accent-to: #8b5cf6;
  --accent-soft: rgba(99, 102, 241, 0.1);
  --accent-text: #4f46e5;

  position: relative;
  display: flex;
  flex-direction: column;
  padding: 1.5rem;
  border-radius: var(--app-radius);
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  box-shadow: var(--app-shadow-sm);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--app-shadow-lg);
  border-color: color-mix(in srgb, var(--accent-from) 35%, var(--app-border));
}

.accent-emerald {
  --accent-from: #10b981;
  --accent-to: #14b8a6;
  --accent-soft: rgba(16, 185, 129, 0.1);
  --accent-text: #059669;
}

.app-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.25rem;
}

.app-icon {
  display: grid;
  place-items: center;
  width: 3rem;
  height: 3rem;
  border-radius: 14px;
  font-size: 1.25rem;
  color: #fff;
  background: linear-gradient(135deg, var(--accent-from), var(--accent-to));
  box-shadow: 0 8px 20px -8px var(--accent-from);
}

.app-tag {
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.25rem 0.625rem;
  border-radius: 999px;
  background: var(--accent-soft);
  color: var(--accent-text);
}

.app-title {
  margin: 0 0 0.375rem;
  font-size: 1.125rem;
  font-weight: 700;
}

.app-desc {
  margin: 0 0 1.5rem;
  flex: 1;
  font-size: 0.9rem;
  color: var(--app-text-muted);
}

.app-card :deep(.app-action) {
  background: linear-gradient(135deg, var(--accent-from), var(--accent-to));
  border: none;
}

.app-card :deep(.app-action:not(:disabled):hover) {
  filter: brightness(1.08);
}

/* Empty state */
.empty {
  text-align: center;
  padding: 3rem 1rem;
  border: 1px dashed var(--app-border);
  border-radius: var(--app-radius);
  color: var(--app-text-muted);
  background: var(--app-surface);
}

.empty i {
  font-size: 1.75rem;
  margin-bottom: 0.5rem;
}

@media (max-width: 600px) {
  .user-meta {
    display: none;
  }

  .content {
    padding-top: 2rem;
  }
}
</style>
