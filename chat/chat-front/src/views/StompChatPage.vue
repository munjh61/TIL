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
                            <div v-for="(msg, index) in messages" :key="index">
                                {{ msg }}
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
import webstomp from 'webstomp-client';

const messages = ref([])
const newMessage = ref('')
const chatbox = ref(null)
const stompClient = ref(null)

onMounted(() => {
    connectWebSocket()
})

onUnmounted(() => {
    if (stompClient.value) {
        stompClient.value.disconnect()
    }
})

function connectWebSocket() {
    // sockJS는 websocket을 내장한 js 라이브러리, http 엔드포인트 사용
    const sockJs = new SockJS(`${import.meta.env.VITE_BASE_URL}/connect`)
    stompClient.value = webstomp.over(sockJs)

    stompClient.value.connect({},() => {
        stompClient.subscribe(`/topic/1`, async (message) => {
            messages.value.push(message.data)
            await nextTick()
            scrollToBottom()
        })
    })

}

function sendMessage() {
    if (newMessage.value.trim() === '') return
    stompClient.value.send(`/publish/1`, newMessage.value)
    newMessage.value = ''
}

function scrollToBottom() {
    if (!chatbox.value) return
    chatbox.value.scrollTop = chatbox.value.scrollHeight
}

</script>
<style scoped>
.chat-box {
    height: 300px;
    overflow-y: auto;
    border: 1px solid #ddd;
    margin-bottom: 10px;
}
</style>