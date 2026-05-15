import LoginPage from "@/views/LoginPage.vue"
import MemberCreate from "@/views/MemberCreate.vue"
import MemberList from "@/views/MemberList.vue"
import SimpleWebSocket from "@/views/SimpleWebSocket.vue"
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
    },
    {
        path:'/member/list',
        name: 'MemberList',
        component: MemberList
    },
    {
        path:'/simple/chat',
        name: 'SimpleWebSocket',
        component: SimpleWebSocket
    }
]
const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router