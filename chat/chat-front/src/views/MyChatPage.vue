<template>
    <v-container>
        <v-row>
            <v-col>
                <v-card>
                    <v-card-title class="text-center text-h5">
                        내 채팅 목록
                    </v-card-title>
                    <v-card-text>
                        <v-table>
                            <thead>
                                <tr>
                                    <th>채팅방 이름</th>
                                    <th>읽지 않은 메시지</th>
                                    <th>액션</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="chat in chatList" :key="chat.roomId">
                                    <td>{{ chat.name }}</td>
                                    <td>{{ chat.unReadCount }}</td>
                                    <td>
                                        <v-btn color="primary" @click="enterChatRoom(chat.roomId)">입장</v-btn>
                                        <v-btn color="secondary" :disabled="chat.isGroupChat === 'Y'" @click="leaveChatRoom(chat.roomId)">퇴장</v-btn>
                                    </td>
                                </tr>
                            </tbody>
                        </v-table>
                    </v-card-text>
                </v-card>
            </v-col>
        </v-row>
    </v-container>
</template>
<script setup>
import axios from 'axios';
import { ref } from 'vue';
import { useRouter } from 'vuetify/lib/composables/router.mjs';

const router = useRouter()
const chatList = ref([])

getChatList()

async function getChatList() {
    const res = await axios.get(`${import.meta.env.VITE_BASE_URL}/chat/my/rooms`)
    chatList.value = res.data
}

function enterChatRoom(roomId){
    router.push(`/chatpage/${roomId}`)
}

async function leaveChatRoom(roomId){
    await axios.delete(`${import.meta.env.VITE_BASE_URL}/chat/room/group/${roomId}/leave`)
    chatList.value = chatList.value.filter(chat => chat.roomId!== roomId)
}

</script>
<style scoped></style>