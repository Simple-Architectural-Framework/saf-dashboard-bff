import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  // history: createWebHistory(import.meta.env.BASE_URL),
  history: createWebHistory('/saf-dashboard-bff/api'),
  routes: [
    {
      path: '/',
      redirect: '/redirect-auth-login'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../components/Login.vue')
    },
    {
      path: '/redirect-auth-login',
      name: 'RedirectAuthLogin',
      component: () => import('../components/RedirectAuthLogin.vue')
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('../components/Home.vue')
    }
  ],
})

// Global interceptor to route the user without needing a backend forward controller
router.beforeEach((to, from, next) => {
  const urlParams = new URLSearchParams(window.location.search);
  
  if (urlParams.get('loginSuccess') === 'true') {
    console.log('Detected successful OAuth2 entry, routing straight to Dashboard...');
    
    // Clear the query parameter from the address bar to keep things clean
    window.history.replaceState({}, document.title, window.location.pathname);
    
    // Force Vue Router to go to your home view component
    next('/home');
  } else {
    next();
  }
}
)



export default router
