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
import { nextTick, onMounted, onUnmounted, ref } from 'vue';

const ws = ref(null)
const messages = ref([])
const newMessage = ref('')
const chatbox = ref(null)

onMounted(() => {
    connectWebSocket()
})

onUnmounted(() => {
    if (ws.value) {
        ws.value.close()
    }
})

function connectWebSocket() {
    ws.value = new WebSocket("ws://localhost:8080/connect")

    ws.value.onopen = () => {
        console.log("Successfully connected!!")
    }

    ws.value.onmessage = async (message) => {
        messages.value.push(message.data)
        await nextTick()
        scrollToBottom()
    }

    ws.value.onclose = () => {
        console.log("Disconnected!!")
    }
}

function sendMessage() {
    if (newMessage.value.trim() === '') return
    ws.value.send(newMessage.value)
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