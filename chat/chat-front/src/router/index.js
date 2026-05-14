import LoginPage from "@/views/LoginPage.vue"
import MemberCreate from "@/views/MemberCreate.vue"
import { createRouter, createWebHistory } from "vue-router"

const routes = [
    {
        path:'/member/create',
        name: 'MemberCreate',
        component: MemberCreate
    },
    {
        path:'/login',
        name: 'LoginPage',
        component: LoginPage
    }
]
const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router