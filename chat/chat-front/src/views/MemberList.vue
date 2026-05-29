<template>
    <v-container>
        <v-row>
            <v-col>
                <v-card>
                    <v-card-title class="text-center text-h5">
                        회원목록
                    </v-card-title>
                    <v-card-text>
                        <v-table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>이름</th>
                                    <th>email</th>
                                    <th>채팅</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="member in memberList" :key="member.id">
                                    <td>{{ member.id }}</td>
                                    <td>{{ member.name }}</td>
                                    <td>{{ member.email }}</td>
                                    <td><v-btn color="primary" @click="startChat(member.id)">채팅하기</v-btn></td>
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
import { useRouter } from 'vue-router';

const router = useRouter()

const memberList = ref([])

getMemberList()

async function getMemberList() {
    const res = await axios.get(`${import.meta.env.VITE_BASE_URL}/member/list`)
    memberList.value = res.data
}

async function startChat(otherMemberId){
    // 기존의 채팅방이 있으면 return 받고, 없으면 새로운 roomId return 받음
    const res = await axios.post(`${import.meta.env.VITE_BASE_URL}/chat/room/private/create?otherMemberId=${otherMemberId}`)
    const roomId = res.data
    router.push(`/chatpage/${roomId}`)
}
</script>
<style scoped></style>