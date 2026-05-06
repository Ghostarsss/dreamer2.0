import { createWebHistory, createRouter } from 'vue-router'

import Welcome from "@/page/Welcome.vue";
import Login from "@/page/Login.vue";
import Register from "@/page/Register.vue";

const routes = [
    { path: '/', redirect: '/home' },
    { path: '/home', component: Welcome },
    { path: '/login', component: Login },
    { path: '/register', component: Register },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

export default router