<template>
    <v-container>
        <v-row>
            <v-col>
                <v-card>
                    <v-card-title class="text-center text-h5">
                        채팅방 목록
                        <div class="d-flex justify-end">
                            <v-btn color="secondary" @click="showCreateRoomModal = true">
                                채팅방 생성
                            </v-btn>
                        </div>
                    </v-card-title>
                    <v-card-text>
                        <v-table>
                            <thead>
                                <tr>
                                    <th>방번호</th>
                                    <th>방제목</th>
                                    <th>채팅</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="chat in chatRoomList" :key="chat.roomId">
                                    <td>{{ chat.roomId }}</td>
                                    <td>{{ chat.roomName }}</td>
                                    <td><v-btn color="primary" @click="joinChatRoom(chat.roomId)">참여하기</v-btn></td>
                                </tr>
                            </tbody>
                        </v-table>
                    </v-card-text>
                </v-card>
            </v-col>
        </v-row>
        <v-dialog v-model="showCreateRoomModal" max-width="500px">
            <v-card>
                <v-card-title class="text-h6">
                    채팅방 생성
                </v-card-title>
                <v-card-text>
                    <v-text-field label="방제목" v-model="newRoomTitle" />
                </v-card-text>
                <v-card-actions>
                    <v-btn color="gray" @click="showCreateRoomModal = false">취소</v-btn>
                    <v-btn color="primary" @click="createChatRoom">생성</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
    </v-container>
</template>
<script setup>
import axios from 'axios';
import { ref } from 'vue';
import { useRouter } from 'vuetify/lib/composables/router.mjs';

const chatRoomList = ref([])
const showCreateRoomModal = ref(false)
const newRoomTitle = ref('')
const router = useRouter()

getRoomList()

async function getRoomList() {
    const res = await axios.get(`${import.meta.env.VITE_BASE_URL}/chat/room/group/list`)
    chatRoomList.value = res.data
}

async function joinChatRoom(roomId) {
    await axios.post(`${import.meta.env.VITE_BASE_URL}/chat/room/group/${roomId}/join`)
    router.push(`/chatpage/${roomId}`)
}

async function createChatRoom() {
    await axios.post(`${import.meta.env.VITE_BASE_URL}/chat/room/group/create?roomName=${newRoomTitle.value}`, null)
    showCreateRoomModal.value = false
    getRoomList()
}

</script>
<style scoped></style>