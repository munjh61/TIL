import GroupChattingList from "@/views/GroupChattingList.vue"
import Home from "@/views/Home.vue"
import LoginPage from "@/views/LoginPage.vue"
import MemberCreate from "@/views/MemberCreate.vue"
import MemberList from "@/views/MemberList.vue"
import MyChatPage from "@/views/MyChatPage.vue"
import SimpleWebSocket from "@/views/SimpleWebSocket.vue"
import StompChatPage from "@/views/StompChatPage.vue"
import { createRouter, createWebHistory } from "vue-router"
import { components } from "vuetify/dist/vuetify.js"

const routes = [
    {
        path:'/',
        name:'Home',
        component: Home
    },
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
    },
    {
        path:'/chatpage/:roomId',
        name: 'StompChatPage',
        component: StompChatPage
    },
    {
        path:'/groupchatting/list',
        name: 'GroupChattingList',
        component: GroupChattingList
    },
    {
        path:'/my/chat/page',
        name: 'MyChatPage',
        component: MyChatPage
    }
]
const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router