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
import axios from 'axios';
import SockJS from 'sockjs-client';
import { nextTick, onMounted, onUnmounted, ref } from 'vue';
import { onBeforeRouteLeave, useRoute } from 'vue-router';
import webstomp from 'webstomp-client';

const route = useRoute()

const messages = ref([])
const newMessage = ref('')
const chatbox = ref(null)
const stompClient = ref(null)
const token = ref('')
const senderEmail = ref(null)
const roomId = ref(null);


onMounted(() => {
    senderEmail.value = localStorage.getItem("email")
    roomId.value = route.params.roomId;
    loadHistory()
    connectWebSocket()
})

onBeforeRouteLeave((to, from, next) => {
    disconnectWebSocket()
    next()
})

onUnmounted(() => {
    disconnectWebSocket()
})

function connectWebSocket() {
    if (stompClient.value && stompClient.value.connected) return // 이미 연결된 경우 중복 연결 방지

    // SockJS는 WebSocket 연결을 브라우저 호환성 있게 지원하는 라이브러리
    // 서버의 STOMP endpoint(/connect)로 연결 요청. http 사용
    const sockJs = new SockJS(`${import.meta.env.VITE_BASE_URL}/connect`)
    stompClient.value = webstomp.over(sockJs)
    token.value = localStorage.getItem("token")
    stompClient.value.connect(
        // STOMP CONNECT frame header
        {
            Authorization: `Bearer ${token.value}`
        },
        // 연결 성공시
        () => {
            console.log('STOMP 연결 성공')
            stompClient.value.subscribe(
                // 구독 destination
                `/topic/${roomId.value}`,
                // 받은 메세지로 할 행동
                async (message) => {
                    const parseMessage = JSON.parse(message.body)
                    messages.value.push(parseMessage)
                    await nextTick()
                    scrollToBottom()
                },
                // SUBSCRIBE 요청 시 함께 보낼 header
                // 필수는 아니지만, 서버에서 구독 권한 검증이 필요한 경우 token 전달
                {
                    Authorization: `Bearer ${token.value}`
                }
            )
        },
        // 연결 실패시
        (error) => {
            console.error('STOMP 연결 실패')
        }
    )
}

function sendMessage() {
    if (newMessage.value.trim() === '') return

    const message = {
        senderEmail: senderEmail.value,
        message: newMessage.value
    }
    stompClient.value.send(`/publish/${roomId.value}`, JSON.stringify(message))
    newMessage.value = ''
}

function scrollToBottom() {
    if (!chatbox.value) return
    chatbox.value.scrollTop = chatbox.value.scrollHeight
}

async function disconnectWebSocket() {

    // 읽은 메시지 처리 (이 부분은 어떻게 구현하느냐에 따라 상이함)
    await axios.post(`${import.meta.env.VITE_BASE_URL}/chat/room/${roomId.value}/read`)

    // disconnect
    if (stompClient.value && stompClient.value.connected) {
        stompClient.value.unsubscribe(`/topic/${roomId.value}`)
        stompClient.value.disconnect()
    }
}

async function loadHistory() {
    const res = await axios.get(`${import.meta.env.VITE_BASE_URL}/chat/history/${roomId.value}`)
    messages.value = res.data
}

</script>
<style scoped>
.chat-box {
    height: 300px;
    overflow-y: auto;
    border: 1px solid #ddd;
    margin-bottom: 10px;
}

.chat-message {
    margin-bottom: 10px;
}

.sent {
    text-align: right;
}

.received {
    text-align: left;
}
</style>