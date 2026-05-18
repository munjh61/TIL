<template>
    <v-container>
        <v-row justify="center">
            <v-col cols="12" md="8">
                <v-card>
                    <v-card-title class="text-center text-h5">
                        채팅
                    </v-card-title>
                    <v-card-text>
                        <div class="chat-box" ref="chatbox">
                            <div v-for="(msg, index) in messages" :key="index"
                                :class="['chat-message', msg.senderEmail === senderEmail ? 'sent' : 'received']">
                                <strong>{{ msg.senderEmail }}: </strong> {{ msg.message }}
                            </div>
                        </div>
                        <v-text-field v-model="newMessage" label="메시지 입력" @keyup.enter="sendMessage" />
                        <v-btn color="primary" @click="sendMessage">전송</v-btn>
                    </v-card-text>
                </v-card>
            </v-col>
        </v-row>
    </v-container>
</template>
<script setup>
import SockJS from 'sockjs-client';
import { nextTick, onMounted, onUnmounted, ref } from 'vue';
import { onBeforeRouteLeave } from 'vue-router';
import webstomp from 'webstomp-client';

const messages = ref([])
const newMessage = ref('')
const chatbox = ref(null)
const stompClient = ref(null)
const token = ref('')
const senderEmail = ref(null)

onMounted(() => {
    senderEmail.value = localStorage.getItem("email")
    connectWebSocket()
})

onBeforeRouteLeave((to, from, next)=>{
    if (stompClient.value) {
        disconnectWebSocket()
    }
    next()
})

onUnmounted(() => {
    if (stompClient.value) {
        disconnectWebSocket()
    }
})

function connectWebSocket() {
    if(stompClient.value && stompClient.value.connected) return // 중복된 객체 만들지 않도록
    // sockJS는 websocket을 내장한 js 라이브러리, http 엔드포인트 사용
    const sockJs = new SockJS(`${import.meta.env.VITE_BASE_URL}/connect`)
    stompClient.value = webstomp.over(sockJs)
    token.value = localStorage.getItem("token")
    stompClient.value.connect(
        {
            Authorization: `Bearer ${token.value}`
        },
        () => {
            console.log('STOMP 연결 성공')
            stompClient.value.subscribe(`/topic/1`, async (message) => {
                const parseMessage = JSON.parse(message.body)
                messages.value.push(parseMessage)
                await nextTick()
                scrollToBottom()
            })
        },
        (error) => {
            console.error('STOMP 연결 실패:')
        }
    )
}

function sendMessage() {
    if (newMessage.value.trim() === '') return

    const message = {
        senderEmail: senderEmail.value,
        message: newMessage.value
    }
    stompClient.value.send(`/publish/1`, JSON.stringify(message))
    newMessage.value = ''
}

function scrollToBottom() {
    if (!chatbox.value) return
    chatbox.value.scrollTop = chatbox.value.scrollHeight
}

function disconnectWebSocket(){
    if(stompClient.value && stompClient.value.connected){
        stompClient.value.unsubscribe(`/topic/1`)
        stompClient.value.disconnect()
    }
}

</script>
<style scoped>
.chat-box {
    height: 300px;
    overflow-y: auto;
    border: 1px solid #ddd;
    margin-bottom: 10px;
}

.chat-message{
    margin-bottom: 10px;
}

.sent{
    text-align: right;
}

.received{
    text-align: left;
}
</style>